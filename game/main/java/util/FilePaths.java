package util;

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

public class FilePaths {

    public static final Path Player = pathFromClassLoad("/player.psdf");
    public static final Path PlayerArrow = pathFromClassLoad("/playerarrow.psdf");
    public static final InputStream PublicGameKey = getStreamResource("/clientts.pkcs12");
    public static final InputStream PrivateGameKey = getStreamResource("/serverks.pkcs12");
    public static final InputStream NotoSansRegular = getStreamResource("/Noto_Sans/NotoSans-Regular.ttf");
    public static final InputStream NotoSansBold = getStreamResource("/Noto_Sans/NotoSans-Bold.ttf");
    public static final InputStream NotoSansBoldItalic = getStreamResource("/Noto_Sans/NotoSans-BoldItalic.ttf");
    public static final InputStream NotoSansItalic = getStreamResource("/Noto_Sans/NotoSans-Italic.ttf");

    private static InputStream getStreamResource(String resourcePath) {
        return Objects.requireNonNull(
                FilePaths.class.getResourceAsStream(resourcePath),
                "Couldn't find resource " + resourcePath
        );
    }

    private static Path pathFromClassLoad(String resourcePath) {
        try {
            URI resource = Objects.requireNonNull(FilePaths.class.getResource(resourcePath), "Couldn't find resource " + resourcePath).toURI();
            checkFileSystem(resource);
            return Paths.get(resource);
        } catch (URISyntaxException | IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static void checkFileSystem(URI resource) throws IOException {
        if (!"jar".equalsIgnoreCase(resource.getScheme())) {
            return;
        }

        for (FileSystemProvider provider : FileSystemProvider.installedProviders()) {
            if (!provider.getScheme().equalsIgnoreCase("jar")) {
                continue;
            }

            try {
                provider.getFileSystem(resource);
            } catch (FileSystemNotFoundException e) {
                // the jar file system doesn't exist yet...
                // in this case we need to initialize it first:
                provider.newFileSystem(resource, Collections.emptyMap());
            }
        }
    }
}
