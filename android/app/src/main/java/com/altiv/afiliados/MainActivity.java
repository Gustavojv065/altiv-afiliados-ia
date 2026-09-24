package com.altiv.afiliados;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private LinearLayout content;
    private final int BG = Color.rgb(5,8,13);
    private final int CARD = Color.rgb(12,18,27);
    private final int BLUE = Color.rgb(20,115,255);
    private final int MUTED = Color.rgb(158,172,192);

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        showHome();
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value); t.setTextSize(size); t.setTextColor(color);
        t.setPadding(0,8,0,8);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private Button button(String label, View.OnClickListener listener) {
        Button b = new Button(this);
        b.setText(label); b.setTextColor(Color.WHITE); b.setTextSize(15);
        b.setAllCaps(false); b.setBackgroundColor(BLUE); b.setOnClickListener(listener);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 120);
        p.setMargins(0,10,0,10); b.setLayoutParams(p);
        return b;
    }

    private LinearLayout card(String title, String body) {
        LinearLayout c = new LinearLayout(this); c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(28,24,28,24); c.setBackgroundColor(CARD);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.setMargins(0,10,0,10); c.setLayoutParams(p);
        c.addView(text(title,20,Color.WHITE,true)); c.addView(text(body,15,MUTED,false));
        return c;
    }

    private void base(String title, String subtitle) {
        ScrollView sc = new ScrollView(this); sc.setBackgroundColor(BG);
        content = new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(38,32,38,42); sc.addView(content);
        content.addView(text("ALTIV • AFILIADOS IA",13,Color.rgb(119,173,255),true));
        content.addView(text(title,34,Color.WHITE,true)); content.addView(text(subtitle,16,MUTED,false));
        setContentView(sc);
    }

    private void showHome() {
        base("Seu copiloto para vender como afiliado.", "Beta 2 nativa — sem depender de Vercel. Escolha um módulo para testar a navegação.");
        content.addView(button("Radar IA", v -> showRadar()));
        content.addView(button("Criador com IA", v -> showCreator()));
        content.addView(button("Publicações", v -> showPublish()));
        content.addView(card("Status do aplicativo", "Base Android funcionando localmente. Próxima etapa: conectar dados reais, APIs e automações."));
    }

    private void addBack() { content.addView(button("← Voltar ao início", v -> showHome())); }

    private void showRadar() {
        base("Radar IA", "Área preparada para descobrir e organizar oportunidades de afiliados.");
        content.addView(card("Produto em alta", "Aqui entrarão produtos, comissão, preço, tendência e origem."));
        content.addView(card("Filtro inteligente", "Categorias, faixa de preço, comissão e potencial de conteúdo."));
        content.addView(button("Testar análise", v -> Toast.makeText(this,"Radar IA funcionando",Toast.LENGTH_SHORT).show())); addBack();
    }

    private void showCreator() {
        base("Criador com IA", "Fluxo para transformar um produto em conteúdo pronto para publicação.");
        content.addView(card("Copy", "Título, descrição, CTA, hashtags e versões para redes sociais."));
        content.addView(card("Mídia", "Espaço preparado para imagem e vídeo do produto."));
        content.addView(button("Gerar exemplo", v -> Toast.makeText(this,"Criador funcionando",Toast.LENGTH_SHORT).show())); addBack();
    }

    private void showPublish() {
        base("Publicações", "Organize aprovação, agendamento e publicação das campanhas.");
        content.addView(card("Fila de aprovação", "Conteúdos pendentes aparecerão aqui antes de publicar."));
        content.addView(card("Agenda", "Publicações programadas e histórico de envios."));
        content.addView(button("Testar módulo", v -> Toast.makeText(this,"Publicações funcionando",Toast.LENGTH_SHORT).show())); addBack();
    }

    @Override public void onBackPressed() { showHome(); }
}
