import com.altiv.afiliados.AffiliateLink;

public class AffiliateLinkTest {
    private static int count = 0;
    private static void check(String expected, String text) {
        String actual = AffiliateLink.extract(text);
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError("Expected " + expected + " but got " + actual);
        }
        count++;
    }
    public static void main(String[] args) {
        check("https://s.shopee.com.br/abc?x=1", "Oferta: https://s.shopee.com.br/abc?x=1.");
        check("https://shope.ee/xyz", "https://shope.ee/xyz");
        check("https://shopee.com.br/p", "https://shopee.com.br/p");
        check(null, "http://shopee.com.br/not-secure");
        check(null, "https://shopee.com.br.bad.test/fake");
        check(null, "https://user@shopee.com.br/unsafe");
        check(null, "https://example.com/shopee.com.br");
        check(null, "Sem link aqui!");
        check("https://shopee.com.br/a", "https://bad.test/x https://shopee.com.br/a)");
        System.out.println("OK " + count + " AffiliateLink tests");
    }
}
