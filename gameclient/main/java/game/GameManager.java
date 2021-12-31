package game;

import tech.fastj.engine.FastJEngine;
import tech.fastj.logging.Log;
import tech.fastj.math.Pointf;
import tech.fastj.graphics.dialog.DialogConfig;
import tech.fastj.graphics.dialog.DialogUtil;
import tech.fastj.graphics.display.FastJCanvas;
import tech.fastj.graphics.display.RenderSettings;
import tech.fastj.graphics.display.SimpleDisplay;

import tech.fastj.input.keyboard.Keys;
import tech.fastj.systems.control.SceneManager;

import javax.swing.SwingUtilities;
import java.io.IOException;

import core.util.Networking;
import network.client.Client;
import scenes.GameScene;
import scenes.MainMenu;
import scenes.Settings;
import util.Colors;
import util.SceneNames;

public class GameManager extends SceneManager {

    private Client client;
    private GameScene gameScene;
    private MainMenu mainMenu;
    private Settings settings;
    private MusicManager musicManager;

    public Client getClient() {
        return client;
    }

    @Override
    public void init(FastJCanvas canvas) {
        canvas.setBackgroundColor(Colors.Snowy.darker());
        canvas.modifyRenderSettings(RenderSettings.Antialiasing.Enable);

        musicManager = new MusicManager(MusicManager.InitialAudioLevel);

        mainMenu = new MainMenu();
        settings = new Settings();
        gameScene = new GameScene();
        addScene(mainMenu);
        addScene(settings);
        addScene(gameScene);
        setCurrentScene(SceneNames.MainMenu);
        loadCurrentScene();

        musicManager.playMainMusic();
    }

    @Override
    public void update(FastJCanvas canvas) {
        super.update(canvas);
        if (client != null && client.isConnectionClosed()) {
            FastJEngine.runAfterUpdate(FastJEngine::closeGame);
        }
    }

    @Override
    public void reset() {
        super.reset();
        if (client != null && !client.isConnectionClosed()) {
            client.shutdown();
        }
        if (musicManager != null) {
            musicManager.pauseMainMusic();
            musicManager.unloadAll();
        }
    }

    public MusicManager getMusicManager() {
        return musicManager;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public void initClient(int playerNumber) {
        client.addServerAction(Networking.Client.AddPlayer, client -> {
            try {
                int newPlayerNumber = client.in().readInt();
                Log.debug(GameManager.class, "Adding other player {}", newPlayerNumber);
                gameScene.addNewPlayer(newPlayerNumber);
            } catch (IOException exception) {
                ClientMain.displayException("Couldn't receive AddPlayer data", exception);
                FastJEngine.closeGame();
            }
        });
        client.addServerAction(Networking.Client.RemovePlayer, client -> {
            try {
                int removedPlayerNumber = client.in().readInt();
                Log.debug(GameManager.class, "Removing other player {}", removedPlayerNumber);
                gameScene.removePlayer(removedPlayerNumber);
            } catch (IOException exception) {
                ClientMain.displayException("Couldn't receive RemovePlayer data", exception);
                FastJEngine.closeGame();
            }
        });
        client.addServerAction(Networking.Client.PlayerSyncTransform, client -> {
            try {
                int syncPlayerNumber = client.in().readInt();
                float translationX = client.in().readFloat();
                float translationY = client.in().readFloat();
                float rotation = client.in().readFloat();
                Log.debug(GameManager.class, "Syncing player {} to {} {} {}", syncPlayerNumber, translationX, translationY, rotation);
                gameScene.syncOtherPlayer(syncPlayerNumber, translationX, translationY, rotation);
            } catch (IOException exception) {
                ClientMain.displayException("Couldn't receive SyncPlayer data", exception);
                FastJEngine.closeGame();
            }
        });
        client.addServerAction(Networking.Client.PlayerKeyPress, client -> {
            String key = "";
            try {
                int player = client.in().readInt();
                key = client.in().readUTF();
                Keys keyPressed = Keys.valueOf(key);
                Log.debug(GameManager.class, "player {} pressed {}", player, keyPressed.name());
                gameScene.transformOtherPlayer(player, keyPressed, true);
            } catch (IOException exception) {
                ClientMain.displayException("Couldn't receive PlayerKeyPress data", exception);
                FastJEngine.closeGame();
            } catch (IllegalArgumentException exception) {
                Log.warn(GameManager.class, "Invalid identifier press {} from player {}", key, playerNumber);
            }
        });
        client.addServerAction(Networking.Client.PlayerKeyRelease, client -> {
            String key = "";
            try {
                int player = client.in().readInt();
                key = client.in().readUTF();
                Keys keyReleased = Keys.valueOf(key);
                Log.debug(GameManager.class, "player {} released {}", player, keyReleased.name());
                gameScene.transformOtherPlayer(player, keyReleased, false);
            } catch (IOException exception) {
                ClientMain.displayException("Couldn't receive PlayerKeyRelease data", exception);
                FastJEngine.closeGame();
            } catch (IllegalArgumentException exception) {
                Log.warn(GameManager.class, "Invalid identifier release {} from player {}", key, playerNumber);
            }
        });
        client.addServerAction(Networking.Client.PlayerCreateSnowball, client -> {
            try {
                int player = client.in().readInt();
                float trajectoryX = client.in().readFloat();
                float trajectoryY = client.in().readFloat();
                float rotation = client.in().readFloat();
                Pointf trajectory = new Pointf(trajectoryX, trajectoryY);
                Log.debug(GameManager.class, "player {} spawned a snowball headed to {} with rotation {}", player, trajectory, rotation);
                gameScene.spawnSnowball(player, trajectory, rotation);
            } catch (IOException exception) {
                ClientMain.displayException("Couldn't receive PlayerCreateSnowball data", exception);
                FastJEngine.closeGame();
            }
        });
        client.addServerAction(Networking.Client.PlayerTemperatureDeath, client -> {
            try {
                int player = client.in().readInt();
                int otherPlayer = client.in().readInt();
                if (otherPlayer == Integer.MIN_VALUE) {
                    Log.info("Player {} was killed by getting too cold.", player);
                } else {
                    Log.info("Player {} was snowballed by player {}", player, otherPlayer);
                }
            } catch (IOException exception) {
                ClientMain.displayException("Couldn't receive PlayerTemperatureDeath data", exception);
                FastJEngine.closeGame();
            }
        });
        client.addServerAction(Networking.Client.PlayerHitDamageDeath, client -> {
            try {
                int player = client.in().readInt();
                int otherPlayer = client.in().readInt();
                Log.info("Player {} was snowballed by player {}", player, otherPlayer);
            } catch (IOException exception) {
                ClientMain.displayException("Couldn't receive PlayerHitDamageDeath data", exception);
                FastJEngine.closeGame();
            }
        });
        client.addServerAction(Networking.Client.PlayerWins, client -> {
            try {
                int player = client.in().readInt();
                Log.info("we need to show player message dialog");
                SwingUtilities.invokeLater(() -> {
                    DialogUtil.showMessageDialog(
                            DialogConfig.create()
                                    .withTitle(player == playerNumber ? "Win" : "Loss")
                                    .withPrompt(player == playerNumber ? "You win!" : String.format("Player %s wins! You'll get 'em next time.", player))
                                    .withParentComponent(FastJEngine.<SimpleDisplay>getDisplay().getWindow())
                                    .build()
                    );
                    getCurrentScene().unload(FastJEngine.getCanvas());
                    switchScenes(SceneNames.MainMenu);
                    client.shutdown();
                });
            } catch (IOException exception) {
                ClientMain.displayException("Couldn't receive PlayerWins data", exception);
                FastJEngine.closeGame();
            }
        });
    }

    public void runClient() {
        client.run();
    }
}
