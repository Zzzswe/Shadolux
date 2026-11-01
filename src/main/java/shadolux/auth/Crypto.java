package shadolux.auth;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class Crypto {
    private static final String ALGO = "AES/GCM/NoPadding";
    private static final int TAG = 128;
    private static final int IV = 12;

    public static String encrypt(String plain, String passphrase) throws Exception {
        byte[] iv = new byte[IV];
        new SecureRandom().nextBytes(iv);
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.ENCRYPT_MODE, key(passphrase), new GCMParameterSpec(TAG, iv));
        byte[] enc = c.doFinal(plain.getBytes(StandardCharsets.UTF_8));
        byte[] out = new byte[iv.length + enc.length];
        System.arraycopy(iv, 0, out, 0, iv.length);
        System.arraycopy(enc, 0, out, iv.length, enc.length);
        return Base64.getEncoder().encodeToString(out);
    }

    public static String decrypt(String b64, String passphrase) throws Exception {
        byte[] out = Base64.getDecoder().decode(b64);
        byte[] iv = new byte[IV];
        System.arraycopy(out, 0, iv, 0, IV);
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.DECRYPT_MODE, key(passphrase), new GCMParameterSpec(TAG, iv));
        byte[] dec = c.doFinal(out, IV, out.length - IV);
        return new String(dec, StandardCharsets.UTF_8);
    }

    private static javax.crypto.SecretKey key(String passphrase) throws Exception {
        MessageDigest d = MessageDigest.getInstance("SHA-256");
        byte[] h = d.digest(passphrase.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(h, "AES");
    }

    public static String machineSecret() throws Exception {
        StringBuilder b = new StringBuilder();
        b.append(System.getProperty("os.name"));
        b.append(System.getProperty("user.name"));
        b.append(System.getProperty("os.arch"));
        try {
            java.net.NetworkInterface.getNetworkInterfaces().asIterator().forEachRemaining(ni -> {
                try {
                    if (ni.getHardwareAddress() != null) b.append(new String(ni.getHardwareAddress()));
                } catch (Exception ignored) {
                }
            });
        } catch (Exception ignored) {
        }
        MessageDigest d = MessageDigest.getInstance("SHA-256");
        return Base64.getEncoder().encodeToString(d.digest(b.toString().getBytes(StandardCharsets.UTF_8)));
    }
}
