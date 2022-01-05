package util;

import tech.fastj.graphics.Drawable;

public class Drawables {
    public static <T extends Drawable> T centered(T drawable) {
        drawable.translate(drawable.getCenter().subtract(drawable.getTranslation()).multiply(-1f));
        return drawable;
    }
}
