package shadolux.launcher;

import shadolux.Config;
import shadolux.Json;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class VersionManager {
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();
    private static final String MANIFEST = "https://launchermeta.mojang.com/mc/game/version_manifest.json";

    public static List<String> listVersions() throws Exception {
        Map<?, ?> j = (Map<?, ?>) Json.parse(get(MANIFEST));
        List<?> versions = (List<?>) j.get("versions");
        List<String> out = new ArrayList<>();
        for (Object o : versions) {
            Map<?, ?> v = (Map<?, ?>) o;
            out.add(String.valueOf(v.get("id")) + " (" + String.valueOf(v.get("type")) + ")");
        }
        return out;
    }

    public static void downloadVersion(String versionId) throws Exception {
        Map<?, ?> m = (Map<?, ?>) Json.parse(get(MANIFEST));
        String url = null;
        for (Object o : (List<?>) m.get("versions")) {
            Map<?, ?> v = (Map<?, ?>) o;
            if (versionId.equals(String.valueOf(v.get("id")))) {
                url = String.valueOf(v.get("url"));
                break;
            }
        }
        if (url == null) throw new IllegalArgumentException("version not found: " + versionId);
        Map<?, ?> vm = (Map<?, ?>) Json.parse(get(url));
        String jarUrl = String.valueOf(((Map<?, ?>) ((Map<?, ?>) vm.get("downloads")).get("client")).get("url"));
        Files.createDirectories(Config.VERSIONS_DIR);
        Path out = Config.VERSIONS_DIR.resolve(versionId + ".jar");
        HttpRequest r = HttpRequest.newBuilder(URI.create(jarUrl)).header("User-Agent", Config.USER_AGENT).GET().build();
        HttpResponse<Path> res = HTTP.send(r, HttpResponse.BodyHandlers.ofFile(out));
        if (res.statusCode() != 200) throw new RuntimeException("download failed: " + res.statusCode());
    }

    private static String get(String url) throws Exception {
        HttpRequest r = HttpRequest.newBuilder(URI.create(url)).header("User-Agent", Config.USER_AGENT).GET().build();
        HttpResponse<String> res = HTTP.send(r, HttpResponse.BodyHandlers.ofString());
        return res.body();
    }
}
