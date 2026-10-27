import com.altiv.afiliados.ShopeeShareParser;
public class ShopeeShareParserTest {
    private static int count = 0;
    private static void expect(boolean result, String msg) {
        if (!result) throw new AssertionError(msg);
        count++;
    }
    private static int occurrences(String text,String needle) {
        int total=0, pos=0;
        while((pos=text.indexOf(needle,pos))>=0){total++;pos+=needle.length();}
        return total;
    }
    public static void main(String[] args) {
        String url="https://s.shopee.com.br/9pfQcCdXOW";
        String copy="Dê uma olhada em Promoção Relâmpago Nativa SPA – Body Splash em Oferta por R$14,90. Compre na Shopee agora! "+url;
        ShopeeShareParser.Offer p=ShopeeShareParser.parse(copy);
        expect(p!=null,"Nao reconheceu link Shopee");
        expect(p.title.contains("Body Splash"),"Nao extraiu titulo: "+p.title);
        expect("14,90".equals(p.price),"Nao extraiu preco: "+p.price);
        expect(!p.description.contains(url),"Nao removeu link do resumo");
        String first=ShopeeShareParser.compose(p,p.description);
        expect(occurrences(first,url)==1,"Link repetido na mensagem inicial");
        expect(first.contains("#publi"),"Publicidade nao identificada");
        String duplicated="Olha essa oferta! "+url+" "+url+"\n#publi\nVeja o produto e confira as condições:\n"+url;
        String fixed=ShopeeShareParser.compose(p,duplicated);
        expect(occurrences(fixed,url)==1,"Links duplicados em texto editado");
        expect(occurrences(fixed,"#publi")==1,"Hashtag repetida");
        expect(ShopeeShareParser.parse("apenas texto") == null,"Aceitou descricao sem URL");
        expect(ShopeeShareParser.parse("https://shopee.com.br.bad.example/produto")==null,"Aceitou dominio falso");
        expect(ShopeeShareParser.parse(url)!=null,"Nao reconheceu apenas URL");
        expect(ShopeeShareParser.compose(ShopeeShareParser.parse(url),"").contains(url),"Faltou URL ao compor");
        System.out.println("OK: "+count+" testes de importacao Shopee");
    }
}
