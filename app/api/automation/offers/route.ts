import { NextResponse } from "next/server";
import { AffiliateError, authorized, searchOffers } from "@/lib/affiliate";
export const runtime = "nodejs";
export const dynamic = "force-dynamic";

export async function GET(request: Request) {
  if (!authorized(request)) return NextResponse.json({ error: "Acesso não autorizado." }, { status: 401 });
  try {
    const params = new URL(request.url).searchParams;
    const keyword = (params.get("keyword") || "ofertas").trim().slice(0, 70);
    const minDiscount = Math.min(100, Math.max(0, Number(params.get("minDiscount") || 0) || 0));
    const offers = (await searchOffers(keyword, 25)).filter(o => o.discount >= minDiscount).slice(0, 15);
    return NextResponse.json({ offers, source: "shopee-affiliate-api" }, { headers: { "Cache-Control": "no-store" } });
  } catch (error) {
    return NextResponse.json({ error: error instanceof Error ? error.message : "Não foi possível buscar ofertas." },
      { status: error instanceof AffiliateError ? error.status : 502 });
  }
}
