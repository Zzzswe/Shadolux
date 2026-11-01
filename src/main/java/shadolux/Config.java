package shadolux;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class Config {
    public static final String CLIENT_ID = "9188040d-6c67-4c5b-b112-36a304b66dad";
    public static final String TENANT = "consumers";
    public static final String SCOPE = "XboxLive.signin offline_access";
    public static final String USER_AGENT = "Shadolux/0.1";
    public static final Path DATA_DIR = Paths.get(System.getProperty("user.home"), ".shadolux");
    public static final Path TOKEN_FILE = DATA_DIR.resolve("tokens.dat");
    public static final Path MODS_DIR = DATA_DIR.resolve("mods");
    public static final Path VERSIONS_DIR = DATA_DIR.resolve("versions");

    private Config() {}
}
