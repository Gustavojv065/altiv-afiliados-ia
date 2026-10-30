import { NextResponse } from "next/server";
import { authorized } from "@/lib/affiliate";
export const runtime = "nodejs";
export const dynamic = "force-dynamic";

export async function GET(request: Request) {
  if (!authorized(request)) return NextResponse.json({ error: "Acesso não autorizado." }, { status: 401 });
  return NextResponse.json({
    shopeeReady: Boolean(process.env.SHOPEE_APP_ID && process.env.SHOPEE_APP_SECRET),
    telegramReady: Boolean(process.env.TELEGRAM_BOT_TOKEN && process.env.TELEGRAM_CHAT_ID),
    schedule: "GitHub Actions (habilitado após configuração)",
  }, { headers: { "Cache-Control": "no-store" } });
}
