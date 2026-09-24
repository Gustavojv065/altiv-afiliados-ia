package com.altiv.afiliados;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private final int BG = Color.rgb(5, 8, 13);
    private final int SURFACE = Color.rgb(12, 18, 27);
    private final int SURFACE_2 = Color.rgb(17, 25, 36);
    private final int BLUE = Color.rgb(20, 115, 255);
    private final int BLUE_SOFT = Color.rgb(119, 173, 255);
    private final int TEXT = Color.rgb(245, 248, 255);
    private final int MUTED = Color.rgb(158, 172, 192);
    private LinearLayout content;
    private LinearLayout nav;
    private SharedPreferences prefs;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        prefs = getSharedPreferences("altiv_prefs", MODE_PRIVATE);
        if (!prefs.getBoolean("intro_seen", false)) showIntro();
        else showHome();
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
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 118);
        lp.setMargins(0, 8, 0, 8);
        b.setLayoutParams(lp);
        return b;
    }

    private Button secondary(String title, View.OnClickListener click) {
        Button b = primary(title, click);
        b.setBackground(bg(SURFACE_2, 28, Color.rgb(38, 54, 76)));
        return b;
    }

    private LinearLayout card(String eyebrow, String title, String body, String action, View.OnClickListener click) {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(28, 24, 28, 24);
        c.setBackground(bg(SURFACE, 30, Color.rgb(24, 38, 56)));
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

        if (action != null && click != null) {
            c.addView(gap(18));
            Button b = secondary(action, click);
            b.getLayoutParams().height = 100;
            c.addView(b);
        }
        return c;
    }

    private void startScreen(String section, String title, String subtitle, int activeIndex) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout.LayoutParams scrollLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        scroll.setLayoutParams(scrollLp);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(34, 28, 34, 34);
        scroll.addView(content);

        TextView brand = label("ALTIV • AFILIADOS IA", 12, BLUE_SOFT, true);
        brand.setLetterSpacing(.10f);
        content.addView(brand);
        content.addView(gap(14));
        content.addView(label(title, 32, TEXT, true));
        content.addView(gap(8));
        content.addView(label(subtitle, 15, MUTED, false));
        content.addView(gap(22));

        root.addView(scroll);
        nav = buildNav(activeIndex);
        root.addView(nav);
        setContentView(root);
    }

    private LinearLayout buildNav(int active) {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER);
        bar.setPadding(8, 8, 8, 12);
        bar.setBackgroundColor(Color.rgb(7, 11, 17));
        String[] labels = {"Início", "Radar", "Criar", "Publicar"};
        View.OnClickListener[] actions = new View.OnClickListener[]{
            v -> showHome(), v -> showRadar(), v -> showCreator(), v -> showPublish()
        };
        for (int i = 0; i < labels.length; i++) {
            TextView item = label(labels[i], 13, i == active ? BLUE_SOFT : MUTED, i == active);
            item.setGravity(Gravity.CENTER);
            item.setPadding(8, 18, 8, 18);
            item.setOnClickListener(actions[i]);
            item.setBackground(i == active ? bg(Color.rgb(13, 30, 52), 22, Color.TRANSPARENT) : null);
            bar.addView(item, new LinearLayout.LayoutParams(0, 88, 1f));
        }
        return bar;
    }

    private void showIntro() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(34, 48, 34, 34);
        root.setBackgroundColor(BG);
        root.setGravity(Gravity.CENTER_VERTICAL);

        TextView brand = label("ALTIV • AFILIADOS IA", 12, BLUE_SOFT, true);
        brand.setLetterSpacing(.10f);
        root.addView(brand);
        root.addView(gap(18));
        root.addView(label("Mais simples. Mais rápido. Mais fácil de usar.", 34, TEXT, true));
        root.addView(gap(14));
        root.addView(label("A Beta 3 organiza o fluxo em três passos claros: encontrar produto, criar conteúdo e publicar.", 16, MUTED, false));
        root.addView(gap(26));
        root.addView(card("PASSO 1", "Encontre", "Use o Radar IA para visualizar oportunidades e escolher o que trabalhar.", null, null));
        root.addView(card("PASSO 2", "Crie", "Gere texto, CTA e prepare a mídia sem sair do fluxo.", null, null));
        root.addView(card("PASSO 3", "Publique", "Revise, aprove e acompanhe o que está pronto para postar.", null, null));
        root.addView(gap(16));
        root.addView(primary("Entrar no painel", v -> {
            prefs.edit().putBoolean("intro_seen", true).apply();
            showHome();
        }));
        setContentView(root);
    }

    private void showHome() {
        startScreen("home", "Painel simples para começar.", "Escolha o próximo passo. O aplicativo mostra o que fazer sem menus complicados.", 0);

        content.addView(card("COMECE AQUI", "Encontrar produto", "Abra o Radar IA e veja rapidamente os produtos que podem virar conteúdo.", "Abrir Radar IA", v -> showRadar()));
        content.addView(card("ATALHO", "Criar conteúdo", "Já escolheu um produto? Vá direto para o criador e prepare sua publicação.", "Abrir Criador", v -> showCreator()));

        LinearLayout status = card("STATUS", "Aplicativo pronto para testes", "Beta 3 nativa, com navegação simplificada e sem dependência da Vercel.", null, null);
        content.addView(status);
    }

    private void showRadar() {
        startScreen("radar", "Radar IA", "Escolha uma oportunidade e siga direto para a criação do conteúdo.", 1);

        content.addView(card("DESTAQUE", "Produto em alta", "Exemplo de oportunidade. Na próxima fase entram preço, comissão, origem e tendência real.", "Usar este produto", v -> {
            Toast.makeText(this, "Produto selecionado", Toast.LENGTH_SHORT).show();
            showCreator();
        }));
        content.addView(card("FILTROS RÁPIDOS", "Refine a busca", "Categoria, faixa de preço, comissão e potencial de conteúdo ficarão concentrados aqui.", "Testar filtros", v -> Toast.makeText(this, "Filtros prontos para integração", Toast.LENGTH_SHORT).show()));
    }

    private void showCreator() {
        startScreen("creator", "Criador com IA", "Um fluxo direto para transformar um produto em publicação.", 2);

        content.addView(card("1 • TEXTO", "Copy automática", "Título, descrição, CTA e hashtags organizados em uma única etapa.", "Gerar exemplo", v -> Toast.makeText(this, "Exemplo de copy gerado", Toast.LENGTH_SHORT).show()));
        content.addView(card("2 • MÍDIA", "Imagem ou vídeo", "Área preparada para adicionar a mídia do produto e criar a peça final.", "Adicionar mídia", v -> Toast.makeText(this, "Mídia será integrada na próxima fase", Toast.LENGTH_SHORT).show()));
        content.addView(primary("Continuar para publicação", v -> showPublish()));
    }

    private void showPublish() {
        startScreen("publish", "Publicações", "Veja apenas o que precisa de atenção: revisar, aprovar ou publicar.", 3);

        content.addView(card("AGORA", "Fila de aprovação", "Os conteúdos preparados aparecerão aqui antes de serem publicados.", "Revisar conteúdo", v -> Toast.makeText(this, "Fila pronta para integração", Toast.LENGTH_SHORT).show()));
        content.addView(card("DEPOIS", "Agenda", "Acompanhe publicações programadas e histórico de envios.", "Ver agenda", v -> Toast.makeText(this, "Agenda pronta para integração", Toast.LENGTH_SHORT).show()));
    }

    @Override public void onBackPressed() {
        showHome();
    }
}
