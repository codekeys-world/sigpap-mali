package ml.apes.rh.security;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Analyse simplifiée de l'en-tête User-Agent : navigateur, système d'exploitation, type d'appareil. */
public final class UserAgentParser {

    private UserAgentParser() {}

    public record Info(String navigateur, String systeme, String appareil) {}

    private static String v(String ua, String regex) {
        Matcher m = Pattern.compile(regex).matcher(ua);
        return m.find() ? " " + m.group(1).split("\\.")[0] : "";
    }

    public static Info parse(String ua) {
        if (ua == null || ua.isBlank()) return new Info("Inconnu", "Inconnu", "Inconnu");
        String nav;
        if (ua.contains("Edg/")) nav = "Edge" + v(ua, "Edg/([\\d.]+)");
        else if (ua.contains("OPR/")) nav = "Opera" + v(ua, "OPR/([\\d.]+)");
        else if (ua.contains("Firefox/")) nav = "Firefox" + v(ua, "Firefox/([\\d.]+)");
        else if (ua.contains("Chrome/") || ua.contains("CriOS/")) nav =
            "Chrome" + v(ua, "(?:Chrome|CriOS)/([\\d.]+)");
        else if (ua.contains("Safari/")) nav = "Safari" + v(ua, "Version/([\\d.]+)");
        else if (ua.contains("MSIE") || ua.contains("Trident/")) nav = "Internet Explorer";
        else if (ua.startsWith("PostmanRuntime")) nav = "Postman";
        else if (ua.toLowerCase().contains("curl")) nav = "curl";
        else nav = "Autre";

        String os;
        if (ua.contains("Windows NT 10.0")) os = "Windows 10/11";
        else if (ua.contains("Windows NT 6.3")) os = "Windows 8.1";
        else if (ua.contains("Windows")) os = "Windows";
        else if (ua.contains("Android")) os = "Android" + v(ua, "Android ([\\d.]+)");
        else if (ua.contains("iPhone") || ua.contains("iPad") || ua.contains("iPod")) os = "iOS";
        else if (ua.contains("Mac OS X")) os = "macOS";
        else if (ua.contains("CrOS")) os = "ChromeOS";
        else if (ua.contains("Linux")) os = "Linux";
        else os = "Autre";

        String appareil;
        if (ua.contains("iPad") || (ua.contains("Android") && !ua.contains("Mobile"))) appareil =
            "Tablette";
        else if (ua.contains("Mobi") || ua.contains("iPhone")) appareil = "Mobile";
        else appareil = "Ordinateur";
        return new Info(nav, os, appareil);
    }
}
