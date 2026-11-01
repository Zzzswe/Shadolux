package shadolux.launcher;

import shadolux.Config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class Launcher {
    public static void launch(String versionId, String javaPath) throws IOException {
        Path jar = Config.VERSIONS_DIR.resolve(versionId + ".jar");
        if (!Files.exists(jar)) throw new IllegalStateException("version jar missing: " + jar);
        List<String> cmd = new ArrayList<>();
        cmd.add(javaPath);
        cmd.add("-jar");
        cmd.add(jar.toString());
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(Config.DATA_DIR.toFile());
        pb.inheritIO();
        pb.start();
    }
}
