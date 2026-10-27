package com.altiv.afiliados;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser local de mensagens compartilhadas pelo usuário no aplicativo Shopee.
 * Não acessa a sessão Shopee, não verifica comissão e não cria link afiliado.
 */
public final class ShopeeShareParser {
    private static final Pattern URLS = Pattern.compile("https?://[^\\s<>\"']+", Pattern.CASE_INSENSITIVE);
    private static final Pattern PRICE = Pattern.compile("(?i)R\\$\\s*([0-9][0-9.,]*)");
    private static final Pattern BOILERPLATE = Pattern.compile(
            "(?iu)^(?:d[eê]\\s+uma\\s+olhada\\s+em|olha\\s+s[oó]|confira|veja|encontrei|achei)\\s*[:!\\-]?\\s*");
    private static final Pattern END_OF_TITLE = Pattern.compile(
            "(?iu)\\s+(?:em\\s+oferta|por\\s+R\\$|a\\s+partir\\s+de\\s+R\\$|compre\\s+na\\s+shopee|na\\s+shopee\\s+por)");
    private static final Pattern HASHTAG = Pattern.compile("(?i)(?:^|\\s)#publi\\b");
    private static final Pattern LINK_LABEL = Pattern.compile("(?im)^\\s*(?:link|veja\\s+o\\s+produto|confira\\s+as\\s+condi[cç][oõ]es)\\s*:\\s*$");

    public static final class Offer {
        public final String link;
        public final String title;
        public final String price;
        public final String description;
        Offer(String link, String title, String price, String description) {
            this.link = link;
            this.title = title;
            this.price = price;
            this.description = description;
        }
    }

    private ShopeeShareParser() {}

    public static Offer parse(String raw) {
        String link = AffiliateLink.extract(raw);
        if (link == null) return null;
        String description = removeShopeeLinks(raw);
        description = description.replaceAll("[\\t ]+", " ")
                .replaceAll("(?m)^[\\t ]+|[\\t ]+$", "")
                .replaceAll("\\n{3,}", "\n\n").trim();
        description = LINK_LABEL.matcher(description).replaceAll("")
                .replaceAll("\\n{3,}", "\n\n").trim();
        description = HASHTAG.matcher(description).replaceAll("").trim();
        Matcher priceMatch = PRICE.matcher(description);
        String price = priceMatch.find() ? priceMatch.group(1).replaceAll("[.,]+$", "") : "";
        String title = description.replace('\n', ' ').replaceAll("\\s+", " ").trim();
        title = BOILERPLATE.matcher(title).replaceFirst("");
        Matcher end = END_OF_TITLE.matcher(title);
        if (end.find()) title = title.substring(0, end.start()).trim();
        title = title.replaceAll("(?iu)[.!\\s]+$", "").trim();
        if (title.length() > 120) title = title.substring(0, 120).trim();
        if (title.isEmpty()) title = "Produto Shopee";
        return new Offer(link, title, price, description);
    }

    /** Elimina todas as URLs Shopee presentes no texto sem mexer em links de outros serviços. */
    public static String removeShopeeLinks(String raw) {
        if (raw == null) return "";
        Matcher matcher = URLS.matcher(raw);
        StringBuffer out = new StringBuffer();
        while (matcher.find()) {
            String candidate = matcher.group().replaceAll("[.,!);\\]}]+$", "");
            if (AffiliateLink.isShopee(candidate)) matcher.appendReplacement(out, "");
            else matcher.appendReplacement(out, Matcher.quoteReplacement(matcher.group()));
        }
        matcher.appendTail(out);
        return out.toString();
    }

    /**
     * Constrói mensagem final mantendo apenas o link de afiliado escolhido,
     * mesmo quando a mensagem recebida já continha links repetidos.
     */
    public static String compose(Offer offer, String editedMessage) {
        if (offer == null) return "";
        String value = editedMessage == null ? "" : removeShopeeLinks(editedMessage);
        value = LINK_LABEL.matcher(value).replaceAll("");
        value = HASHTAG.matcher(value).replaceAll("");
        value = value.replaceAll("\\n{3,}", "\n\n").trim();
        if (value.isEmpty()) {
            value = offer.description.isEmpty()
                    ? "🛍️ Confira este produto na Shopee!"
                    : offer.description;
        }
        value = value.replaceAll("\\n{3,}", "\n\n").trim();
        return value + "\n\n🔗 " + offer.link + "\n\n#publi";
    }
}
