import { createHash, timingSafeEqual } from "node:crypto";

export type Offer = {
  itemId: string;
  title: string;
  offerLink: string;
  imageUrl: string;
  price: number;
  discount: number;
  rating: number;
  sales: number;
  commissionRate: number;
  shopName: string;
};

export class AffiliateError extends Error {
  constructor(message: string, public status = 502) { super(message); }
}

export function authorized(request: Request): boolean {
  const expected = process.env.ALTIV_ACCESS_TOKEN || "";
  const received = (request.headers.get("authorization") || "").replace(/^Bearer\s+/i, "");
  if (!expected || !received) return false;
  const a = createHash("sha256").update(received).digest();
  const b = createHash("sha256").update(expected).digest();
  return timingSafeEqual(a, b);
}

export function approvedShopeeUrl(candidate: string): boolean {
  try {
    const u = new URL(candidate);
    const host = u.hostname.toLowerCase();
    return u.protocol === "https:" && !u.username && !u.password &&
      (host === "shope.ee" || host === "shopee.com.br" ||
       host.endsWith(".shopee.com.br"));
  } catch { return false; }
}

const QUERY = `query SearchProducts($keyword: String, $page: Int, $limit: Int) {
  productOfferV2(keyword: $keyword, page: $page, limit: $limit) {
    nodes {
      itemId productName offerLink imageUrl priceMin priceDiscountRate
      ratingStar sales commissionRate shopName
    }
  }
}`;

export async function searchOffers(keyword: string, limit = 12): Promise<Offer[]> {
  const id = process.env.SHOPEE_APP_ID;
  const secret = process.env.SHOPEE_APP_SECRET;
  if (!id || !secret) throw new AffiliateError("API da Shopee ainda não configurada no servidor.", 503);
  const body = JSON.stringify({
    query: QUERY,
    operationName: "SearchProducts",
    variables: { keyword: keyword.trim().slice(0, 70), page: 1, limit: Math.min(30, Math.max(1, limit)) },
  });
  const timestamp = Math.floor(Date.now() / 1000);
  const signature = createHash("sha256").update(id + timestamp + body + secret).digest("hex");
  const response = await fetch("https://open-api.affiliate.shopee.com.br/graphql", {
    method: "POST",
    headers: { "Content-Type": "application/json",
      Authorization: `SHA256 Credential=${id},Timestamp=${timestamp},Signature=${signature}` },
    body, cache: "no-store", signal: AbortSignal.timeout(10000)
  });
  if (!response.ok) throw new AffiliateError("A Shopee não respondeu à consulta (" + response.status + ").");
  const payload = await response.json() as {
    data?: { productOfferV2?: { nodes?: Record<string, unknown>[] } };
    errors?: { message?: string }[];
  };
  if (payload.errors?.length) throw new AffiliateError("Erro na API Shopee: " + String(payload.errors[0].message || "consulte as credenciais"));
  const nodes = payload.data?.productOfferV2?.nodes;
  if (!Array.isArray(nodes)) throw new AffiliateError("Resposta da Shopee sem lista de ofertas.");
  return nodes.flatMap((item): Offer[] => {
    const offerLink = String(item.offerLink || "");
    if (!approvedShopeeUrl(offerLink)) return [];
    const numeric = (value: unknown) => Number.isFinite(Number(value)) ? Number(value) : 0;
    return [{
      itemId: String(item.itemId || ""),
      title: String(item.productName || "Produto Shopee").slice(0, 180),
      offerLink,
      imageUrl: String(item.imageUrl || ""),
      price: numeric(item.priceMin),
      discount: numeric(item.priceDiscountRate),
      rating: numeric(item.ratingStar),
      sales: numeric(item.sales),
      commissionRate: numeric(item.commissionRate),
      shopName: String(item.shopName || "").slice(0, 100)
    }];
  });
}

export function offerMessage(offer: Pick<Offer, "title" | "price" | "discount" | "offerLink">): string {
  if (!approvedShopeeUrl(offer.offerLink)) throw new AffiliateError("Link afiliado inválido.", 400);
  if (!offer.title || offer.title.length > 180 || !Number.isFinite(offer.price) || offer.price < 0)
    throw new AffiliateError("Dados de oferta inválidos.", 400);
  const money = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(offer.price);
  const discount = offer.discount > 0 && offer.discount <= 100 ? "\n🏷️ " + Math.round(offer.discount) + "% OFF" : "";
  return `🛍️ ACHADINHO SHOPEE\n\n${offer.title}\n💰 ${money}${discount}\n\n👉 ${offer.offerLink}\n\n#publi • Link de afiliado. Preços sujeitos a alteração.`;
}

export async function sendTelegram(message: string): Promise<void> {
  const token = process.env.TELEGRAM_BOT_TOKEN || "";
  const chatId = process.env.TELEGRAM_CHAT_ID || "";
  if (!token || !chatId) throw new AffiliateError("Telegram ainda não configurado no servidor.", 503);
  const response = await fetch(`https://api.telegram.org/bot${token}/sendMessage`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ chat_id: chatId, text: message, disable_web_page_preview: false }),
    cache: "no-store", signal: AbortSignal.timeout(10000)
  });
  if (!response.ok) throw new AffiliateError("Telegram não confirmou a publicação (HTTP " + response.status + ").");
  const result = await response.json() as { ok?: boolean };
  if (!result.ok) throw new AffiliateError("Telegram não confirmou a publicação.");
}
