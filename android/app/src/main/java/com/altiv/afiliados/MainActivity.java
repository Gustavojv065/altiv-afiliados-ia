package com.altiv.afiliados;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private final int BG = Color.rgb(5, 8, 13);
    private final int SURFACE = Color.rgb(12, 18, 27);
    private final int SURFACE_2 = Color.rgb(17, 25, 36);
    private final int BLUE = Color.rgb(20, 115, 255);
    private final int BLUE_SOFT = Color.rgb(119, 173, 255);
    private final int GREEN = Color.rgb(73, 213, 148);
    private final int TEXT = Color.rgb(245, 248, 255);
    private final int MUTED = Color.rgb(158, 172, 192);

    private LinearLayout content;
    private Product selectedProduct;
    private String generatedCopy = "";
    private String preparedLink = "";

    private static class Product {
        String name;
        double price;
        double commissionPct;
        int sales;
        int trend;
        int score;

        Product(String name, double price, double commissionPct, int sales, int trend) {
            this.name = name;
            this.price = price;
            this.commissionPct = commissionPct;
            this.sales = sales;
            this.trend = trend;
            this.score = score();
        }

        int score() {
            double salesScore = Math.min(100.0, sales / 150.0);
            double commissionScore = Math.min(100.0, commissionPct * 5.0);
            double trendScore = Math.min(100.0, trend);
            return (int) Math.round(salesScore * 0.45 + commissionScore * 0.35 + trendScore * 0.20);
        }

        double estimatedCommission() {
            return price * (commissionPct / 100.0);
        }
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        t.setLineSpacing(0, 1.08f);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private View gap(int h) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(1, h));
        return v;
    }

    private Button primary(String title, View.OnClickListener click) {
        Button b = new Button(this);
        b.setText(title);
        b.setTextColor(Color.WHITE);
        b.setTextSize(15);
        b.setAllCaps(false);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setBackground(bg(BLUE, 28, Color.TRANSPARENT));
        b.setOnClickListener(click);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 112);
        lp.setMargins(0, 8, 0, 8);
        b.setLayoutParams(lp);
        return b;
    }

    private Button secondary(String title, View.OnClickListener click) {
        Button b = primary(title, click);
        b.setBackground(bg(SURFACE_2, 28, Color.rgb(38, 54, 76)));
        return b;
    }

    private EditText input(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setHintTextColor(Color.rgb(105, 122, 146));
        e.setTextColor(TEXT);
        e.setTextSize(15);
        e.setSingleLine(false);
        e.setPadding(24, 18, 24, 18);
        e.setBackground(bg(SURFACE_2, 24, Color.rgb(38, 54, 76)));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 8, 0, 10);
        e.setLayoutParams(lp);
        return e;
    }

    private LinearLayout card(String eyebrow, String title, String body) {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(26, 22, 26, 22);
        c.setBackground(bg(SURFACE, 28, Color.rgb(24, 38, 56)));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 10, 0, 10);
        c.setLayoutParams(lp);

        if (eyebrow != null && !eyebrow.isEmpty()) {
            TextView e = label(eyebrow, 12, BLUE_SOFT, true);
            e.setLetterSpacing(.08f);
            c.addView(e);
            c.addView(gap(8));
        }
        c.addView(label(title, 20, TEXT, true));
        c.addView(gap(8));
        c.addView(label(body, 15, MUTED, false));
        return c;
    }

    private void startScreen(String title, String subtitle, int activeIndex) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(32, 28, 32, 34);
        scroll.addView(content);

        TextView brand = label("ALTIV • AGENTE DE ACHADOS", 12, BLUE_SOFT, true);
        brand.setLetterSpacing(.10f);
        content.addView(brand);
        content.addView(gap(14));
        content.addView(label(title, 31, TEXT, true));
        content.addView(gap(8));
        content.addView(label(subtitle, 15, MUTED, false));
        content.addView(gap(22));

        root.addView(scroll);
        root.addView(buildNav(activeIndex));
        setContentView(root);
    }

    private LinearLayout buildNav(int active) {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER);
        bar.setPadding(8, 8, 8, 12);
        bar.setBackgroundColor(Color.rgb(7, 11, 17));

        String[] labels = {"Início", "Achados", "Criar", "Publicar"};
        View.OnClickListener[] actions = new View.OnClickListener[]{
                v -> showHome(), v -> showAgent(), v -> showCreator(), v -> showPublish()
        };

        for (int i = 0; i < labels.length; i++) {
            TextView item = label(labels[i], 13, i == active ? BLUE_SOFT : MUTED, i == active);
            item.setGravity(Gravity.CENTER);
            item.setPadding(6, 18, 6, 18);
            item.setOnClickListener(actions[i]);
            item.setBackground(i == active ? bg(Color.rgb(13, 30, 52), 22, Color.TRANSPARENT) : null);
            bar.addView(item, new LinearLayout.LayoutParams(0, 88, 1f));
        }
        return bar;
    }

    private void showHome() {
        startScreen("Venda com um fluxo simples.", "O agente encontra oportunidades. Você escolhe o produto, prepara o link, gera o texto e publica.", 0);

        LinearLayout agent = card("PASSO 1", "Agente de Achados", "Ordena produtos por potencial de vendas, comissão e tendência para destacar oportunidades mais interessantes.");
        agent.addView(gap(16));
        agent.addView(primary("Procurar melhores achados", v -> showAgent()));
        content.addView(agent);

        LinearLayout flow = card("FLUXO", "Achado → Link → Texto → Publicação", "Depois de selecionar um produto, o aplicativo leva você direto para a criação da postagem, sem telas desnecessárias.");
        flow.addView(gap(16));
        flow.addView(secondary("Continuar uma publicação", v -> showCreator()));
        content.addView(flow);

        content.addView(card("BETA 4", "Teste funcional local", "A busca usa dados de demonstração nesta versão. Geração de texto, cópia e compartilhamento já funcionam localmente. A busca real entra quando conectarmos a fonte de produtos."));
    }

    private List<Product> demoProducts() {
        List<Product> products = new ArrayList<>();
        products.add(new Product("Mini projetor portátil", 89.90, 12.0, 12400, 94));
        products.add(new Product("Kit organizador de cozinha", 49.90, 16.0, 8700, 89));
        products.add(new Product("Luminária LED recarregável", 39.90, 18.0, 6500, 91));
        products.add(new Product("Suporte magnético para celular", 24.90, 20.0, 5100, 82));
        Collections.sort(products, new Comparator<Product>() {
            @Override public int compare(Product a, Product b) {
                return Integer.compare(b.score, a.score);
            }
        });
        return products;
    }

    private void showAgent() {
        startScreen("Agente de Achados", "Veja primeiro os produtos com melhor combinação de vendas, comissão e tendência.", 1);

        TextView demo = label("Dados de demonstração • integração real será conectada na próxima fase", 12, Color.rgb(245, 186, 79), true);
        demo.setPadding(0, 0, 0, 10);
        content.addView(demo);

        int position = 1;
        for (Product p : demoProducts()) {
            String money = String.format(Locale.US, "R$ %.2f", p.price).replace(".", ",");
            String commission = String.format(Locale.US, "R$ %.2f", p.estimatedCommission()).replace(".", ",");
            String body = "Preço: " + money +
                    "\nVendas: " + p.sales +
                    "\nComissão: " + (int)p.commissionPct + "% • estimada " + commission +
                    "\nTendência: " + p.trend + "/100";

            LinearLayout product = card("TOP " + position + " • SCORE " + p.score + "/100", p.name, body);
            TextView badge = label("Potencial " + (p.score >= 85 ? "alto" : "bom"), 13, GREEN, true);
            product.addView(gap(10));
            product.addView(badge);
            product.addView(gap(14));
            product.addView(primary("Usar este produto", v -> {
                selectedProduct = p;
                generatedCopy = "";
                preparedLink = "";
                showCreator();
            }));
            content.addView(product);
            position++;
        }
    }

    private void showCreator() {
        startScreen("Criar publicação", "Produto escolhido → link → texto. Tudo na mesma tela.", 2);

        if (selectedProduct == null) {
            LinearLayout empty = card("PASSO 1", "Escolha um produto primeiro", "Abra o Agente de Achados e selecione uma oportunidade para montar a publicação automaticamente.");
            empty.addView(gap(16));
            empty.addView(primary("Abrir Agente de Achados", v -> showAgent()));
            content.addView(empty);
            return;
        }

        String price = String.format(Locale.US, "R$ %.2f", selectedProduct.price).replace(".", ",");
        content.addView(card("PRODUTO SELECIONADO • SCORE " + selectedProduct.score, selectedProduct.name, "Preço de referência: " + price + " • comissão " + (int)selectedProduct.commissionPct + "%"));

        content.addView(label("1. Link do produto / afiliado", 17, TEXT, true));
        content.addView(gap(6));
        EditText linkInput = input("Cole aqui o link de afiliado do produto");
        linkInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        if (!preparedLink.isEmpty()) linkInput.setText(preparedLink);
        content.addView(linkInput);

        content.addView(secondary("Preparar link", v -> {
            String value = linkInput.getText().toString().trim();
            if (value.isEmpty()) {
                Toast.makeText(this, "Cole seu link de afiliado primeiro", Toast.LENGTH_SHORT).show();
                return;
            }
            preparedLink = value;
            Toast.makeText(this, "Link preparado", Toast.LENGTH_SHORT).show();
        }));

        content.addView(gap(18));
        content.addView(label("2. Texto da publicação", 17, TEXT, true));
        content.addView(gap(6));

        final EditText copyBox = input("O texto gerado aparecerá aqui");
        copyBox.setMinLines(6);
        if (!generatedCopy.isEmpty()) copyBox.setText(generatedCopy);
        content.addView(copyBox);

        content.addView(primary("Gerar texto automaticamente", v -> {
            preparedLink = linkInput.getText().toString().trim();
            String link = preparedLink.isEmpty() ? "[cole seu link de afiliado]" : preparedLink;
            generatedCopy = "ACHADO DO DIA!\n\n" +
                    selectedProduct.name + " por " + price + ".\n" +
                    "Uma opção que está chamando atenção e pode valer a pena conferir.\n\n" +
                    "Confira aqui: " + link + "\n\n" +
                    "#achados #ofertas #promocao #comprasonline";
            copyBox.setText(generatedCopy);
            Toast.makeText(this, "Texto pronto", Toast.LENGTH_SHORT).show();
        }));

        content.addView(secondary("Copiar texto + link", v -> {
            String current = copyBox.getText().toString().trim();
            if (current.isEmpty()) {
                Toast.makeText(this, "Gere o texto primeiro", Toast.LENGTH_SHORT).show();
                return;
            }
            generatedCopy = current;
            ClipboardManager cb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cb.setPrimaryClip(ClipData.newPlainText("ALTIV publicação", generatedCopy));
            Toast.makeText(this, "Copiado", Toast.LENGTH_SHORT).show();
        }));

        content.addView(gap(18));
        content.addView(primary("Continuar para publicar", v -> {
            generatedCopy = copyBox.getText().toString().trim();
            preparedLink = linkInput.getText().toString().trim();
            showPublish();
        }));
    }

    private void showPublish() {
        startScreen("Publicar", "Escolha onde enviar. O ALTIV prepara o conteúdo e abre o aplicativo de destino.", 3);

        if (selectedProduct == null || generatedCopy.trim().isEmpty()) {
            LinearLayout empty = card("FALTA CONTEÚDO", "Prepare a publicação primeiro", "Selecione um produto e gere o texto antes de abrir os canais de compartilhamento.");
            empty.addView(gap(16));
            empty.addView(primary("Voltar para Criar", v -> showCreator()));
            content.addView(empty);
            return;
        }

        content.addView(card("PRONTO PARA PUBLICAR", selectedProduct.name, generatedCopy));

        content.addView(primary("Enviar para WhatsApp", v -> shareToPackage("com.whatsapp", "WhatsApp")));
        content.addView(primary("Enviar para Instagram", v -> shareToPackage("com.instagram.android", "Instagram")));
        content.addView(secondary("Mais opções de compartilhamento", v -> shareGeneral()));

        content.addView(gap(12));
        content.addView(card("PUBLICAÇÃO AUTOMÁTICA", "Próxima integração", "Para publicar sem abrir os aplicativos, será necessário conectar as APIs oficiais e autorizar as contas do Instagram/WhatsApp Business."));
    }

    private void shareToPackage(String packageName, String label) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, generatedCopy);
        intent.setPackage(packageName);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, label + " não encontrado. Abrindo opções de compartilhamento.", Toast.LENGTH_SHORT).show();
            shareGeneral();
        }
    }

    private void shareGeneral() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, generatedCopy);
        startActivity(Intent.createChooser(intent, "Publicar com ALTIV"));
    }

    @Override public void onBackPressed() {
        showHome();
    }
}
