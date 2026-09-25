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
import android.os.Bundle;
import android.os.Build;
import android.view.WindowInsets;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final String SUPABASE_URL = "https://sxnlcilvbgqcdbeytnkg.supabase.co";
    private static final String SUPABASE_KEY = "sb_publishable_WNxNS-2w2_NlUp-_7AGU4A_5MegjG1o";
    private static final String AGENT_URL = SUPABASE_URL + "/functions/v1/affiliate-agent";

    private final int BG = Color.rgb(5,8,13);
    private final int SURFACE = Color.rgb(12,18,27);
    private final int SURFACE_2 = Color.rgb(17,25,36);
    private final int BLUE = Color.rgb(20,115,255);
    private final int BLUE_SOFT = Color.rgb(119,173,255);
    private final int GREEN = Color.rgb(73,213,148);
    private final int TEXT = Color.rgb(245,248,255);
    private final int MUTED = Color.rgb(158,172,192);
    private final int WARN = Color.rgb(245,186,79);

    private SharedPreferences prefs;
    private LinearLayout content;
    private Product selectedProduct;
    private String generatedCopy = "";

    private static class Product {
        String id, platform, title, url;
        double price, commission;
        long sales;
        double trend, score;

        Product(String id, String platform, String title, String url, double price, double commission, long sales, double trend, double score) {
            this.id = id;
            this.platform = platform;
            this.title = title;
            this.url = url;
            this.price = price;
            this.commission = commission;
            this.sales = sales;
            this.trend = trend;
            this.score = score;
        }
    }

    private interface NetworkTask { void run() throws Exception; }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void applySystemInsets(View root) {
        root.setOnApplyWindowInsetsListener((v, insets) -> {
            int top;
            int bottom;
            if (Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets bars = insets.getInsets(WindowInsets.Type.systemBars());
                top = bars.top;
                bottom = bars.bottom;
            } else {
                top = insets.getSystemWindowInsetTop();
                bottom = insets.getSystemWindowInsetBottom();
            }
            v.setPadding(v.getPaddingLeft(), top, v.getPaddingRight(), bottom);
            return insets;
        });
        root.requestApplyInsets();
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("altiv_beta6", MODE_PRIVATE);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        if (token().isEmpty()) showLogin();
        else showHome();
    }

    private String token() { return prefs.getString("access_token", ""); }

    private void saveSession(JSONObject auth) {
        String access = auth.optString("access_token", "");
        String refresh = auth.optString("refresh_token", "");
        if (!access.isEmpty()) {
            prefs.edit().putString("access_token", access).putString("refresh_token", refresh).apply();
        }
    }

    private void logout() {
        prefs.edit().remove("access_token").remove("refresh_token").apply();
        selectedProduct = null;
        generatedCopy = "";
        showLogin();
    }

    private GradientDrawable bg(int color, float radius, int strokeColor) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp((int)radius));
        if (strokeColor != Color.TRANSPARENT) d.setStroke(1, strokeColor);
        return d;
    }

    private TextView label(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setLineSpacing(0,1.08f);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private View gap(int h) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(1,dp(h)));
        return v;
    }

    private Button button(String title, boolean primary, View.OnClickListener click) {
        Button b = new Button(this);
        b.setText(title);
        b.setTextColor(Color.WHITE);
        b.setTextSize(15);
        b.setAllCaps(false);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setBackground(primary ? bg(BLUE,14,Color.TRANSPARENT) : bg(SURFACE_2,14,Color.rgb(38,54,76)));
        b.setOnClickListener(click);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,dp(56));
        lp.setMargins(0,dp(4),0,dp(4));
        b.setLayoutParams(lp);
        return b;
    }

    private EditText input(String hint, boolean secret, boolean numeric) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setHintTextColor(Color.rgb(105,122,146));
        e.setTextColor(TEXT);
        e.setTextSize(15);
        e.setPadding(dp(16),dp(12),dp(16),dp(12));
        e.setBackground(bg(SURFACE_2,14,Color.rgb(38,54,76)));
        if (secret) e.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        else if (numeric) e.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        else e.setInputType(InputType.TYPE_CLASS_TEXT);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0,dp(4),0,dp(8));
        e.setLayoutParams(lp);
        return e;
    }

    private LinearLayout card(String eyebrow, String title, String body) {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(16),dp(16),dp(16),dp(16));
        c.setBackground(bg(SURFACE,16,Color.rgb(24,38,56)));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0,dp(6),0,dp(6));
        c.setLayoutParams(lp);

        if (eyebrow != null && !eyebrow.isEmpty()) {
            TextView e = label(eyebrow,12,BLUE_SOFT,true);
            e.setLetterSpacing(.08f);
            c.addView(e);
            c.addView(gap(8));
        }
        c.addView(label(title,20,TEXT,true));
        c.addView(gap(8));
        c.addView(label(body,15,MUTED,false));
        return c;
    }

    private void showLogin() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BG);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20),dp(28),dp(20),dp(24));
        scroll.addView(root);

        TextView brand = label("ALTIV • AGENTE DE ACHADOS",12,BLUE_SOFT,true);
        brand.setLetterSpacing(.10f);
        root.addView(brand);
        root.addView(gap(18));
        root.addView(label("Entre para sincronizar seus achados.",32,TEXT,true));
        root.addView(gap(10));
        root.addView(label("Sua conta mantém produtos, ranking e publicações ligados ao seu usuário.",15,MUTED,false));
        root.addView(gap(24));

        EditText email = input("Seu e-mail",false,false);
        email.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        EditText password = input("Sua senha",true,false);
        root.addView(email);
        root.addView(password);

        root.addView(button("Entrar",true,v -> authenticate(email.getText().toString().trim(),password.getText().toString(),"login")));
        root.addView(button("Criar conta",false,v -> authenticate(email.getText().toString().trim(),password.getText().toString(),"signup")));

        root.addView(gap(18));
        root.addView(card("BETA 8","Layout responsivo","Interface ajustada para Android 15+, com respeito à barra de status, navegação do sistema e medidas em dp."));
        applySystemInsets(scroll);
        setContentView(scroll);
    }

    private void authenticate(String email, String password, String mode) {
        if (email.isEmpty() || password.length() < 6) {
            Toast.makeText(this,"Informe e-mail e senha com pelo menos 6 caracteres",Toast.LENGTH_SHORT).show();
            return;
        }
        toast("Conectando...");
        runAsync(() -> {
            JSONObject body = new JSONObject();
            body.put("email",email);
            body.put("password",password);
            String path = mode.equals("signup") ? "/auth/v1/signup" : "/auth/v1/token?grant_type=password";
            JSONObject response = requestJson("POST",SUPABASE_URL + path,body,null);
            String access = response.optString("access_token","");
            if (access.isEmpty() && mode.equals("signup")) {
                runOnUiThread(() -> Toast.makeText(this,"Conta criada. Se for solicitado, confirme o e-mail e depois entre.",Toast.LENGTH_LONG).show());
                return;
            }
            if (access.isEmpty()) throw new Exception(response.optString("msg",response.optString("error_description","Falha na autenticação")));
            saveSession(response);
            runOnUiThread(this::showHome);
        });
    }

    private void startScreen(String title, String subtitle, int active) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0,1f));

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(18),dp(14),dp(18),dp(18));
        scroll.addView(content);

        TextView brand = label("ALTIV • AGENTE DE ACHADOS",12,BLUE_SOFT,true);
        brand.setLetterSpacing(.10f);
        content.addView(brand);
        content.addView(gap(14));
        content.addView(label(title,31,TEXT,true));
        content.addView(gap(8));
        content.addView(label(subtitle,15,MUTED,false));
        content.addView(gap(22));

        root.addView(scroll);
        root.addView(buildNav(active));
        applySystemInsets(root);
        setContentView(root);
    }

    private LinearLayout buildNav(int active) {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER);
        bar.setPadding(dp(4),dp(4),dp(4),dp(6));
        bar.setBackgroundColor(Color.rgb(7,11,17));

        String[] labels = {"Início","Achados","Criar","Publicar","Fontes"};
        View.OnClickListener[] actions = new View.OnClickListener[]{
                v -> showHome(), v -> loadOpportunities(), v -> showCreator(), v -> showPublish(), v -> showSources()
        };
        for (int i=0;i<labels.length;i++) {
            TextView item = label(labels[i],12,i==active?BLUE_SOFT:MUTED,i==active);
            item.setGravity(Gravity.CENTER);
            item.setPadding(dp(2),dp(10),dp(2),dp(10));
            item.setOnClickListener(actions[i]);
            item.setBackground(i==active?bg(Color.rgb(13,30,52),12,Color.TRANSPARENT):null);
            bar.addView(item,new LinearLayout.LayoutParams(0,dp(56),1f));
        }
        return bar;
    }

    private void showHome() {
        startScreen("Seu agente de produtos rentáveis.", "Encontre, compare, gere conteúdo e publique em poucos passos.",0);

        LinearLayout a = card("1 • ACHADOS","Procurar oportunidades","Veja seus produtos ordenados automaticamente pelo score de vendas, comissão e tendência.");
        a.addView(gap(14));
        a.addView(button("Abrir Achados",true,v -> loadOpportunities()));
        content.addView(a);

        LinearLayout b = card("2 • ADICIONAR","Salvar produto real","Cadastre um produto real da Shopee ou TikTok e envie para o backend analisar.");
        b.addView(gap(14));
        b.addView(button("Adicionar produto",false,v -> showAddProduct()));
        content.addView(b);

        LinearLayout c = card("3 • CONECTAR","Fontes oficiais","Confira o status das contas Shopee e TikTok e prepare as integrações automáticas.");
        c.addView(gap(14));
        c.addView(button("Ver Fontes",false,v -> showSources()));
        content.addView(c);

        content.addView(button("Sair da conta",false,v -> logout()));
    }

    private void showAddProduct() {
        startScreen("Adicionar produto","Salve um produto real para o agente calcular a oportunidade.",1);

        EditText platform = input("Plataforma: shopee ou tiktok_shop",false,false);
        EditText title = input("Nome do produto",false,false);
        EditText url = input("Link do produto / afiliado",false,false);
        EditText price = input("Preço",false,true);
        EditText commission = input("Comissão %",false,true);
        EditText sales = input("Número de vendas",false,true);
        EditText trend = input("Tendência 0 a 100",false,true);

        content.addView(platform); content.addView(title); content.addView(url);
        content.addView(price); content.addView(commission); content.addView(sales); content.addView(trend);

        content.addView(button("Salvar e analisar",true,v -> {
            try {
                JSONObject p = new JSONObject();
                String pf = platform.getText().toString().trim().toLowerCase();
                if (pf.equals("tiktok")) pf = "tiktok_shop";
                p.put("platform",pf);
                p.put("title",title.getText().toString().trim());
                p.put("product_url",url.getText().toString().trim());
                p.put("price",Double.parseDouble(price.getText().toString().replace(",",".")));
                p.put("commission_rate",Double.parseDouble(commission.getText().toString().replace(",",".")));
                p.put("sales_count",Long.parseLong(sales.getText().toString().trim()));
                p.put("trend_score",Double.parseDouble(trend.getText().toString().replace(",",".")));
                if (p.optString("title").isEmpty() || (!pf.equals("shopee") && !pf.equals("tiktok_shop"))) {
                    toast("Informe os dados corretamente");
                    return;
                }
                JSONObject body = new JSONObject();
                body.put("action","save_product");
                body.put("product",p);
                toast("Salvando...");
                runAsync(() -> {
                    JSONObject result = callAgent(body);
                    if (result.has("error")) throw new Exception(result.optString("error"));
                    runOnUiThread(() -> {
                        Toast.makeText(this,"Produto analisado e salvo",Toast.LENGTH_SHORT).show();
                        loadOpportunities();
                    });
                });
            } catch (Exception e) {
                toast("Preencha todos os campos corretamente");
            }
        }));
    }

    private void loadOpportunities() {
        startScreen("Achados","Carregando suas melhores oportunidades...",1);
        runAsync(() -> {
            JSONObject body = new JSONObject();
            body.put("action","opportunities");
            JSONObject result = callAgent(body);
            JSONArray arr = result.optJSONArray("opportunities");
            List<Product> products = new ArrayList<>();
            if (arr != null) {
                for (int i=0;i<arr.length();i++) {
                    JSONObject op = arr.optJSONObject(i);
                    JSONObject p = op != null ? op.optJSONObject("products") : null;
                    if (p == null) continue;
                    products.add(new Product(
                            p.optString("id"),p.optString("platform"),p.optString("title"),p.optString("product_url"),
                            p.optDouble("price"),p.optDouble("commission_rate"),p.optLong("sales_count"),
                            p.optDouble("trend_score"),op.optDouble("score")
                    ));
                }
            }
            runOnUiThread(() -> renderOpportunities(products));
        });
    }

    private void renderOpportunities(List<Product> products) {
        startScreen("Achados","Ranking sincronizado pelo backend ALTIV.",1);
        if (products.isEmpty()) {
            LinearLayout e = card("SEM ACHADOS","Adicione o primeiro produto","Depois de salvar um produto, ele aparece aqui com score calculado.");
            e.addView(gap(14));
            e.addView(button("Adicionar produto",true,v -> showAddProduct()));
            content.addView(e);
            return;
        }

        int pos = 1;
        for (Product p:products) {
            String price = String.format(Locale.US,"R$ %.2f",p.price).replace(".",",");
            String body = "Fonte: "+p.platform+
                    "\nPreço: "+price+
                    "\nVendas: "+p.sales+
                    "\nComissão: "+String.format(Locale.US,"%.1f",p.commission).replace(".",",")+"%"+
                    "\nTendência: "+String.format(Locale.US,"%.0f",p.trend)+"/100";
            LinearLayout c = card("TOP "+pos+" • SCORE "+String.format(Locale.US,"%.0f",p.score)+"/100",p.title,body);
            c.addView(gap(10));
            c.addView(label(p.score>=70?"Potencial alto":"Potencial em análise",13,p.score>=70?GREEN:WARN,true));
            c.addView(gap(14));
            c.addView(button("Criar publicação",true,v -> {
                selectedProduct = p;
                generatedCopy = "";
                showCreator();
            }));
            content.addView(c);
            pos++;
        }
    }

    private void showCreator() {
        startScreen("Criar publicação","Escolha o produto, confirme o link e gere o texto.",2);
        if (selectedProduct == null) {
            LinearLayout e = card("FALTA PRODUTO","Selecione um achado","Abra Achados e escolha o produto que deseja divulgar.");
            e.addView(gap(14));
            e.addView(button("Abrir Achados",true,v -> loadOpportunities()));
            content.addView(e);
            return;
        }

        String price = String.format(Locale.US,"R$ %.2f",selectedProduct.price).replace(".",",");
        content.addView(card("SELECIONADO • SCORE "+String.format(Locale.US,"%.0f",selectedProduct.score),selectedProduct.title,
                selectedProduct.platform+" • "+price+" • comissão "+String.format(Locale.US,"%.1f",selectedProduct.commission).replace(".",",")+"%"));

        EditText link = input("Link de afiliado",false,false);
        link.setText(selectedProduct.url);
        EditText copy = input("Texto da publicação",false,false);
        copy.setMinLines(7);
        if (!generatedCopy.isEmpty()) copy.setText(generatedCopy);

        content.addView(label("1. Link",17,TEXT,true));
        content.addView(link);
        content.addView(gap(12));
        content.addView(label("2. Texto",17,TEXT,true));
        content.addView(copy);

        content.addView(button("Gerar texto com ALTIV",true,v -> {
            toast("Gerando...");
            runAsync(() -> {
                JSONObject p = new JSONObject();
                p.put("title",selectedProduct.title);
                p.put("price",selectedProduct.price);
                p.put("product_url",selectedProduct.url);
                JSONObject body = new JSONObject();
                body.put("action","generate_copy");
                body.put("product",p);
                body.put("affiliate_link",link.getText().toString().trim());
                JSONObject result = callAgent(body);
                String caption = result.optString("caption","");
                if (caption.isEmpty()) throw new Exception("copy_failed");
                generatedCopy = caption;
                runOnUiThread(() -> copy.setText(caption));
            });
        }));

        content.addView(button("Copiar texto + link",false,v -> {
            String value = copy.getText().toString().trim();
            if (value.isEmpty()) { toast("Gere o texto primeiro"); return; }
            generatedCopy = value;
            ClipboardManager cb=(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
            cb.setPrimaryClip(ClipData.newPlainText("ALTIV",value));
            toast("Conteúdo copiado");
        }));

        content.addView(button("Continuar para publicar",true,v -> {
            generatedCopy = copy.getText().toString().trim();
            if (generatedCopy.isEmpty()) { toast("Gere o texto primeiro"); return; }
            showPublish();
        }));
    }

    private void showPublish() {
        startScreen("Publicar","Revise e escolha o canal. Você mantém o controle antes do envio.",3);
        if (selectedProduct == null || generatedCopy.isEmpty()) {
            LinearLayout e = card("SEM CONTEÚDO","Prepare a publicação","Selecione um produto e gere o texto antes de compartilhar.");
            e.addView(gap(14));
            e.addView(button("Ir para Criar",true,v -> showCreator()));
            content.addView(e);
            return;
        }

        content.addView(card("PRONTO PARA PUBLICAR",selectedProduct.title,generatedCopy));
        content.addView(button("Enviar para WhatsApp",true,v -> shareToPackage("com.whatsapp","WhatsApp")));
        content.addView(button("Enviar para Instagram",true,v -> shareToPackage("com.instagram.android","Instagram")));
        content.addView(button("Mais opções",false,v -> shareGeneral()));
    }

    private void showSources() {
        startScreen("Fontes","Consultando o status das integrações...",4);
        runAsync(() -> {
            JSONObject body = new JSONObject();
            body.put("action","source_status");
            JSONObject result = callAgent(body);
            JSONObject sources = result.optJSONObject("sources");
            runOnUiThread(() -> renderSources(sources));
        });
    }

    private void connectSource(String provider, String displayName) {
        toast("Preparando conexão...");
        runAsync(() -> {
            JSONObject body = new JSONObject();
            body.put("action","connect_source");
            body.put("provider",provider);
            body.put("display_name",displayName);
            JSONObject result = callAgent(body);
            if (result.has("error")) throw new Exception(result.optString("error"));
            runOnUiThread(() -> {
                Toast.makeText(this,"Fonte preparada. Falta concluir a autorização oficial.",Toast.LENGTH_LONG).show();
                showSources();
            });
        });
    }

    private void disconnectSource(String provider) {
        toast("Desconectando...");
        runAsync(() -> {
            JSONObject body = new JSONObject();
            body.put("action","disconnect_source");
            body.put("provider",provider);
            callAgent(body);
            runOnUiThread(this::showSources);
        });
    }

    private void syncSource(String provider) {
        toast("Sincronizando...");
        runAsync(() -> {
            JSONObject body = new JSONObject();
            body.put("action","sync_products");
            body.put("provider",provider);
            JSONObject result = callAgent(body);
            if (result.has("error")) throw new Exception(result.optString("error"));
            runOnUiThread(() -> {
                Toast.makeText(this,"Sincronização concluída",Toast.LENGTH_SHORT).show();
                loadOpportunities();
            });
        });
    }

    private void renderSources(JSONObject sources) {
        startScreen("Fontes","Conecte as fontes oficiais sem colocar segredos dentro do APK.",4);
        String shopee = "disconnected";
        String tiktok = "disconnected";
        if (sources != null) {
            JSONObject s = sources.optJSONObject("shopee");
            JSONObject t = sources.optJSONObject("tiktok_shop");
            if (s != null) shopee = s.optString("connection_status","disconnected");
            if (t != null) tiktok = t.optString("connection_status","disconnected");
        }

        LinearLayout s = card("SHOPEE","Status: "+statusLabel(shopee),"Prepare sua conta para a integração oficial e sincronização de produtos.");
        s.addView(gap(12));
        if ("disconnected".equals(shopee)) s.addView(button("Preparar conexão Shopee",true,v -> connectSource("shopee","Shopee Afiliados")));
        else {
            s.addView(button("Tentar sincronizar Shopee",true,v -> syncSource("shopee")));
            s.addView(button("Desconectar Shopee",false,v -> disconnectSource("shopee")));
        }
        s.addView(button("Abrir portal Shopee",false,v -> openUrl("https://affiliate.shopee.com.br/open_api/document?type=overview")));
        content.addView(s);

        LinearLayout t = card("TIKTOK SHOP","Status: "+statusLabel(tiktok),"Prepare sua conta para produtos, analytics e oportunidades oficiais.");
        t.addView(gap(12));
        if ("disconnected".equals(tiktok)) t.addView(button("Preparar conexão TikTok",true,v -> connectSource("tiktok_shop","TikTok Shop")));
        else {
            t.addView(button("Tentar sincronizar TikTok",true,v -> syncSource("tiktok_shop")));
            t.addView(button("Desconectar TikTok",false,v -> disconnectSource("tiktok_shop")));
        }
        t.addView(button("Abrir TikTok Shop Partner",false,v -> openUrl("https://partner.tiktokshop.com/")));
        content.addView(t);

        content.addView(card("PRÓXIMA ETAPA","Conectar automaticamente","Quando as credenciais oficiais forem autorizadas, o agente poderá sincronizar produtos sem cadastro manual."));
    }

    private String statusLabel(String s) {
        if ("connected".equals(s)) return "Conectado";
        if ("pending".equals(s)) return "Pendente";
        if ("error".equals(s)) return "Erro";
        return "Não conectado";
    }

    private JSONObject callAgent(JSONObject body) throws Exception {
        return requestJson("POST",AGENT_URL,body,token());
    }

    private JSONObject requestJson(String method, String endpoint, JSONObject body, String bearer) throws Exception {
        HttpURLConnection c = (HttpURLConnection)new URL(endpoint).openConnection();
        c.setRequestMethod(method);
        c.setConnectTimeout(15000);
        c.setReadTimeout(20000);
        c.setRequestProperty("Content-Type","application/json");
        c.setRequestProperty("apikey",SUPABASE_KEY);
        if (bearer != null && !bearer.isEmpty()) c.setRequestProperty("Authorization","Bearer "+bearer);
        c.setDoOutput(true);

        byte[] bytes = body.toString().getBytes(StandardCharsets.UTF_8);
        try (OutputStream os = c.getOutputStream()) { os.write(bytes); }

        int code = c.getResponseCode();
        InputStream is = code >= 200 && code < 300 ? c.getInputStream() : c.getErrorStream();
        StringBuilder sb = new StringBuilder();
        if (is != null) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is,StandardCharsets.UTF_8))) {
                String line;
                while ((line=br.readLine()) != null) sb.append(line);
            }
        }
        c.disconnect();

        JSONObject result = sb.length()==0 ? new JSONObject() : new JSONObject(sb.toString());
        if (code == 401 && bearer != null) {
            runOnUiThread(() -> {
                Toast.makeText(this,"Sessão expirada. Entre novamente.",Toast.LENGTH_LONG).show();
                logout();
            });
            throw new Exception("unauthorized");
        }
        if (code < 200 || code >= 300) throw new Exception(result.optString("error_description",result.optString("msg",result.optString("error","Erro de conexão"))));
        return result;
    }

    private void runAsync(NetworkTask task) {
        new Thread(() -> {
            try { task.run(); }
            catch (Exception e) {
                String msg = e.getMessage()==null ? "Falha de conexão" : e.getMessage();
                runOnUiThread(() -> Toast.makeText(this,msg,Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void toast(String value) { Toast.makeText(this,value,Toast.LENGTH_SHORT).show(); }

    private void openUrl(String url) {
        try { startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url))); }
        catch (Exception e) { toast("Não foi possível abrir o navegador"); }
    }

    private void shareToPackage(String packageName,String label) {
        Intent intent=new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT,generatedCopy);
        intent.setPackage(packageName);
        try { startActivity(intent); }
        catch (ActivityNotFoundException e) {
            Toast.makeText(this,label+" não encontrado. Abrindo outras opções.",Toast.LENGTH_SHORT).show();
            shareGeneral();
        }
    }

    private void shareGeneral() {
        Intent intent=new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT,generatedCopy);
        startActivity(Intent.createChooser(intent,"Publicar com ALTIV"));
    }

    @Override public void onBackPressed() {
        if (token().isEmpty()) showLogin();
        else showHome();
    }
}
