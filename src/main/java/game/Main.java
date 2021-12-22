package game;

import tech.fastj.engine.FastJEngine;
import tech.fastj.graphics.display.FastJCanvas;

import tech.fastj.systems.control.SimpleManager;

public class Main extends SimpleManager {

    @Override
    public void init(FastJCanvas canvas) {
    }

    @Override
    public void update(FastJCanvas canvas) {
    }

    public static void main(String[] args) {
        FastJEngine.init("", new Main());
        FastJEngine.run();
    }
}
