package scene;

import tech.fastj.logging.Log;
import tech.fastj.graphics.display.FastJCanvas;
import tech.fastj.graphics.game.Model2D;

import tech.fastj.resources.models.ModelUtil;
import tech.fastj.systems.control.Scene;

import scripts.PlayerController;
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
        PlayerController playerController = new PlayerController(this::updatePlayerInfo, inputManager);
        player.addBehavior(playerController, this);
        drawableManager.addGameObject(player);
    }

    @Override
    public void unload(FastJCanvas canvas) {
        player = null;
    }

    @Override
    public void update(FastJCanvas canvas) {
    }

    public void updatePlayerInfo() {
        Log.info(GameScene.class, "Player Position: {}", player.getCenter());
        Log.info(GameScene.class, "Player Rotation: {}", player.getRotationWithin360());
    }
}
