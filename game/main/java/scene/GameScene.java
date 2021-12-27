package scene;

import tech.fastj.engine.FastJEngine;
import tech.fastj.logging.Log;
import tech.fastj.math.Pointf;
import tech.fastj.math.Transform2D;
import tech.fastj.graphics.display.FastJCanvas;
import tech.fastj.graphics.game.Model2D;
import tech.fastj.graphics.game.Polygon2D;
import tech.fastj.graphics.util.DrawUtil;

import tech.fastj.input.keyboard.Keys;
import tech.fastj.resources.models.ModelUtil;
import tech.fastj.systems.control.Scene;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import game.GameManager;
import network.client.Client;
import scripts.PlayerController;
import util.FilePaths;
import util.Networking;
import util.SceneNames;

import static scripts.PlayerController.MovementSpeed;
import static scripts.PlayerController.RotationSpeed;

public class GameScene extends Scene {

    private Model2D player;
    private int playerNumber;

    private final Map<Integer, Model2D> otherPlayers = new HashMap<>();
    private final Map<Integer, Transform2D> otherPlayerTransforms = new HashMap<>();
    private ScheduledExecutorService transformSync;

    public GameScene(int playerNumber) {
        super(SceneNames.GameScene);
        this.playerNumber = playerNumber;
    }

    @Override
    public void load(FastJCanvas canvas) {
        Client client = FastJEngine.<GameManager>getLogicManager().getClient();
        player = Model2D.fromPolygons(ModelUtil.loadModel(FilePaths.Player));
        PlayerController playerController = new PlayerController(this::updatePlayerInfo, inputManager, client, playerNumber);
        player.addBehavior(playerController, this);
        drawableManager.addGameObject(player);
        transformSync = Executors.newSingleThreadScheduledExecutor();
        transformSync.scheduleWithFixedDelay(this::sendTransformSync, 1, 1, TimeUnit.SECONDS);
    }

    private synchronized void sendTransformSync() {
        Client client = FastJEngine.<GameManager>getLogicManager().getClient();
        try {
            client.send(
                    Networking.Server.SyncTransform,
                    playerNumber,
                    player.getTranslation().x,
                    player.getTranslation().y,
                    player.getRotation()
            );
        } catch (IOException exception) {
            client.shutdown();
            Log.error(GameScene.class, "couldn't sync transform, disconnecting client", exception);
        }
    }

    public void syncOtherPlayer(int otherPlayerNumber, float translationX, float translationY, float rotation) {
        Model2D otherPlayer = otherPlayers.get(otherPlayerNumber);
        otherPlayer.setTranslation(new Pointf(translationX, translationY));
        otherPlayer.setRotation(rotation);
    }

    @Override
    public void unload(FastJCanvas canvas) {
        player = null;
        playerNumber = 0;
        otherPlayers.clear();

        if (transformSync != null) {
            transformSync.shutdownNow();
        }
        transformSync = null;
    }

    @Override
    public void update(FastJCanvas canvas) {
        for (Map.Entry<Integer, Transform2D> otherPlayerTransform : otherPlayerTransforms.entrySet()) {
            Model2D player = otherPlayers.get(otherPlayerTransform.getKey());
            if (player == null) {
                continue;
            }

            Pointf movement = otherPlayerTransform.getValue().getTranslation();
            float rotation = otherPlayerTransform.getValue().getRotation();
            PlayerController.transformPlayer(movement, rotation, player);
        }
    }

    public void updatePlayerInfo() {
//        Log.info(GameScene.class, "Player Position: {}", player.getCenter());
//        Log.info(GameScene.class, "Player Rotation: {}", player.getRotationWithin360());
    }

    public void addNewPlayer(int newPlayerNumber) {
        if (otherPlayers.containsKey(newPlayerNumber)) {
            Log.warn("Already contains player {}", newPlayerNumber);
            return;
        }

        Polygon2D[] playerModel = ModelUtil.loadModel(FilePaths.Player);
        playerModel[0].setFill(DrawUtil.randomColor());

        Model2D newPlayer = Model2D.fromPolygons(playerModel);
        drawableManager.addGameObject(newPlayer);
        otherPlayers.put(newPlayerNumber, newPlayer);
        otherPlayerTransforms.put(newPlayerNumber, new Transform2D());
    }

    public synchronized void transformOtherPlayer(int otherPlayerNumber, Keys key, boolean isPressed) {
        if (!otherPlayers.containsKey(otherPlayerNumber)) {
            Log.info("Didn't find player {}", otherPlayerNumber);
            return;
        }

        Pointf movement = Pointf.origin();
        float rotation = 0;

        switch (key) {
            case W -> movement.y -= (isPressed) ? MovementSpeed : -MovementSpeed;
            case A -> rotation -= (isPressed) ? RotationSpeed : -RotationSpeed;
            case S -> movement.y += (isPressed) ? MovementSpeed : -MovementSpeed;
            case D -> rotation += (isPressed) ? RotationSpeed : -RotationSpeed;
        }

        Transform2D otherPlayerTransform = otherPlayerTransforms.get(otherPlayerNumber);
        otherPlayerTransform.rotate(rotation, player.getCenter());
        otherPlayerTransform.translate(movement);
    }

    public void removePlayer(int removedPlayerNumber) {
        Model2D removedPlayer = otherPlayers.remove(removedPlayerNumber);
        drawableManager.removeGameObject(removedPlayer.getID());
        otherPlayerTransforms.remove(removedPlayerNumber);
    }
}
