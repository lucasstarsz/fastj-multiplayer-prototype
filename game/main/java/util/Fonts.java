package util;

import java.awt.Font;

public class Fonts {

    public static final Font DefaultNotoSans = Fonts.notoSans(Font.BOLD, 16);

    public static Font notoSans(int style, int size) {
        return new Font("Noto Sans", style, size);
    }
}
