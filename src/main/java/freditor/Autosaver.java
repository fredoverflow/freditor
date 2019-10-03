package freditor;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
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

        directory = System.getProperty("user.home") + File.separator + application + File.separator;
        filename = "!" + application + EXTENSION;
        pathname = directory + filename;
    }

    public void loadOrDefault(String program) {
        try {
            freditor.loadFromFile(pathname);
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
            return directory + encoded + EXTENSION;
        } catch (NoSuchAlgorithmException sha1unsupported) {
            throw new RuntimeException(sha1unsupported);
        }
    }
}
