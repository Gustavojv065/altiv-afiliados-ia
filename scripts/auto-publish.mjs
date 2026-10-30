import fs from "node:fs/promises";

const base = (process.env.ALTIV_BACKEND_URL || "").replace(/\/+$/, "");
const token = process.env.ALTIV_ACCESS_TOKEN || "";
const dryRun = process.env.DRY_RUN === "true";
if (!base.startsWith("https://") || token.length < 16) {
  console.log("Automação inativa: defina ALTIV_BACKEND_URL (HTTPS) e ALTIV_ACCESS_TOKEN.");
  process.exit(0);
}
const stateFile = "automation-state.json";
let state = { posts: [] };
try {
  const saved = JSON.parse(await fs.readFile(stateFile, "utf8"));
  if (Array.isArray(saved.posts)) state = saved;
} catch { /* primeiro ciclo */ }

const now = Date.now();
state.posts = state.posts.filter(p => p && typeof p.id === "string" &&
  Number.isFinite(p.at) && now - p.at < 30 * 86400000);

const hours = new Date().getUTCHours();
const keyword = hours === 13 ? "casa cozinha" : hours === 17 ? "eletronicos" : "beleza";
async function request(path, options = {}) {
  const response = await fetch(base + path, {
    ...options,
    headers: { Authorization: "Bearer " + token, "Content-Type": "application/json" },
    signal: AbortSignal.timeout(15000)
  });
  const data = await response.json().catch(() => ({}));
  if (!response.ok) throw new Error("Endpoint ALTIV indisponível (" + response.status +
    "): " + String(data.error || "").slice(0, 140));
  return data;
}
const data = await request("/api/automation/offers?keyword=" + encodeURIComponent(keyword) + "&minDiscount=15");
if (!Array.isArray(data.offers)) throw new Error("Servidor não retornou ofertas válidas.");
const known = new Set(state.posts.map(p => p.id));
const eligible = data.offers.filter(p => p && p.itemId && p.offerLink &&
  !known.has(p.itemId) && p.price > 0);
eligible.sort((a, b) => (b.discount - a.discount) ||
  (b.rating - a.rating) || (b.sales - a.sales));
if (eligible.length === 0) {
  console.log("Nenhum produto novo disponível para esta rodada.");
  await fs.writeFile(stateFile, JSON.stringify(state, null, 2));
  process.exit(0);
}
const offer = eligible[0];
if (dryRun) {
  console.log("DRY RUN: item " + offer.itemId + " selecionado; nenhuma publicação enviada.");
} else {
  await request("/api/automation/publish", {
    method: "POST",
    body: JSON.stringify({
      title: offer.title, price: offer.price,
      discount: offer.discount, offerLink: offer.offerLink
    })
  });
  state.posts.push({ id: offer.itemId, at: now });
  console.log("Telegram confirmou publicação do item " + offer.itemId + ".");
}
await fs.writeFile(stateFile, JSON.stringify(state, null, 2));
