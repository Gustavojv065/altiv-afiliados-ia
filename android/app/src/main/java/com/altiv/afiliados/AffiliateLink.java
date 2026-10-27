package com.altiv.afiliados;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Extrai URLs oficiais Shopee; nao confirma se sao links de afiliado. */
public final class AffiliateLink {
    private static final Pattern URLS =
            Pattern.compile("https?://[^\\s<>\"']+", Pattern.CASE_INSENSITIVE);

    private AffiliateLink() {}

    public static String extract(String text) {
        if (text == null) return null;
        Matcher m = URLS.matcher(text);
        while (m.find()) {
            String value = m.group();
            while (value.endsWith(".") || value.endsWith(",") ||
                    value.endsWith(")") || value.endsWith("]") ||
                    value.endsWith("}") || value.endsWith("!")) {
                value = value.substring(0, value.length() - 1);
            }
            if (isShopee(value)) return value;
        }
        return null;
    }

    public static boolean isShopee(String input) {
        if (input == null) return false;
        try {
            URI url = new URI(input);
            if (!"https".equalsIgnoreCase(url.getScheme())
                    || url.getRawUserInfo() != null || url.getHost() == null) return false;
            String host = url.getHost().toLowerCase(Locale.ROOT);
            return matches(host, "shopee.com.br")
                    || matches(host, "shopee.com") || matches(host, "shope.ee");
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private static boolean matches(String host, String domain) {
        return host.equals(domain) || host.endsWith("." + domain);
    }
}
