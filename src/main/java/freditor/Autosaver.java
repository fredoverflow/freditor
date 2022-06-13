package freditor;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Autosaver {
    private static final Pattern APPLICATIONS = Pattern.compile("[a-z]{1,16}");
    private static final String EXTENSION = ".txt";

    private final Freditor freditor;

    public final String directory;
    public final String filename;
    public final String pathname;

    public Autosaver(Freditor freditor, String application) {
        if (!APPLICATIONS.matcher(application).matches()) {
            throw new IllegalArgumentException(application + " does not match " + APPLICATIONS);
        }

        this.freditor = freditor;

        directory = directory(application) + File.separator + application + File.separator;
        filename = application + EXTENSION;
        pathname = directory + filename;
    }

    private static String directory(String application) {
        String classPath = System.getProperty("java.class.path");
        Pattern pattern = Pattern.compile(application + "[ ]++((?:[^%]|%[0-9a-f]{2})+?)(?:%2f|%5c)?+[.]jar", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(classPath);
        if (matcher.matches()) {
            String directory = decodeUrl(matcher.group(1));
            if (new File(directory).isDirectory()) {
                return directory;
            } else {
                System.out.println("invalid directory " + directory);
            }
        }
        return System.getProperty("user.home");
    }

    private static String decodeUrl(String encoded) {
        StringBuilder decoded = new StringBuilder();
        for (int i = 0; i < encoded.length(); ++i) {
            char ch = encoded.charAt(i);
            if (ch == '%') {
                int hi = Character.digit(encoded.charAt(++i), 16);
                int lo = Character.digit(encoded.charAt(++i), 16);
                ch = (char) (hi << 4 | lo);
            }
            decoded.append(ch);
        }
        return decoded.toString();
    }

    public void loadOrDefault(String program) {
        try {
            freditor.loadFromFile(pathname);
            if (freditor.isBlank()) {
                freditor.loadFromString(program);
            }
        } catch (IOException ignored) {
            freditor.loadFromString(program);
        }
    }

    public void save() {
        createDirectory();
        saveAs(pathname);
        saveAs(contentHashPathname());
    }

    private void createDirectory() {
        File dir = new File(directory);
        if (dir.mkdir()) {
            System.out.println("created directory " + dir);
        }
    }

    private void saveAs(String pathname) {
        try {
            System.out.println("saving code as " + pathname);
            freditor.saveToFile(pathname);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private String contentHashPathname() {
        try {
            byte[] text = freditor.toByteArray();
            byte[] hash = MessageDigest.getInstance("SHA-1").digest(text);
            String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
            // File names starting with a minus sign require special care.
            // A base64url-encoded SHA-1 hash never ends with a minus sign,
            // so reversing the encoded string is an easy fix.
            String reversed = new StringBuilder(encoded).reverse().toString();
            return directory + reversed + EXTENSION;
        } catch (NoSuchAlgorithmException sha1unsupported) {
            throw new RuntimeException(sha1unsupported);
        }
    }
}
