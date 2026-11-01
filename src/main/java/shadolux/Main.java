package shadolux;

import shadolux.auth.MicrosoftAuth;
import shadolux.auth.TokenStore;
import shadolux.launcher.Launcher;
import shadolux.launcher.ModManager;
import shadolux.launcher.VersionManager;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public final class Main {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        TokenStore store = TokenStore.load();

        if (!store.has("refresh_token")) {
            Map<String, String> dc = MicrosoftAuth.beginDeviceCode();
            System.out.println(dc.get("message"));
            String deviceCode = dc.get("device_code");
            int interval = Integer.parseInt(dc.getOrDefault("interval", "5"));
            Map<String, String> tokens = null;
            while (true) {
                Thread.sleep(interval * 1000L);
                Map<String, String> t = MicrosoftAuth.pollToken(deviceCode);
                if (t.containsKey("access_token")) {
                    tokens = t;
                    break;
                }
            }
            for (Map.Entry<String, String> e : tokens.entrySet()) store.put(e.getKey(), e.getValue());
            store.save();
        }

        String refresh = store.get("refresh_token");
        Map<String, String> tokens = MicrosoftAuth.refresh(refresh);
        if (tokens.containsKey("refresh_token")) store.put("refresh_token", tokens.get("refresh_token"));
        store.put("access_token", tokens.get("access_token"));
        store.save();

        String msToken = store.get("access_token");
        String xbl = MicrosoftAuth.xboxLiveToken(msToken);
        Map<String, String> xsts = MicrosoftAuth.xstsToken(xbl);
        String mcToken = MicrosoftAuth.minecraftToken(xsts.get("token"), xsts.get("uhs"));

        if (!MicrosoftAuth.ownsGame(mcToken)) {
            System.out.println("This account does not own Minecraft. Launch blocked.");
            return;
        }

        Map<String, String> profile = MicrosoftAuth.minecraftProfile(mcToken);
        System.out.println("Signed in as " + profile.get("name") + " (" + profile.get("uuid") + ")");

        List<String> mods = ModManager.listMods();
        System.out.println("Installed mods: " + mods);

        System.out.println("Enter version to launch (or blank to skip):");
        String ver = sc.nextLine().trim();
        if (!ver.isEmpty()) {
            VersionManager.downloadVersion(ver);
            Launcher.launch(ver, "java");
        }
    }
}
