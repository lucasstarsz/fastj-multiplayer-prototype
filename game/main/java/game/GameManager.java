package game;

import tech.fastj.engine.FastJEngine;
import tech.fastj.logging.Log;
import tech.fastj.graphics.display.FastJCanvas;
import tech.fastj.graphics.display.SimpleDisplay;

import tech.fastj.input.keyboard.Keys;
import tech.fastj.systems.control.SceneManager;

import java.io.IOException;
import java.security.GeneralSecurityException;

import network.client.Client;
import network.client.ClientConfig;
import network.security.SecureServerConfig;
import network.security.SecureTypes;
import network.server.Server;
import scene.GameScene;
import util.FilePaths;
import util.Networking;
import util.SceneNames;

public class GameManager extends SceneManager {

    private Client client;
    private GameScene gameScene;

    public Client getClient() {
        return client;
    }

    @Override
    public void init(FastJCanvas canvas) {
        try {
            Log.info("connecting....");
            client = new Client(
                    new ClientConfig("localhost", Networking.Port),
                    new SecureServerConfig(
                            FilePaths.PublicGameKeyPath,
                            "sslpublicpassword",
                            SecureTypes.TLSv1_3
                    )
            );
            Log.info("connected.");
            int playerNumber = client.in().readInt();
            FastJEngine.<SimpleDisplay>getDisplay().setTitle("Game: Player " + playerNumber);
            Log.info(GameManager.class, "Ready player {}", playerNumber);

            client.addServerAction(Networking.Client.AddPlayer, client -> {
                try {
                    int newPlayerNumber = client.in().readInt();
                    Log.info(GameManager.class, "Adding player {}", newPlayerNumber);
                    gameScene.addNewPlayer(newPlayerNumber);
                } catch (IOException exception) {
                    FastJEngine.error("IO error", exception);
                }
            });
            client.addServerAction(Networking.Client.RemovePlayer, client -> {
                try {
                    int removedPlayerNumber = client.in().readInt();
                    Log.info(GameManager.class, "Removing player {}", removedPlayerNumber);
                    gameScene.removePlayer(removedPlayerNumber);
                } catch (IOException exception) {
                    FastJEngine.error("IO error", exception);
                }
            });
            client.addServerAction(Networking.Client.SyncPlayerTransform, client -> {
                try {
                    int syncPlayerNumber = client.in().readInt();
                    float translationX = client.in().readFloat();
                    float translationY = client.in().readFloat();
                    float rotation = client.in().readFloat();
                    Log.info(GameManager.class, "Syncing player {} to {} {} {}", syncPlayerNumber, translationX, translationY, rotation);
                    gameScene.syncOtherPlayer(syncPlayerNumber, translationX, translationY, rotation);
                } catch (IOException exception) {
                    FastJEngine.error("IO error", exception);
                }
            });
            client.addServerAction(Networking.Client.PlayerKeyPress, client -> {
                String key = "";
                try {
                    int player = client.in().readInt();
                    key = client.in().readUTF();
                    Keys keyPressed = Keys.valueOf(key);
                    Log.info(GameManager.class, "player {} pressed {}", player, keyPressed.name());
                    gameScene.transformOtherPlayer(player, keyPressed, true);
                } catch (IOException exception) {
                    FastJEngine.error("IO error", exception);
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
                    Log.info(GameManager.class, "player {} released {}", player, keyReleased.name());
                    gameScene.transformOtherPlayer(player, keyReleased, false);
                } catch (IOException exception) {
                    FastJEngine.error("IO error", exception);
                } catch (IllegalArgumentException exception) {
                    Log.warn(GameManager.class, "Invalid identifier release {} from player {}", key, playerNumber);
                }
            });

            gameScene = new GameScene(playerNumber);
            addScene(gameScene);
            setCurrentScene(SceneNames.GameScene);
            loadCurrentScene();
            client.run();
        } catch (IOException | GeneralSecurityException exception) {
            FastJEngine.error("Couldn't connect", exception);
        }
    }

    @Override
    public void reset() {
        super.reset();
        if (client != null) {
            client.disconnect(Server.ClientLeave, "Session ended.");
        }
    }
}
