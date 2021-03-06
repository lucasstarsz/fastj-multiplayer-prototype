package scenes;

import tech.fastj.engine.FastJEngine;
import tech.fastj.logging.Log;
import tech.fastj.math.Pointf;
import tech.fastj.math.Transform2D;
import tech.fastj.graphics.dialog.DialogConfig;
import tech.fastj.graphics.display.FastJCanvas;
import tech.fastj.graphics.game.Polygon2D;
import tech.fastj.graphics.util.DrawUtil;

import tech.fastj.input.keyboard.Keys;
import tech.fastj.resources.models.ModelUtil;
import tech.fastj.systems.control.Scene;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import core.util.Networking;
import game.ClientMain;
import game.GameManager;
import network.client.Client;
import objects.Player;
import objects.Snowball;
import scripts.PlayerController;
import scripts.SnowballController;
import ui.HealthBar;
import ui.PercentageBox;
import ui.StatusBox;
import util.Dialogs;
import util.FilePaths;
import util.Fonts;
import util.SceneNames;
import util.Scenes;
import util.Tags;

import static scripts.PlayerController.MovementSpeed;
import static scripts.PlayerController.RotationSpeed;

public class GameScene extends Scene implements FocusListener {

    private Player player;
    private PlayerController playerController;
    private SnowballController snowballController;
    private HealthBar temperatureBar, hitDamageBar;
    private int localPlayerNumber = -1;
    private Pointf canvasCenter;
    private boolean isDead;

    private final Map<Integer, Player> otherPlayers = new HashMap<>();
    private final Map<Integer, Transform2D> otherPlayerTransforms = new HashMap<>();
    private ScheduledExecutorService transformSync;

    private PercentageBox<Integer> temperaturePercentageBox, hitDamagePercentageBox, snowballPercentageBox;
    private StatusBox snowballThrowStatusBox, snowballMakeStatusBox;

    public GameScene() {
        super(SceneNames.GameScene);
    }

    public void setLocalPlayerNumber(int localPlayerNumber) {
        this.localPlayerNumber = localPlayerNumber;
    }

    @Override
    public void load(FastJCanvas canvas) {
        isDead = false;
        canvas.getRawCanvas().addFocusListener(this);
        canvasCenter = canvas.getCanvasCenter();

        Client client = FastJEngine.<GameManager>getLogicManager().getClient();
        player = new Player(ModelUtil.loadModel(FilePaths.Player), localPlayerNumber, true);
        player.addTag(Tags.LocalPlayer, this);
        player.translate(canvasCenter);

        playerController = new PlayerController(this::updatePlayerInfo, inputManager, client, localPlayerNumber, this);
        player.addBehavior(playerController, this);
        snowballController = new SnowballController(this, client, this::updateSnowballInfo);
        player.addBehavior(snowballController, this);
        drawableManager.addGameObject(player);

        temperatureBar = new HealthBar(this, Color.red, 200);
        drawableManager.addUIElement(temperatureBar);
        temperaturePercentageBox = new PercentageBox<>(this, temperatureBar.getHealthRemaining(), temperatureBar.getMaxHealth(), "Temp (C): ");
        temperaturePercentageBox.getStatDisplay().setFont(Fonts.MonoStatTextFont);
        temperaturePercentageBox.translate(Pointf.unit().multiply(15f, 5f));
        drawableManager.addUIElement(temperaturePercentageBox);
        Log.info("{}", drawableManager.getUIElements().keySet());

        hitDamageBar = new HealthBar(this, Color.red.darker().darker(), 200);
        hitDamageBar.translate(Pointf.down().multiply(45f));
        drawableManager.addUIElement(hitDamageBar);
        hitDamagePercentageBox = new PercentageBox<>(this, hitDamageBar.getHealthRemaining(), hitDamageBar.getMaxHealth(), "Health: ");
        hitDamagePercentageBox.getStatDisplay().setFont(Fonts.MonoStatTextFont);
        hitDamagePercentageBox.translate(Pointf.unit().multiply(15f, 50f));
        drawableManager.addUIElement(hitDamagePercentageBox);
        Log.info("{}", drawableManager.getUIElements().keySet());

        snowballPercentageBox = new PercentageBox<>(this, snowballController.getSnowballCount(), SnowballController.MaxSnowballsCarried, "Snowballs: ");
        snowballPercentageBox.getStatDisplay().setFont(Fonts.StatTextFont);
        snowballPercentageBox.translate(Pointf.down().multiply(90f));
        drawableManager.addUIElement(snowballPercentageBox);

        snowballMakeStatusBox = new StatusBox(this, "Make Snowball", false);
        snowballMakeStatusBox.getStatDisplay().setFont(Fonts.SmallStatTextFont);
        snowballMakeStatusBox.translate(Pointf.down().multiply(115f));
        drawableManager.addUIElement(snowballMakeStatusBox);

        snowballThrowStatusBox = new StatusBox(this, "Throw Snowball", false);
        snowballThrowStatusBox.getStatDisplay().setFont(Fonts.SmallStatTextFont);
        snowballThrowStatusBox.translate(Pointf.down().multiply(140f));
        drawableManager.addUIElement(snowballThrowStatusBox);

        transformSync = Executors.newScheduledThreadPool(1);
        transformSync.scheduleWithFixedDelay(this::sendTransformSync, 1, 1, TimeUnit.SECONDS);
    }

    private synchronized void sendTransformSync() {
        Client client = FastJEngine.<GameManager>getLogicManager().getClient();
        try {
            client.send(
                    Networking.Server.SyncTransform,
                    localPlayerNumber,
                    player.getTranslation().x,
                    player.getTranslation().y,
                    player.getRotation()
            );
        } catch (IOException exception) {
            if (client.isConnectionClosed()) {
                Dialogs.message(DialogConfig.create().withTitle("Server Closed")
                        .withParentComponent(FastJEngine.getDisplay().getWindow())
                        .withPrompt("Server connection closed. Session ended.")
                        .build()
                );
            } else {
                ClientMain.displayException("Couldn't sync player transform, disconnecting client", exception);
                Scenes.switchScene(SceneNames.MainMenu, true);
            }
        }
    }

    public void syncOtherPlayer(int otherPlayerNumber, float translationX, float translationY, float rotation) {
        Player otherPlayer = otherPlayers.get(otherPlayerNumber);
        otherPlayer.setTranslation(new Pointf(translationX, translationY));
        otherPlayer.setRotation(rotation);
    }

    @Override
    public void unload(FastJCanvas canvas) {
        player = null;
        playerController = null;
        snowballController = null;
        snowballPercentageBox = null;
        temperaturePercentageBox = null;
        hitDamagePercentageBox = null;
        temperatureBar = null;
        hitDamageBar = null;
        localPlayerNumber = -1;
        otherPlayers.clear();
        otherPlayerTransforms.clear();

        if (transformSync != null) {
            transformSync.shutdownNow();
        }
        transformSync = null;
        canvas.getRawCanvas().removeFocusListener(this);
    }

    @Override
    public void update(FastJCanvas canvas) {
        for (Map.Entry<Integer, Transform2D> otherPlayerTransform : otherPlayerTransforms.entrySet()) {
            Player player = otherPlayers.get(otherPlayerTransform.getKey());
            if (player == null) {
                continue;
            }

            Pointf movement = otherPlayerTransform.getValue().getTranslation();
            float rotation = otherPlayerTransform.getValue().getRotation();
            PlayerController.transformPlayer(movement, rotation, player);
        }
    }

    public void updatePlayerInfo() {
    }

    private void updateSnowballInfo() {
        Log.info("{}", snowballController.getSnowballCount());
        snowballPercentageBox.setCurrentValue(snowballController.getSnowballCount());
        snowballThrowStatusBox.setCurrentStatus(snowballController.isSnowballThrowReady());
        snowballMakeStatusBox.setCurrentStatus(snowballController.isSnowballMakeReady());
    }

    public void addNewPlayer(int newPlayerNumber) {
        if (otherPlayers.containsKey(newPlayerNumber)) {
            Log.warn(GameScene.class, "Already contains player {}", newPlayerNumber);
            return;
        }

        Polygon2D[] playerModel = ModelUtil.loadModel(FilePaths.Player);
        playerModel[0].setFill(DrawUtil.randomColor());

        Player newPlayer = new Player(playerModel, newPlayerNumber, false);
        newPlayer.addTag(Tags.Enemy, this);
        newPlayer.translate(canvasCenter);
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
            default -> throw new IllegalArgumentException("Invalid key: " + key);
        }

        Transform2D otherPlayerTransform = otherPlayerTransforms.get(otherPlayerNumber);
        otherPlayerTransform.rotate(rotation, player.getCenter());
        otherPlayerTransform.translate(movement);
    }

    public void removePlayer(int removedPlayerNumber) {
        Player removedPlayer = otherPlayers.remove(removedPlayerNumber);
        drawableManager.removeGameObject(removedPlayer.getID());
        otherPlayerTransforms.remove(removedPlayerNumber);
    }

    @Override
    public void focusGained(FocusEvent e) {
    }

    @Override
    public void focusLost(FocusEvent focusEvent) {
        Log.info(PlayerController.class, "Focus lost. Releasing all keys.");
        playerController.resetMovement();
    }

    public int getLocalPlayerNumber() {
        return localPlayerNumber;
    }

    public boolean isPlayerDead() {
        return isDead;
    }

    public void spawnSnowball(int otherPlayerNumber, Pointf trajectory, float rotation) {
        Player otherPlayer = otherPlayers.get(otherPlayerNumber);
        if (otherPlayer == null) {
            Log.info("Didn't find player {}", otherPlayerNumber);
            return;
        }

        spawnSnowball(otherPlayer, trajectory, rotation);
    }

    public void spawnSnowball(Player player, Pointf trajectory, float rotation) {
        Snowball snowball = new Snowball(trajectory, rotation, player, this);
        snowball.addBehavior(snowball, this);
        this.addBehaviorListener(snowball);
        snowball.init(player);
        drawableManager.addGameObject(snowball);
    }

    public void playerTakeSnowballDamage(int otherPlayerNumber) {
        if (isDead) {
            return;
        }

        try {
            if (temperatureBar.modifyHealthRemaining(-Snowball.SnowballTempDamage)) {
                Log.info("Player {} died to temp damage.", localPlayerNumber);
                Client client = FastJEngine.<GameManager>getLogicManager().getClient();
                client.send(Networking.Server.TemperatureDeath, localPlayerNumber, otherPlayerNumber);
            } else if (hitDamageBar.modifyHealthRemaining(-Snowball.SnowballHitDamage)) {
                Log.info("Player {} died to hit damage.", localPlayerNumber);
                Client client = FastJEngine.<GameManager>getLogicManager().getClient();
                client.send(Networking.Server.HitDamageDeath, localPlayerNumber, otherPlayerNumber);
            }
        } catch (IOException exception) {
            ClientMain.displayException("Couldn't sync player death, disconnecting client", exception);
            Scenes.switchScene(SceneNames.MainMenu, true);
        }
    }

    public void playerTakeTempDamage() {
        if (isDead || otherPlayers.values().size() < 1) {
            return;
        }

        try {
            if (!temperatureBar.modifyHealthRemaining(-PlayerController.TempDamage)) {
                return;
            }

            Log.info("Player {} died to residual temp damage.", localPlayerNumber);
            Client client = FastJEngine.<GameManager>getLogicManager().getClient();
            client.send(Networking.Server.TemperatureDeath, localPlayerNumber, Integer.MIN_VALUE);
        } catch (IOException exception) {
            ClientMain.displayException("Couldn't sync player death, disconnecting client", exception);
            Scenes.switchScene(SceneNames.MainMenu, true);
        }
    }
}
