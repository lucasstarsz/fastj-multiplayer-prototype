package util;

import tech.fastj.engine.FastJEngine;

import game.GameManager;

public class Scenes {
    public static void switchScene(String to, boolean unloadCurrent) {
        FastJEngine.runAfterUpdate(() -> {
            GameManager gameManager = FastJEngine.getLogicManager();
            if (unloadCurrent) {
                gameManager.getCurrentScene().reset();
            }
            gameManager.switchScenes(to);
        });
    }
}
