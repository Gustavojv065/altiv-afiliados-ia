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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private final int BG = Color.rgb(5,8,13);
    private final int SURFACE = Color.rgb(12,18,27);
    private final int SURFACE_2 = Color.rgb(17,25,36);
    private final int BLUE = Color.rgb(20,115,255);
    private final int BLUE_SOFT = Color.rgb(119,173,255);
    private final int GREEN = Color.rgb(73,213,148);
    private final int TEXT = Color.rgb(245,248,255);
    private final int MUTED = Color.rgb(158,172,192);
    private final int WARN = Color.rgb(245,186,79);

    private LinearLayout content;
    private SharedPreferences prefs;
    private Product selectedProduct;
    private String generatedCopy = "";

    private static class Product {
        String source, name, url;
        double price, commissionPct;
        int sales, trend, score;

        Product(String source, String name, String url, double price, double commissionPct, int sales, int trend) {
            this.source = source;
            this.name = name;
            this.url = url;
            this.price = price;
            this.commissionPct = commissionPct;
            this.sales = sales;
            this.trend = trend;
            this.score = score();
        }

        int score() {
            double salesScore = Math.min(100.0, Math.log10(Math.max(10, sales)) / 5.0 * 100.0);
            double commissionScore = Math.min(100.0, commissionPct * 5.0);
            double trendScore = Math.min(100.0, Math.max(0, trend));
            return (int)Math.round(salesScore * .45 + commissionScore * .35 + trendScore * .20);
        }

        double estimatedCommission() { return price * (commissionPct / 100.0); }
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("altiv_beta5", MODE_PRIVATE);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        showHome();
    }

    private GradientDrawable bg(int color, float radius, int strokeColor) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radius);
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
        v.setLayoutParams(new LinearLayout.LayoutParams(1,h));
        return v;
    }

    private Button primary(String title, View.OnClickListener click) {
        Button b = new Button(this);
        b.setText(title);
        b.setTextColor(Color.WHITE);
        b.setTextSize(15);
        b.setAllCaps(false);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setBackground(bg(BLUE,28,Color.TRANSPARENT));
        b.setOnClickListener(click);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,112);
        lp.setMargins(0,8,0,8);
        b.setLayoutParams(lp);
        return b;
    }

    private Button secondary(String title, View.OnClickListener click) {
        Button b = primary(title,click);
        b.setBackground(bg(SURFACE_2,28,Color.rgb(38,54,76)));
        return b;
    }

    private EditText input(String hint, boolean numeric) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setHintTextColor(Color.rgb(105,122,146));
        e.setTextColor(TEXT);
        e.setTextSize(15);
        e.setPadding(24,18,24,18);
        e.setBackground(bg(SURFACE_2,24,Color.rgb(38,54,76)));
        e.setInputType(numeric ? InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL : InputType.TYPE_CLASS_TEXT);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0,8,0,10);
        e.setLayoutParams(lp);
        return e;
    }

    private LinearLayout card(String eyebrow, String title, String body) {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(26,22,26,22);
        c.setBackground(bg(SURFACE,28,Color.rgb(24,38,56)));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0,10,0,10);
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

    private void startScreen(String title, String subtitle, int active) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0,1f));

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(32,28,32,34);
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
        setContentView(root);
    }

    private LinearLayout buildNav(int active) {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER);
        bar.setPadding(8,8,8,12);
        bar.setBackgroundColor(Color.rgb(7,11,17));

        String[] labels = {"Início","Achados","Criar","Publicar","Fontes"};
        View.OnClickListener[] actions = new View.OnClickListener[]{
                v -> showHome(), v -> showAgent(), v -> showCreator(), v -> showPublish(), v -> showSources()
        };

        for (int i=0;i<labels.length;i++) {
            TextView item = label(labels[i],12,i==active?BLUE_SOFT:MUTED,i==active);
            item.setGravity(Gravity.CENTER);
            item.setPadding(4,18,4,18);
            item.setOnClickListener(actions[i]);
            item.setBackground(i==active?bg(Color.rgb(13,30,52),22,Color.TRANSPARENT):null);
            bar.addView(item,new LinearLayout.LayoutParams(0,88,1f));
        }
        return bar;
    }

    private void showHome() {
        startScreen("Do achado até a publicação.", "Fluxo simples: conectar fonte, avaliar oportunidade, gerar texto e compartilhar.",0);

        LinearLayout a = card("1 • ENCONTRAR","Melhores oportunidades","O ranking considera vendas, comissão e tendência para destacar produtos mais interessantes.");
        a.addView(gap(16));
        a.addView(primary("Ver Achados",v -> showAgent()));
        content.addView(a);

        LinearLayout b = card("2 • PRODUTO REAL","Adicionar produto","Enquanto as APIs automáticas não estão autorizadas, você pode cadastrar um produto real da Shopee ou TikTok e o agente calcula o score.");
        b.addView(gap(16));
        b.addView(secondary("Adicionar produto real",v -> showAddProduct()));
        content.addView(b);

        LinearLayout c = card("3 • PUBLICAR","Link + texto + compartilhamento","Selecione o produto, use seu link de afiliado, gere a copy e envie para WhatsApp ou Instagram.");
        c.addView(gap(16));
        c.addView(secondary("Continuar publicação",v -> showCreator()));
        content.addView(c);
    }

    private void showSources() {
        startScreen("Fontes oficiais","Use integrações oficiais para automatizar a busca. Segredos de API não ficam gravados dentro do APK.",4);

        LinearLayout shopee = card("SHOPEE","Shopee Afiliados Open API","Existe um portal oficial de Open API para afiliados. A próxima etapa é autorizar sua conta e chamar a API por um backend seguro.");
        shopee.addView(gap(14));
        shopee.addView(primary("Abrir portal Shopee Afiliados",v -> openUrl("https://affiliate.shopee.com.br/open_api/document?type=overview")));
        content.addView(shopee);

        LinearLayout tt = card("TIKTOK SHOP","TikTok Shop Partner Center","O TikTok Shop oferece APIs oficiais para produtos, analytics, bestsellers e afiliados mediante autorização.");
        tt.addView(gap(14));
        tt.addView(primary("Abrir TikTok Shop Partner",v -> openUrl("https://partner.tiktokshop.com/")));
        content.addView(tt);

        content.addView(card("SEGURANÇA","Credenciais protegidas","App Secret, tokens e chaves privadas deverão ficar no backend. O Android receberá somente dados e sessões autorizadas."));
    }

    private void showAddProduct() {
        startScreen("Adicionar produto real","Cadastre os dados visíveis do produto. O agente calcula e salva o score localmente.",1);

        EditText source = input("Fonte: Shopee ou TikTok",false);
        EditText name = input("Nome do produto",false);
        EditText url = input("Link do produto / afiliado",false);
        EditText price = input("Preço (ex.: 89.90)",true);
        EditText commission = input("Comissão % (ex.: 12)",true);
        EditText sales = input("Número de vendas",true);
        EditText trend = input("Tendência de 0 a 100",true);

        content.addView(source);
        content.addView(name);
        content.addView(url);
        content.addView(price);
        content.addView(commission);
        content.addView(sales);
        content.addView(trend);

        content.addView(primary("Salvar e analisar",v -> {
            try {
                String s = source.getText().toString().trim();
                String n = name.getText().toString().trim();
                String u = url.getText().toString().trim();
                if (s.isEmpty() || n.isEmpty() || u.isEmpty()) throw new Exception();
                Product p = new Product(
                        s,n,u,
                        Double.parseDouble(price.getText().toString().replace(",",".")),
                        Double.parseDouble(commission.getText().toString().replace(",",".")),
                        Integer.parseInt(sales.getText().toString().trim()),
                        Integer.parseInt(trend.getText().toString().trim())
                );
                saveProduct(p);
                selectedProduct = p;
                Toast.makeText(this,"Produto salvo. Score: "+p.score,Toast.LENGTH_LONG).show();
                showAgent();
            } catch (Exception e) {
                Toast.makeText(this,"Preencha todos os campos corretamente",Toast.LENGTH_SHORT).show();
            }
        }));
    }

    private List<Product> loadProducts() {
        List<Product> result = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(prefs.getString("products","[]"));
            for (int i=0;i<arr.length();i++) {
                JSONObject o = arr.getJSONObject(i);
                result.add(new Product(
                        o.optString("source"),
                        o.optString("name"),
                        o.optString("url"),
                        o.optDouble("price"),
                        o.optDouble("commission"),
                        o.optInt("sales"),
                        o.optInt("trend")
                ));
            }
        } catch (Exception ignored) {}
        Collections.sort(result,new Comparator<Product>() {
            @Override public int compare(Product a, Product b) { return Integer.compare(b.score,a.score); }
        });
        return result;
    }

    private void saveProduct(Product p) {
        try {
            JSONArray arr = new JSONArray(prefs.getString("products","[]"));
            JSONObject o = new JSONObject();
            o.put("source",p.source);
            o.put("name",p.name);
            o.put("url",p.url);
            o.put("price",p.price);
            o.put("commission",p.commissionPct);
            o.put("sales",p.sales);
            o.put("trend",p.trend);
            arr.put(o);
            prefs.edit().putString("products",arr.toString()).apply();
        } catch (Exception ignored) {}
    }

    private void showAgent() {
        startScreen("Achados","Produtos cadastrados são ordenados pelo score de vendas, comissão e tendência.",1);

        List<Product> products = loadProducts();
        if (products.isEmpty()) {
            LinearLayout empty = card("SEM PRODUTOS","Adicione um produto real","Cadastre um produto da Shopee ou TikTok para o agente começar a comparar oportunidades.");
            empty.addView(gap(16));
            empty.addView(primary("Adicionar produto",v -> showAddProduct()));
            content.addView(empty);
            return;
        }

        int pos=1;
        for (Product p:products) {
            String price = String.format(Locale.US,"R$ %.2f",p.price).replace(".",",");
            String est = String.format(Locale.US,"R$ %.2f",p.estimatedCommission()).replace(".",",");
            String body = "Fonte: "+p.source+
                    "\nPreço: "+price+
                    "\nVendas: "+p.sales+
                    "\nComissão: "+String.format(Locale.US,"%.1f",p.commissionPct).replace(".",",")+"% • "+est+
                    "\nTendência: "+p.trend+"/100";
            LinearLayout card = card("TOP "+pos+" • SCORE "+p.score+"/100",p.name,body);
            card.addView(gap(10));
            card.addView(label(p.score>=75?"Potencial alto":"Potencial moderado",13,p.score>=75?GREEN:WARN,true));
            card.addView(gap(14));
            card.addView(primary("Usar este produto",v -> {
                selectedProduct=p;
                generatedCopy="";
                showCreator();
            }));
            content.addView(card);
            pos++;
        }

        content.addView(secondary("+ Adicionar outro produto",v -> showAddProduct()));
    }

    private void showCreator() {
        startScreen("Criar publicação","Produto → link → texto. Sem complicação.",2);

        if (selectedProduct==null) {
            List<Product> products=loadProducts();
            if (!products.isEmpty()) selectedProduct=products.get(0);
        }

        if (selectedProduct==null) {
            LinearLayout e=card("FALTA PRODUTO","Escolha um achado","Selecione ou adicione um produto antes de gerar a publicação.");
            e.addView(gap(14));
            e.addView(primary("Abrir Achados",v -> showAgent()));
            content.addView(e);
            return;
        }

        String price=String.format(Locale.US,"R$ %.2f",selectedProduct.price).replace(".",",");
        content.addView(card("PRODUTO SELECIONADO • SCORE "+selectedProduct.score,selectedProduct.name,
                "Fonte: "+selectedProduct.source+" • "+price+" • Comissão "+String.format(Locale.US,"%.1f",selectedProduct.commissionPct).replace(".",",")+"%"));

        EditText link=input("Link do afiliado",false);
        link.setText(selectedProduct.url);
        content.addView(label("1. Link",17,TEXT,true));
        content.addView(link);

        EditText copy=input("Texto gerado",false);
        copy.setMinLines(6);
        if (!generatedCopy.isEmpty()) copy.setText(generatedCopy);
        content.addView(gap(12));
        content.addView(label("2. Texto",17,TEXT,true));
        content.addView(copy);

        content.addView(primary("Gerar texto",v -> {
            String finalLink=link.getText().toString().trim();
            generatedCopy="🔥 ACHADO DO DIA!\n\n"+
                    selectedProduct.name+" por "+price+".\n"+
                    "Produto em destaque com bom potencial de procura.\n\n"+
                    "🛒 Confira aqui: "+finalLink+"\n\n"+
                    "#achados #ofertas #promocao #comprasonline";
            copy.setText(generatedCopy);
            Toast.makeText(this,"Texto pronto",Toast.LENGTH_SHORT).show();
        }));

        content.addView(secondary("Copiar texto + link",v -> {
            String value=copy.getText().toString().trim();
            if (value.isEmpty()) {
                Toast.makeText(this,"Gere o texto primeiro",Toast.LENGTH_SHORT).show();
                return;
            }
            generatedCopy=value;
            ClipboardManager cb=(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
            cb.setPrimaryClip(ClipData.newPlainText("ALTIV",generatedCopy));
            Toast.makeText(this,"Conteúdo copiado",Toast.LENGTH_SHORT).show();
        }));

        content.addView(primary("Continuar para publicar",v -> {
            generatedCopy=copy.getText().toString().trim();
            if (generatedCopy.isEmpty()) {
                Toast.makeText(this,"Gere o texto primeiro",Toast.LENGTH_SHORT).show();
                return;
            }
            showPublish();
        }));
    }

    private void showPublish() {
        startScreen("Publicar","Escolha o canal. O conteúdo continua sob sua aprovação antes do envio.",3);

        if (selectedProduct==null || generatedCopy.trim().isEmpty()) {
            LinearLayout e=card("FALTA CONTEÚDO","Prepare a publicação","Escolha um produto e gere o texto antes de compartilhar.");
            e.addView(gap(14));
            e.addView(primary("Ir para Criar",v -> showCreator()));
            content.addView(e);
            return;
        }

        content.addView(card("PRONTO",selectedProduct.name,generatedCopy));
        content.addView(primary("Enviar para WhatsApp",v -> shareToPackage("com.whatsapp","WhatsApp")));
        content.addView(primary("Enviar para Instagram",v -> shareToPackage("com.instagram.android","Instagram")));
        content.addView(secondary("Mais opções",v -> shareGeneral()));
        content.addView(card("AUTOMAÇÃO","Publicação automática","Para publicar sem abrir os aplicativos, vamos conectar as APIs oficiais e as permissões das contas. Esta versão mantém aprovação manual para evitar posts indevidos."));
    }

    private void openUrl(String url) {
        try { startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url))); }
        catch (Exception e) { Toast.makeText(this,"Não foi possível abrir o navegador",Toast.LENGTH_SHORT).show(); }
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

    @Override public void onBackPressed() { showHome(); }
}
