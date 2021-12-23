package game;

import tech.fastj.graphics.display.FastJCanvas;

import tech.fastj.systems.control.SceneManager;

import scene.GameScene;
import util.SceneNames;

public class GameManager extends SceneManager {
    @Override
    public void init(FastJCanvas canvas) {
        addScene(new GameScene());
        setCurrentScene(SceneNames.GameScene);
        loadCurrentScene();
    }
}
