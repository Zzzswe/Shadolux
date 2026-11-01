package shadolux.auth;

import shadolux.Config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class TokenStore {
    private final Map<String, String> data = new LinkedHashMap<>();

    public void put(String k, String v) {
        data.put(k, v);
    }

    public String get(String k) {
        return data.get(k);
    }

    public boolean has(String k) {
        return data.containsKey(k) && data.get(k) != null;
    }

    public void save() throws Exception {
        Files.createDirectories(Config.DATA_DIR);
        StringBuilder b = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> e : data.entrySet()) {
            if (!first) b.append(",");
            first = false;
            b.append(quote(e.getKey())).append(":").append(quote(e.getValue()));
        }
        b.append("}");
        String enc = Crypto.encrypt(b.toString(), Crypto.machineSecret());
        Files.writeString(Config.TOKEN_FILE, enc);
    }

    public static TokenStore load() throws Exception {
        TokenStore ts = new TokenStore();
        Path p = Config.TOKEN_FILE;
        if (!Files.exists(p)) return ts;
        String enc = Files.readString(p);
        String json = Crypto.decrypt(enc, Crypto.machineSecret());
        Object parsed = shadolux.Json.parse(json);
        if (parsed instanceof Map) {
            for (Map.Entry<?, ?> e : ((Map<?, ?>) parsed).entrySet()) {
                if (e.getValue() != null) ts.put(String.valueOf(e.getKey()), String.valueOf(e.getValue()));
            }
        }
        return ts;
    }

    private static String quote(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
