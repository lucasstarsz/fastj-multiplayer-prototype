package util;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.Objects;

public class FilePaths {
    public static final Path Player = pathFromClassLoad("/player.psdf");

    private static Path pathFromClassLoad(String resourcePath) {
        try {
            URI resource = Objects.requireNonNull(FilePaths.class.getResource(resourcePath), "Couldn't find " + resourcePath).toURI();

            if ("jar".equals(resource.getScheme())) {
                for (FileSystemProvider provider : FileSystemProvider.installedProviders()) {
                    if (provider.getScheme().equalsIgnoreCase("jar")) {
                        try {
                            provider.getFileSystem(resource);
                        } catch (FileSystemNotFoundException e) {
                            // in this case we need to initialize it first:
                            provider.newFileSystem(resource, Collections.emptyMap());
                        }
                    }
                }
            }

            return Paths.get(resource);
        } catch (URISyntaxException | IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
