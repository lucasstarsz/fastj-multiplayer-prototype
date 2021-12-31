package util;

import tech.fastj.engine.FastJEngine;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;

import game.ClientMain;

public class Fonts {

    public static final Font DefaultNotoSans = Fonts.notoSans(Font.BOLD, 16);
    public static final Font ButtonTextFont = Fonts.notoSans(Font.PLAIN, 24);
    public static final Font TitleTextFont = Fonts.notoSans(Font.BOLD, 64);

    public static Font notoSans(int style, int size) {
        return new Font("Noto Sans", style, size);
    }

    public static Font notoSansMono(int style, int size) {
        return new Font("Noto Sans Mono", style, size);
    }

    static {
        try {
            Fonts.load(Font.TRUETYPE_FONT, FilePaths.NotoSansRegular);
            Fonts.load(Font.TRUETYPE_FONT, FilePaths.NotoSansBold);
            Fonts.load(Font.TRUETYPE_FONT, FilePaths.NotoSansBoldItalic);
            Fonts.load(Font.TRUETYPE_FONT, FilePaths.NotoSansItalic);
            Fonts.load(Font.TRUETYPE_FONT, FilePaths.NotoSansMono);
        } catch (FontFormatException | IOException exception) {
            ClientMain.displayException("Couldn't load font files", exception);
            FastJEngine.closeGame();
        }
    }

    private static void load(int fontType, InputStream fontFile) throws IOException, FontFormatException {
        Font font = Font.createFont(fontType, fontFile);
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
    }
}
