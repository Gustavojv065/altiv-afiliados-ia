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
    private EditText sourceField, captionField;
    private TextView offerStatus;

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
        return value == null ? null : value.toString();
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

    private void showScreen(String sharedText) {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int side = dp(18);
        layout.setPadding(side, dp(24), side, dp(24));
        layout.setOnApplyWindowInsetsListener((view, insets) -> {
            int top, bottom;
            if (Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets bars = insets.getInsets(
                    WindowInsets.Type.systemBars() | WindowInsets.Type.ime());
                top = bars.top; bottom = bars.bottom;
            } else {
                top = insets.getSystemWindowInsetTop();
                bottom = insets.getSystemWindowInsetBottom();
            }
            view.setPadding(side, top + dp(14), side, bottom + dp(24));
            return insets;
        });
        scroll.addView(layout);
        layout.addView(txt("ALTIV • AFILIADOS IA", 12, Color.rgb(138, 188, 255), true));
        layout.addView(space(10));
        layout.addView(txt("Achadinhos Shopee", 30, WHITE, true));
        layout.addView(space(8));
        layout.addView(txt("Encontre ofertas no aplicativo Shopee, copie as informações e publique com um toque.", 15, MUTED, false));
        layout.addView(space(18));

        LinearLayout discovery = box("01  Encontrar ofertas",
            "No app Shopee: Eu → Criadores e Afiliados → Recomendação. Avalie preço, comissão e avaliações antes de divulgar.");
        discovery.addView(btn("NOVO • Radar automático de ofertas", true, v -> startActivity(new Intent(this, AutomationActivity.class))));
        discovery.addView(btn("Abrir aplicativo Shopee", false, v -> openShopeeApp()));
        discovery.addView(btn("Abrir portal no navegador", false, v -> openPortal()));
        discovery.addView(txt("A Shopee é aberta na tela inicial; você escolhe os produtos. A busca automática depende de acesso oficial à API.", 12, MUTED, false));
        layout.addView(discovery);

        LinearLayout imported = box("02  Copiar informações",
            "Na Shopee, use Compartilhar → Copiar Informações. Cole tudo de uma vez, inclusive a descrição e o link.");
        imported.addView(btn("Colar informações da Shopee", true, v -> pasteInformation()));
        sourceField = edit("Ex.: Dê uma olhada nesse produto por R$14,90 ... https://s.shopee.com.br/...", true);
        sourceField.setMinLines(3);
        String previous = drafts.getString("source", "");
        if (previous.isEmpty()) {
            String oldLink = drafts.getString("link", "");
            if (!oldLink.isEmpty()) {
                previous = drafts.getString("caption", "");
                if (previous.isEmpty()) previous = drafts.getString("product", "");
                if (!previous.contains(oldLink)) previous += " " + oldLink;
            }
        }
        sourceField.setText(sharedText == null ? previous : sharedText);
        imported.addView(sourceField);
        offerStatus = txt("Cole a descrição e o link para identificar a oferta.", 13, MUTED, false);
        imported.addView(offerStatus);
        imported.addView(space(12));
        imported.addView(btn("Preparar anúncio", false, v -> prepare()));
        layout.addView(imported);

        LinearLayout publish = box("03  Prévia da publicação",
            "Uma mensagem e um único link. Revise o conteúdo antes de enviar.");
        captionField = edit("A legenda aparecerá aqui...", true);
        captionField.setMinLines(5);
        captionField.setText(sharedText == null ? drafts.getString("preview_v10", "") : "");
        publish.addView(captionField);
        publish.addView(btn("Compartilhar no WhatsApp", true, v -> shareWhatsApp()));
        publish.addView(btn("Copiar mensagem", false, v -> copyMessage()));
        publish.addView(btn("Salvar rascunho", false, v -> {
            if (save()) toast("Rascunho salvo neste celular.");
        }));
        publish.addView(txt("No WhatsApp, selecione seu grupo e confirme. Não há disparo automático.", 12, MUTED, false));
        layout.addView(publish);
        layout.addView(txt("ALTIV • Beta 10 • Sem acesso à API Shopee ou IA Qwen nesta etapa", 12, MUTED, false));
        setContentView(scroll);
        scroll.requestApplyInsets();
        ShopeeShareParser.Offer detected = ShopeeShareParser.parse(sourceField.getText().toString());
        if (sharedText != null) {
            if (detected != null) {
                prepare();
                toast("Texto e link importados da Shopee.");
            } else {
                toast("Não encontrei um link válido da Shopee.");
            }
        } else if (detected != null) {
            showDetected(detected);
            if (captionField.getText().toString().trim().isEmpty()) prepare();
        }
        sourceField.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int start,int count,int after) {}
            @Override public void onTextChanged(CharSequence s,int start,int before,int count) {
                captionField.setText("");
                offerStatus.setText("Texto alterado: toque em Preparar anúncio.");
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void showDetected(ShopeeShareParser.Offer offer) {
        offerStatus.setText("✓ " + offer.title
            + (offer.price.isEmpty() ? "" : " • R$ " + offer.price)
            + "\n✓ Link reconhecido. Confirme que é o seu link de afiliado.");
    }

    private void openShopeeApp() {
        try {
            Intent launch = getPackageManager().getLaunchIntentForPackage("com.shopee.br");
            if (launch != null) {
                startActivity(launch);
                return;
            }
        } catch (Exception ignored) {
            // Sem acesso ao launcher do app no aparelho.
        }
        toast("Shopee não encontrada. Abrindo portal no navegador.");
        openPortal();
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

    private void pasteInformation() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = clipboard == null ? null : clipboard.getPrimaryClip();
        CharSequence text = clip != null && clip.getItemCount() > 0
                ? clip.getItemAt(0).coerceToText(this) : null;
        if (text == null || text.length() == 0) {
            toast("Copie as informações do produto na Shopee primeiro.");
            return;
        }
        if (ShopeeShareParser.parse(text.toString()) == null) {
            toast("O conteúdo copiado não inclui link HTTPS válido da Shopee.");
            return;
        }
        sourceField.setText(text);
        prepare();
        toast("Oferta importada com texto, preço e link. Revise o anúncio.");
    }

    private void prepare() {
        ShopeeShareParser.Offer offer =
                ShopeeShareParser.parse(sourceField.getText().toString());
        if (offer == null) {
            offerStatus.setText("Não foi encontrado link HTTPS válido da Shopee.");
            toast("Copie as informações ou um link gerado na Shopee.");
            return;
        }
        showDetected(offer);
        String content = offer.description.isEmpty()
                ? "🛍️ Confira esta oferta na Shopee!" : offer.description;
        captionField.setText(ShopeeShareParser.compose(offer, content));
        save();
    }

    private boolean save() {
        ShopeeShareParser.Offer offer =
                ShopeeShareParser.parse(sourceField.getText().toString());
        if (offer == null) {
            toast("Importe uma oferta com um link válido da Shopee.");
            return false;
        }
        drafts.edit().putString("source", sourceField.getText().toString())
                .putString("preview_v10", captionField.getText().toString())
                .apply();
        return true;
    }

    private String message() {
        ShopeeShareParser.Offer offer =
                ShopeeShareParser.parse(sourceField.getText().toString());
        if (offer == null) {
            toast("Importe primeiro informações com o link Shopee.");
            return null;
        }
        String result = ShopeeShareParser.compose(offer, captionField.getText().toString());
        captionField.setText(result);
        showDetected(offer);
        return result;
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
