package core.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.Objects;

public class FilePathUtil {

    public static InputStream streamResource(Class<?> classToLoadFrom, String resourcePath) {
        return Objects.requireNonNull(
                classToLoadFrom.getResourceAsStream(resourcePath),
                "Couldn't find resource " + resourcePath
        );
    }

    public static Path pathFromClassLoad(Class<?> classToLoadFrom, String resourcePath, String expectedScheme) {
        try {
            URI resource = Objects.requireNonNull(classToLoadFrom.getResource(resourcePath), "Couldn't find resource " + resourcePath).toURI();
            checkFileSystem(resource, expectedScheme);
            return Paths.get(resource);
        } catch (URISyntaxException | IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static void checkFileSystem(URI resource, String expectedScheme) throws IOException {
        if (!expectedScheme.equalsIgnoreCase(resource.getScheme())) {
            return;
        }

        for (FileSystemProvider provider : FileSystemProvider.installedProviders()) {
            if (!provider.getScheme().equalsIgnoreCase(expectedScheme)) {
                continue;
            }

            try {
                provider.getFileSystem(resource);
            } catch (FileSystemNotFoundException e) {
                // the file system doesn't exist yet...
                // in this case we need to initialize it first:
                provider.newFileSystem(resource, Collections.emptyMap());
            }
        }
    }
}
