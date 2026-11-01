package shadolux;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Json {
    public static Object parse(String s) {
        return new Parser(s).parseValue();
    }

    private static final class Parser {
        private final String s;
        private int i;

        Parser(String s) {
            this.s = s;
        }

        Object parseValue() {
            skipWs();
            char c = s.charAt(i);
            if (c == '{') return parseObject();
            if (c == '[') return parseArray();
            if (c == '"') return parseString();
            if (c == 't' || c == 'f') return parseBool();
            if (c == 'n') { i += 4; return null; }
            return parseNumber();
        }

        Map<String, Object> parseObject() {
            Map<String, Object> m = new LinkedHashMap<>();
            i++;
            skipWs();
            if (s.charAt(i) == '}') { i++; return m; }
            while (true) {
                skipWs();
                String key = parseString();
                skipWs();
                i++;
                skipWs();
                Object v = parseValue();
                m.put(key, v);
                skipWs();
                char c = s.charAt(i);
                if (c == ',') { i++; continue; }
                if (c == '}') { i++; break; }
            }
            return m;
        }

        List<Object> parseArray() {
            List<Object> l = new ArrayList<>();
            i++;
            skipWs();
            if (s.charAt(i) == ']') { i++; return l; }
            while (true) {
                Object v = parseValue();
                l.add(v);
                skipWs();
                char c = s.charAt(i);
                if (c == ',') { i++; continue; }
                if (c == ']') { i++; break; }
            }
            return l;
        }

        String parseString() {
            StringBuilder b = new StringBuilder();
            i++;
            while (true) {
                char c = s.charAt(i);
                if (c == '"') { i++; break; }
                if (c == '\\') {
                    i++;
                    char e = s.charAt(i);
                    if (e == '"') b.append('"');
                    else if (e == '\\') b.append('\\');
                    else if (e == '/') b.append('/');
                    else if (e == 'n') b.append('\n');
                    else if (e == 't') b.append('\t');
                    else if (e == 'r') b.append('\r');
                    else if (e == 'b') b.append('\b');
                    else if (e == 'f') b.append('\f');
                    else if (e == 'u') { i++; b.append((char) Integer.parseInt(s.substring(i, i + 4), 16)); i += 3; }
                    else b.append(e);
                } else b.append(c);
                i++;
            }
            return b.toString();
        }

        Object parseNumber() {
            int start = i;
            while (i < s.length() && "0123456789+-.eE".indexOf(s.charAt(i)) >= 0) i++;
            return Double.parseDouble(s.substring(start, i));
        }

        Boolean parseBool() {
            if (s.charAt(i) == 't') { i += 4; return Boolean.TRUE; }
            i += 5;
            return Boolean.FALSE;
        }

        void skipWs() {
            while (i < s.length() && " \t\n\r".indexOf(s.charAt(i)) >= 0) i++;
        }
    }
}
