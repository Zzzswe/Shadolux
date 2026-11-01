package shadolux.launcher;

import shadolux.Config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class ModManager {
    public static List<String> listMods() throws IOException {
        Files.createDirectories(Config.MODS_DIR);
        List<String> mods = new ArrayList<>();
        try (Stream<Path> s = Files.list(Config.MODS_DIR)) {
            s.filter(p -> p.toString().endsWith(".jar")).forEach(p -> mods.add(p.getFileName().toString()));
        }
        return mods;
    }

    public static void disable(String name) throws IOException {
        Path src = Config.MODS_DIR.resolve(name);
        Path disabled = Config.MODS_DIR.resolve("disabled");
        Files.createDirectories(disabled);
        Files.move(src, disabled.resolve(name));
    }

    public static void enable(String name) throws IOException {
        Path src = Config.MODS_DIR.resolve("disabled").resolve(name);
        Files.move(src, Config.MODS_DIR.resolve(name));
    }

    public static Path modsDir() {
        return Config.MODS_DIR;
    }
}
