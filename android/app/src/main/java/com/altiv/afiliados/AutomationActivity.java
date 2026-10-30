package com.altiv.afiliados;
import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.Uri;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/** Radar nativo: chaves Shopee/Telegram ficam somente no servidor. */
public class AutomationActivity extends Activity {
    private static final int BG=0xff060c15, CARD=0xff111e2d, BLUE=0xff217aff, TXT=0xfff3f8ff, SUB=0xffb1c2d5;
    private SharedPreferences pref;
    private EditText url, key, keyword, discount;
    private LinearLayout list;
    private TextView status;

    private int dp(int v){return (int)(getResources().getDisplayMetrics().density*v+.5f);}
    private GradientDrawable surface(int color){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(16));return d;}
    private TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setLineSpacing(dp(2),1.06f);return t;}
    private LinearLayout card(String title,String subtitle){
        LinearLayout c=new LinearLayout(this);c.setOrientation(1);c.setPadding(dp(16),dp(16),dp(16),dp(16));c.setBackground(surface(CARD));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=dp(14);c.setLayoutParams(p);
        c.addView(text(title,20,TXT));c.addView(text(subtitle,13,SUB));return c;
    }
    private EditText field(String hint,String value){
        EditText e=new EditText(this);e.setText(value);e.setHint(hint);e.setSingleLine(true);e.setTextColor(TXT);e.setHintTextColor(SUB);
        e.setTextSize(14);e.setPadding(dp(12),dp(10),dp(12),dp(10));e.setBackground(surface(0xff1d3046));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=dp(10);e.setLayoutParams(p);return e;
    }
    private Button button(String title,View.OnClickListener click){
        Button b=new Button(this);b.setAllCaps(false);b.setText(title);b.setTextColor(TXT);b.setTextSize(14);b.setBackground(surface(BLUE));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(50));p.topMargin=dp(10);b.setLayoutParams(p);b.setOnClickListener(click);return b;
    }
    private void say(String msg){Toast.makeText(this,msg,Toast.LENGTH_LONG).show();}
    @Override public void onCreate(Bundle b){
        super.onCreate(b);pref=getSharedPreferences("altiv_automation_v11",MODE_PRIVATE);
        getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);
        ScrollView scroll=new ScrollView(this);scroll.setBackgroundColor(BG);scroll.setFillViewport(true);
        LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(18),dp(30),dp(18),dp(30));
        scroll.addView(root);root.addView(text("ALTIV • AFILIADOS IA",13,0xff77b4ff));
        root.addView(text("Radar de ofertas",32,TXT));
        root.addView(text("Produtos, comissões e links oficiais da sua conta Shopee.",15,SUB));
        LinearLayout setup=card("01  Conectar servidor","Informe a URL HTTPS do ALTIV publicado e o token privado. Não coloque App Secret da Shopee no celular.");
        url=field("https://seu-altiv.vercel.app",pref.getString("url",""));
        key=field("Token do painel",pref.getString("key",""));
        key.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
        setup.addView(url);setup.addView(key);
        setup.addView(button("Salvar e verificar",v->saveSettings()));
        status=text("Servidor ainda não verificado",14,SUB);setup.addView(status);root.addView(setup);
        LinearLayout search=card("02  Buscar ofertas","Selecione um produto verdadeiro retornado pela API oficial.");
        keyword=field("Produto ou categoria","fone bluetooth");discount=field("Desconto mínimo (%)","20");
        discount.setInputType(InputType.TYPE_CLASS_NUMBER);
        search.addView(keyword);search.addView(discount);search.addView(button("Pesquisar produtos",v->search()));root.addView(search);
        LinearLayout products=card("03  Resultados","Confira cada oferta antes de publicar no Telegram.");
        list=new LinearLayout(this);list.setOrientation(1);
        list.addView(text("Use Pesquisar produtos para carregar a lista.",14,SUB));
        products.addView(list);root.addView(products);
        LinearLayout schedule=card("04  Automação de publicações",
            "O agendamento no GitHub Actions pode publicar no Telegram com o APK fechado, após configuração.");
        schedule.addView(text("9h • 13h • 18h (horário de Mato Grosso). Não automatiza grupos do WhatsApp.",14,SUB));
        schedule.addView(button("Abrir automação no GitHub",v->startActivity(new Intent(Intent.ACTION_VIEW,
            Uri.parse("https://github.com/Gustavojv065/altiv-afiliados-ia/actions")))));
        root.addView(schedule);root.addView(button("Voltar ao modo manual",v->finish()));
        setContentView(scroll);
        scroll.setOnApplyWindowInsetsListener((v,insets)->{
            int top=0,bottom=0;
            if(Build.VERSION.SDK_INT>=30){android.graphics.Insets s=insets.getInsets(WindowInsets.Type.systemBars());top=s.top;bottom=s.bottom;}
            else{top=insets.getSystemWindowInsetTop();bottom=insets.getSystemWindowInsetBottom();}
            root.setPadding(dp(18),top+dp(18),dp(18),bottom+dp(25));return insets;
        });scroll.requestApplyInsets();
    }
    private void saveSettings(){
        String target=url.getText().toString().trim().replaceAll("/+$",""),token=key.getText().toString().trim();
        try{URL u=new URL(target);
            if(!"https".equals(u.getProtocol())||u.getUserInfo()!=null||u.getPort()!=-1||token.length()<16)throw new Exception();
        }catch(Exception e){say("Informe URL HTTPS e token de pelo menos 16 caracteres.");return;}
        pref.edit().putString("url",target).putString("key",token).apply();
        status.setText("Verificando...");
        async(()->request("GET","/api/automation/status",null),r->status.setText(
            "Conectado ✓ • Shopee: "+(r.optBoolean("shopeeReady")?"OK":"pendente")
            +" • Telegram: "+(r.optBoolean("telegramReady")?"OK":"pendente")));
    }
    private interface Job{JSONObject run()throws Exception;}
    private interface Result{void accept(JSONObject value);}
    private void async(Job job,Result onSuccess){
        new Thread(()->{try{JSONObject value=job.run();runOnUiThread(()->onSuccess.accept(value));}
            catch(Exception e){runOnUiThread(()->say(e.getMessage()==null?"Erro de conexão":e.getMessage()));}}).start();
    }
    private JSONObject request(String method,String path,JSONObject body)throws Exception{
        String base=pref.getString("url",""),token=pref.getString("key","");
        if(base.isEmpty()||token.isEmpty())throw new Exception("Configure o servidor primeiro.");
        HttpURLConnection c=(HttpURLConnection)new URL(base+path).openConnection();
        c.setRequestMethod(method);c.setConnectTimeout(12000);c.setReadTimeout(15000);
        c.setRequestProperty("Authorization","Bearer "+token);c.setRequestProperty("Accept","application/json");
        if(body!=null){c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json");
            try(OutputStream out=c.getOutputStream()){out.write(body.toString().getBytes(StandardCharsets.UTF_8));}}
        try{int http=c.getResponseCode();InputStream input=http>=400?c.getErrorStream():c.getInputStream();
            if(input==null)throw new Exception("HTTP "+http);
            StringBuilder s=new StringBuilder();
            try(BufferedReader r=new BufferedReader(new InputStreamReader(input,StandardCharsets.UTF_8))){
                String line;while((line=r.readLine())!=null){s.append(line);if(s.length()>300000)throw new Exception("Resposta muito grande");}}
            JSONObject result=new JSONObject(s.toString());
            if(http>=400)throw new Exception(result.optString("error","Erro HTTP "+http));return result;
        }finally{c.disconnect();}
    }
    private void search(){
        String name=keyword.getText().toString().trim(),pct=discount.getText().toString().trim();int min;
        try{min=pct.isEmpty()?0:Integer.parseInt(pct);if(min<0||min>100)throw new Exception();}catch(Exception e){say("Desconto de 0 a 100%.");return;}
        if(name.isEmpty()){say("Informe o produto.");return;}
        list.removeAllViews();list.addView(text("Buscando ofertas...",14,SUB));
        try{String path="/api/automation/offers?keyword="+URLEncoder.encode(name,"UTF-8")+"&minDiscount="+min;
            async(()->request("GET",path,null),r->display(r.optJSONArray("offers")));
        }catch(Exception e){say("Não foi possível pesquisar.");}
    }
    private void display(JSONArray arr){
        list.removeAllViews();
        if(arr==null||arr.length()==0){list.addView(text("Nenhuma oferta encontrada. Tente outra busca.",14,SUB));return;}
        list.addView(text(arr.length()+" ofertas encontradas",15,0xff71e2b2));
        for(int i=0;i<arr.length();i++){
            JSONObject product=arr.optJSONObject(i);if(product==null)continue;
            LinearLayout item=card(product.optString("title"),product.optString("shopName")+" • "+product.optDouble("discount")+"% OFF");
            item.addView(text(String.format(new Locale("pt","BR"),"R$ %.2f",product.optDouble("price")),18,0xff71e2b2));
            item.addView(text(String.format(new Locale("pt","BR"),"Comissão estimada: %.1f%%", 
                product.optDouble("commissionRate",0) * 100.0),13,SUB));
            item.addView(button("Ver prévia e publicar",v->preview(product)));list.addView(item);
        }
    }
    private void preview(JSONObject p){
        String message=p.optString("title")+"\n"+String.format(new Locale("pt","BR"),"R$ %.2f",p.optDouble("price"))
            +"\n"+p.optString("offerLink")+"\n\n#publi • Link de afiliado";
        new AlertDialog.Builder(this).setTitle("Confirmar publicação").setMessage(message)
            .setNegativeButton("Cancelar",null).setPositiveButton("Publicar Telegram",(d,w)->publish(p)).show();
    }
    private void publish(JSONObject p){
        JSONObject data=new JSONObject();
        try{data.put("title",p.optString("title"));data.put("price",p.optDouble("price"));
            data.put("discount",p.optDouble("discount"));data.put("offerLink",p.optString("offerLink"));}
        catch(Exception e){say("Produto inválido");return;}
        async(()->request("POST","/api/automation/publish",data),
            result->say(result.optBoolean("published")?"Oferta enviada ao Telegram!":"Publicação não confirmada."));
    }
}
