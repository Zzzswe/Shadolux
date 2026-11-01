package shadolux.auth;

import shadolux.Config;
import shadolux.Json;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MicrosoftAuth {
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();

    public static Map<String, String> beginDeviceCode() throws Exception {
        String body = "client_id=" + Config.CLIENT_ID + "&scope=" + Config.SCOPE.replace(" ", "%20");
        String resp = post("https://login.microsoftonline.com/" + Config.TENANT + "/oauth2/v2.0/devicecode", body);
        Map<?, ?> j = (Map<?, ?>) Json.parse(resp);
        Map<String, String> m = new LinkedHashMap<>();
        m.put("user_code", String.valueOf(j.get("user_code")));
        m.put("verification_url", String.valueOf(j.get("verification_uri")));
        m.put("message", String.valueOf(j.get("message")));
        m.put("device_code", String.valueOf(j.get("device_code")));
        Object iv = j.get("interval");
        m.put("interval", iv != null ? String.valueOf(iv) : "5");
        return m;
    }

    public static Map<String, String> pollToken(String deviceCode) throws Exception {
        String body = "grant_type=urn:ietf:params:oauth:grant-type:device_code" + "&client_id=" + Config.CLIENT_ID + "&device_code=" + deviceCode;
        String resp = post("https://login.microsoftonline.com/" + Config.TENANT + "/oauth2/v2.0/token", body);
        return parseTokens(resp);
    }

    public static Map<String, String> refresh(String refreshToken) throws Exception {
        String body = "grant_type=refresh_token" + "&client_id=" + Config.CLIENT_ID + "&refresh_token=" + refreshToken;
        String resp = post("https://login.microsoftonline.com/" + Config.TENANT + "/oauth2/v2.0/token", body);
        return parseTokens(resp);
    }

    private static Map<String, String> parseTokens(String resp) {
        Map<?, ?> j = (Map<?, ?>) Json.parse(resp);
        Map<String, String> m = new LinkedHashMap<>();
        if (j.get("access_token") != null) m.put("access_token", String.valueOf(j.get("access_token")));
        if (j.get("refresh_token") != null) m.put("refresh_token", String.valueOf(j.get("refresh_token")));
        return m;
    }

    public static String xboxLiveToken(String msToken) throws Exception {
        String payload = "{\"Properties\":{\"AuthMethod\":\"RPS\",\"SiteName\":\"user.auth.xboxlive.com\",\"RpsTicket\":\"" + msToken + "\"},\"RelyingParty\":\"http://auth.xboxlive.com\",\"TokenType\":\"JWT\"}";
        String resp = postJson("https://user.auth.xboxlive.com/user/authenticate", payload);
        Map<?, ?> j = (Map<?, ?>) Json.parse(resp);
        return String.valueOf(j.get("Token"));
    }

    public static Map<String, String> xstsToken(String xblToken) throws Exception {
        String payload = "{\"Properties\":{\"SandboxId\":\"RETAIL\",\"UserTokens\":[\"" + xblToken + "\"]},\"RelyingParty\":\"rp://api.minecraftservices.com/\",\"TokenType\":\"JWT\"}";
        String resp = postJson("https://xsts.auth.xboxlive.com/xsts/authorize", payload);
        Map<?, ?> j = (Map<?, ?>) Json.parse(resp);
        String token = String.valueOf(j.get("Token"));
        Object xui = ((Map<?, ?>) j.get("DisplayClaims")).get("xui");
        String uhs = String.valueOf(((Map<?, ?>) ((List<?>) xui).get(0)).get("uhs"));
        Map<String, String> m = new LinkedHashMap<>();
        m.put("token", token);
        m.put("uhs", uhs);
        return m;
    }

    public static String minecraftToken(String xsts, String uhs) throws Exception {
        String payload = "{\"identityToken\":\"XBL3.0 x=" + uhs + ";" + xsts + "\"}";
        String resp = postJson("https://api.minecraftservices.com/authentication/login_with_xbox", payload);
        Map<?, ?> j = (Map<?, ?>) Json.parse(resp);
        return String.valueOf(j.get("access_token"));
    }

    public static Map<String, String> minecraftProfile(String mcToken) throws Exception {
        String resp = get("https://api.minecraftservices.com/minecraft/profile", mcToken);
        Map<?, ?> j = (Map<?, ?>) Json.parse(resp);
        Map<String, String> m = new LinkedHashMap<>();
        m.put("name", String.valueOf(j.get("name")));
        m.put("uuid", String.valueOf(j.get("id")));
        return m;
    }

    public static boolean ownsGame(String mcToken) throws Exception {
        String requestId = java.util.UUID.randomUUID().toString();
        String resp = get("https://api.minecraftservices.com/ownership/v2/entitlements?requestId=" + requestId, mcToken);
        Map<?, ?> j = (Map<?, ?>) Json.parse(resp);
        Object items = j.get("items");
        if (items instanceof List) return !((List<?>) items).isEmpty();
        return false;
    }

    private static String post(String url, String body) throws Exception {
        HttpRequest r = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("User-Agent", Config.USER_AGENT)
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> res = HTTP.send(r, HttpResponse.BodyHandlers.ofString());
        return res.body();
    }

    private static String postJson(String url, String body) throws Exception {
        HttpRequest r = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/json")
                .header("User-Agent", Config.USER_AGENT)
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> res = HTTP.send(r, HttpResponse.BodyHandlers.ofString());
        return res.body();
    }

    private static String get(String url, String bearer) throws Exception {
        HttpRequest r = HttpRequest.newBuilder(URI.create(url))
                .header("Authorization", "Bearer " + bearer)
                .header("User-Agent", Config.USER_AGENT)
                .GET().build();
        HttpResponse<String> res = HTTP.send(r, HttpResponse.BodyHandlers.ofString());
        return res.body();
    }
}
