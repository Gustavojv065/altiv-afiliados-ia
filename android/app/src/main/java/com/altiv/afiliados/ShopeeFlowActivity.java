package com.altiv.afiliados;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.browser.customtabs.CustomTabsIntent;
import java.util.Locale;

/**
 * Central Shopee: o usuario faz login no navegador seguro e cria seu
 * proprio link afiliado. Sem scraping, captura de senha ou disparos automaticos.
 */
public class ShopeeFlowActivity extends Activity {
    private static final String PORTAL = "https://affiliate.shopee.com.br/";
    private static final int BG = Color.rgb(6, 10, 17);
    private static final int SURFACE = Color.rgb(17, 26, 39);
    private static final int INPUT = Color.rgb(24, 36, 50);
    private static final int BLUE = Color.rgb(23, 116, 252);
    private static final int WHITE = Color.rgb(246, 249, 255);
    private static final int MUTED = Color.rgb(167, 184, 206);
    private SharedPreferences drafts;
    private EditText linkField, productField, priceField, captionField;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        drafts = getSharedPreferences("altiv_shopee_local", MODE_PRIVATE);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        showScreen(readShare(getIntent()));
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        showScreen(readShare(intent));
    }

    private String readShare(Intent intent) {
        if (intent == null || !Intent.ACTION_SEND.equals(intent.getAction())) return null;
        CharSequence value = intent.getCharSequenceExtra(Intent.EXTRA_TEXT);
        return value == null ? null : AffiliateLink.extract(value.toString());
    }

    private int dp(int unit) {
        return Math.round(unit * getResources().getDisplayMetrics().density);
    }

    private GradientDrawable surface(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }

    private TextView txt(String label, int size, int color, boolean bold) {
        TextView v = new TextView(this);
        v.setText(label);
        v.setTextSize(size);
        v.setTextColor(color);
        if (bold) v.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        v.setLineSpacing(dp(1), 1.08f);
        return v;
    }

    private View space(int size) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(1, dp(size)));
        return v;
    }

    private Button btn(String title, boolean primary, View.OnClickListener action) {
        Button b = new Button(this);
        b.setText(title);
        b.setAllCaps(false);
        b.setTextSize(15);
        b.setTextColor(WHITE);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setBackground(surface(primary ? BLUE : INPUT, 12));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(54));
        lp.bottomMargin = dp(9);
        b.setLayoutParams(lp);
        b.setOnClickListener(action);
        return b;
    }

    private EditText edit(String hint, boolean multiline) {
        EditText e = new EditText(this);
        e.setTextSize(15);
        e.setTextColor(WHITE);
        e.setHintTextColor(MUTED);
        e.setHint(hint);
        e.setBackground(surface(INPUT, 12));
        e.setPadding(dp(14), dp(12), dp(14), dp(12));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = dp(12);
        e.setLayoutParams(lp);
        if (multiline) {
            e.setMinLines(5);
            e.setGravity(Gravity.TOP);
            e.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                    | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        } else {
            e.setSingleLine(true);
        }
        return e;
    }

    private LinearLayout box(String title, String subtitle) {
        LinearLayout block = new LinearLayout(this);
        block.setOrientation(LinearLayout.VERTICAL);
        block.setPadding(dp(16), dp(16), dp(16), dp(16));
        block.setBackground(surface(SURFACE, 18));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = dp(16);
        block.setLayoutParams(lp);
        block.addView(txt(title, 19, WHITE, true));
        block.addView(space(7));
        block.addView(txt(subtitle, 14, MUTED, false));
        block.addView(space(14));
        return block;
    }

    private void showScreen(String importedLink) {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int horizontal = dp(18);
        layout.setPadding(horizontal, dp(24), horizontal, dp(24));
        layout.setOnApplyWindowInsetsListener((view, insets) -> {
            int top, bottom;
            if (Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets bars = insets.getInsets(
                        WindowInsets.Type.systemBars() | WindowInsets.Type.ime());
                top = bars.top;
                bottom = bars.bottom;
            } else {
                top = insets.getSystemWindowInsetTop();
                bottom = insets.getSystemWindowInsetBottom();
            }
            view.setPadding(horizontal, top + dp(14), horizontal, bottom + dp(24));
            return insets;
        });
        scroll.addView(layout);
        layout.addView(txt("ALTIV • AFILIADOS IA", 12, Color.rgb(138, 188, 255), true));
        layout.addView(space(12));
        layout.addView(txt("Central Shopee", 30, WHITE, true));
        layout.addView(space(8));
        layout.addView(txt("Entre com sua conta na Shopee, gere seu link e divulgue pelo WhatsApp.", 15, MUTED, false));
        layout.addView(space(22));

        LinearLayout portal = box("01  Sua conta Shopee",
                "Abre o portal oficial em uma aba segura do navegador Android. Sua senha não passa pelo ALTIV.");
        portal.addView(btn("Abrir portal de afiliados", true, v -> openPortal()));
        layout.addView(portal);

        LinearLayout product = box("02  Seu produto",
                "Gere o link de afiliado na Shopee, copie e volte para colar aqui; ou compartilhe para o ALTIV.");
        product.addView(btn("Colar link copiado", false, v -> pasteLink()));
        linkField = edit("https://s.shopee.com.br/...", false);
        linkField.setText(importedLink != null ? importedLink : drafts.getString("link", ""));
        product.addView(txt("Link de afiliado", 13, WHITE, true));
        product.addView(space(6));
        product.addView(linkField);
        productField = edit("Ex.: Fone Bluetooth", false);
        productField.setText(drafts.getString("product", ""));
        product.addView(txt("Produto", 13, WHITE, true));
        product.addView(space(6));
        product.addView(productField);
        priceField = edit("Ex.: 49,90 — opcional", false);
        priceField.setText(drafts.getString("price", ""));
        product.addView(txt("Preço informado por você (opcional)", 13, WHITE, true));
        product.addView(space(6));
        product.addView(priceField);
        layout.addView(product);

        LinearLayout post = box("03  Criar e compartilhar",
                "A primeira versão oferece modelo de legenda local. Revise antes de publicar; a IA Qwen virá depois.");
        captionField = edit("A legenda vai aparecer aqui...", true);
        captionField.setText(importedLink != null ? "" : drafts.getString("caption", ""));
        post.addView(btn("Montar legenda", true, v -> makeCaption()));
        post.addView(captionField);
        post.addView(btn("Salvar rascunho", false, v -> {
            if (save()) toast("Rascunho salvo neste aparelho.");
        }));
        post.addView(btn("Compartilhar no WhatsApp", true, v -> shareWhatsApp()));
        post.addView(btn("Copiar mensagem", false, v -> copyMessage()));
        post.addView(txt("Você escolhe os grupos ou contatos no WhatsApp e confirma o envio. Não há disparo automático.", 12, MUTED, false));
        layout.addView(post);
        layout.addView(txt("ALTIV • Beta 9 • Conteúdo publicitário identificado com #publi", 12, MUTED, false));
        setContentView(scroll);
        scroll.requestApplyInsets();
        if (importedLink != null) toast("Link da Shopee importado. Confira se é seu link de afiliado.");
        else if (Intent.ACTION_SEND.equals(getIntent() == null ? null : getIntent().getAction())) {
            toast("Compartilhamento sem um link HTTPS Shopee válido.");
        }
    }

    private void openPortal() {
        try {
            new CustomTabsIntent.Builder()
                    .setShowTitle(true)
                    .setToolbarColor(BLUE)
                    .build()
                    .launchUrl(this, Uri.parse(PORTAL));
        } catch (Exception error) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(PORTAL)));
            } catch (ActivityNotFoundException missing) {
                toast("Nenhum navegador instalado para abrir a Shopee.");
            }
        }
    }

    private void pasteLink() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = clipboard == null ? null : clipboard.getPrimaryClip();
        CharSequence text = clip != null && clip.getItemCount() > 0
                ? clip.getItemAt(0).coerceToText(this) : null;
        String url = text == null ? null : AffiliateLink.extract(text.toString());
        if (url == null) {
            toast("Copie um link HTTPS Shopee na sua conta de afiliado.");
            return;
        }
        linkField.setText(url);
        linkField.setSelection(url.length());
        toast("Link colado. Confirme se ele contém seu código de afiliado.");
    }

    private String validLink() {
        String current = linkField.getText().toString().trim();
        String url = AffiliateLink.extract(current);
        if (url == null || !current.equals(url)) {
            toast("Informe apenas uma URL HTTPS legítima da Shopee.");
            return null;
        }
        return url;
    }

    private boolean save() {
        String url = validLink();
        if (url == null) return false;
        drafts.edit()
                .putString("link", url)
                .putString("product", productField.getText().toString().trim())
                .putString("price", priceField.getText().toString().trim())
                .putString("caption", captionField.getText().toString().trim())
                .apply();
        return true;
    }

    private void makeCaption() {
        String url = validLink();
        if (url == null) return;
        String product = productField.getText().toString().trim();
        if (product.isEmpty()) {
            toast("Informe o nome do produto para gerar a legenda.");
            productField.requestFocus();
            return;
        }
        String amount = priceField.getText().toString().trim();
        StringBuilder message = new StringBuilder("🛍️ Olha esse achado na Shopee!\n\n");
        message.append(product).append("\n");
        if (!amount.isEmpty()) message.append("Preço informado: R$ ").append(amount).append(" (confira na loja)\n");
        message.append("\nVeja o produto e confira as condições:\n")
                .append(url).append("\n\n#publi #Shopee");
        captionField.setText(message.toString());
        save();
    }

    private String message() {
        String url = validLink();
        if (url == null) return null;
        String value = captionField.getText().toString().trim();
        if (value.isEmpty()) {
            makeCaption();
            value = captionField.getText().toString().trim();
            if (value.isEmpty()) return null;
        }
        if (!value.contains(url)) value += "\n\nLink: " + url;
        if (!value.toLowerCase(Locale.ROOT).contains("#publi")) value += "\n#publi";
        captionField.setText(value);
        return value;
    }

    private void shareWhatsApp() {
        String value = message();
        if (value == null || !save()) return;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, value);
        for (String app : new String[] {"com.whatsapp", "com.whatsapp.w4b"}) {
            intent.setPackage(app);
            try {
                startActivity(intent);
                return;
            } catch (ActivityNotFoundException ignored) {
                // Continua para WhatsApp Business e, depois, para o seletor Android.
            }
        }
        intent.setPackage(null);
        try {
            startActivity(Intent.createChooser(intent, "Compartilhar publicação ALTIV"));
        } catch (ActivityNotFoundException error) {
            toast("Instale WhatsApp ou outro aplicativo de mensagens.");
        }
    }

    private void copyMessage() {
        String value = message();
        if (value == null || !save()) return;
        ClipboardManager clipboard =
                (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText("ALTIV oferta", value));
            toast("Mensagem copiada.");
        }
    }

    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }
}
