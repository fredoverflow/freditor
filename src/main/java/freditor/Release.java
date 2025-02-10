package freditor;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.Collections;

public class Release {
    public static String compilationDate(Class<?> clazz) {
        if (OperatingSystem.isBrowser) {
            // prevent java.nio.file.ProviderNotFoundException: Provider "jar" not found
            return "browser";
        }
        try {
            String name = clazz.getName().replace('.', '/').concat(".class");
            URL url = clazz.getClassLoader().getResource(name);
            if (url == null) return name;

            URI uri = url.toURI();
            if ("jar".equals(uri.getScheme())) {
                try (FileSystem ignored = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
                    return lastModifiedDate(uri);
                }
            } else {
                return lastModifiedDate(uri);
            }
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    private static String lastModifiedDate(URI uri) throws IOException {
        Path path = Paths.get(uri);
        String iso8601 = Files.getLastModifiedTime(path).toString();
        return iso8601.substring(0, iso8601.indexOf('T'));
    }
}
