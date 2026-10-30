import { NextResponse } from "next/server";
import { AffiliateError, authorized, approvedShopeeUrl, offerMessage, sendTelegram } from "@/lib/affiliate";
export const runtime = "nodejs";
export const dynamic = "force-dynamic";

export async function POST(request: Request) {
  if (!authorized(request)) return NextResponse.json({ error: "Acesso não autorizado." }, { status: 401 });
  try {
    if (Number(request.headers.get("content-length") || "0") > 8192)
      return NextResponse.json({ error: "Requisição muito grande." }, { status: 413 });
    const offer = await request.json() as { title?: unknown; price?: unknown; discount?: unknown; offerLink?: unknown };
    if (typeof offer.title !== "string" || typeof offer.offerLink !== "string" ||
        !approvedShopeeUrl(offer.offerLink) || offer.title.length > 180 ||
        typeof offer.price !== "number" || typeof offer.discount !== "number")
      return NextResponse.json({ error: "Dados de oferta inválidos." }, { status: 400 });
    await sendTelegram(offerMessage({
      title: offer.title, price: offer.price, discount: offer.discount, offerLink: offer.offerLink
    }));
    return NextResponse.json({ published: true, channel: "telegram" }, { headers: { "Cache-Control": "no-store" } });
  } catch (error) {
    return NextResponse.json({ error: error instanceof Error ? error.message : "Falha na publicação." },
      { status: error instanceof AffiliateError ? error.status : 502 });
  }
}
