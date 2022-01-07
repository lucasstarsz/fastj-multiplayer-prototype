package util;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Objects;

import core.util.FilePathUtil;

public class FilePaths {
    public static final Path Player = FilePathUtil.pathFromClassLoad(FilePaths.class, "/player.psdf", "jar");
    public static final Path PlayerArrow = FilePathUtil.pathFromClassLoad(FilePaths.class, "/playerarrow.psdf", "jar");
    public static final Path Snowball = FilePathUtil.pathFromClassLoad(FilePaths.class, "/snowball.psdf", "jar");
    public static final Path HealthBar = FilePathUtil.pathFromClassLoad(FilePaths.class, "/healthbar.psdf", "jar");
    public static final Path DanceHype = Path.of("music/Dance_Hype.wav");
    public static final String PublicGameKey = "/clientts.pkcs12";

    public static final InputStream NotoSansRegular = streamResourceFromFolder("/notosans/NotoSans-Regular.ttf");
    public static final InputStream NotoSansBold = streamResourceFromFolder("/notosans/NotoSans-Bold.ttf");
    public static final InputStream NotoSansBoldItalic = streamResourceFromFolder("/notosans/NotoSans-BoldItalic.ttf");
    public static final InputStream NotoSansItalic = streamResourceFromFolder("/notosans/NotoSans-Italic.ttf");
    public static final InputStream NotoSansMono = streamResourceFromFolder("/notosansmono/NotoSansMono-VariableFont_wdth,wght.ttf");
    public static final InputStream GameIcon = streamResourceFromFolder("/fastj_icon.png");

    public static InputStream streamResourceFromFolder(String resourcePath) {
        return Objects.requireNonNull(
                FilePaths.class.getResourceAsStream(resourcePath),
                "Couldn't find resource " + resourcePath
        );
    }
}
