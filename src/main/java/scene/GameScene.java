package scene;

import tech.fastj.graphics.display.FastJCanvas;
import tech.fastj.graphics.game.Model2D;

import tech.fastj.resources.models.ModelUtil;
import tech.fastj.systems.control.Scene;

import util.FilePaths;
import util.SceneNames;

public class GameScene extends Scene {

    private Model2D player;

    public GameScene() {
        super(SceneNames.GameScene);
    }

    @Override
    public void load(FastJCanvas canvas) {
        player = Model2D.fromPolygons(ModelUtil.loadModel(FilePaths.Player));
        drawableManager.addGameObject(player);
    }

    @Override
    public void unload(FastJCanvas canvas) {
    }

    @Override
    public void update(FastJCanvas canvas) {
    }
}
