package game;

import tech.fastj.engine.FastJEngine;
import tech.fastj.logging.Log;
import tech.fastj.graphics.dialog.DialogConfig;
import tech.fastj.graphics.dialog.DialogUtil;
import tech.fastj.graphics.display.FastJCanvas;
import tech.fastj.graphics.display.RenderSettings;
import tech.fastj.graphics.display.SimpleDisplay;

import tech.fastj.input.keyboard.Keys;
import tech.fastj.systems.control.SceneManager;

import java.awt.Color;
import java.io.IOException;
import java.security.GeneralSecurityException;

import core.util.Networking;
import network.client.Client;
import network.client.ClientConfig;
import network.security.SecureServerConfig;
import network.security.SecureTypes;
import scene.GameScene;
import util.FilePaths;
import util.SceneNames;

public class GameManager extends SceneManager {

    private Client client;
    private GameScene gameScene;

    public Client getClient() {
        return client;
    }

    @Override
    public void init(FastJCanvas canvas) {
        canvas.setBackgroundColor(Color.lightGray.darker());
        canvas.modifyRenderSettings(RenderSettings.Antialiasing.Enable);

        String hostname;
        do {
            hostname = DialogUtil.showInputDialog(
                    DialogConfig.create().withTitle("Connection Information")
                            .withPrompt("Please specify the IP address, or host name, of the server you want to connect to.")
                            .build()
            );

            if (hostname == null && ClientMain.chooseExit()) {
                System.exit(0);
            }

        } while (hostname == null || hostname.isBlank());

        try {
            Log.info("connecting....");
            client = new Client(
                    new ClientConfig(hostname, Networking.Port),
                    new SecureServerConfig(
                            FilePaths.PublicGameKey,
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
            client.addServerAction(Networking.Client.SyncPlayerTransform, client -> {
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

            gameScene = new GameScene(playerNumber);
            addScene(gameScene);
            setCurrentScene(SceneNames.GameScene);
            loadCurrentScene();
            client.run();
        } catch (IOException | GeneralSecurityException exception) {
            ClientMain.displayException("IO/Certificate Configuration error", exception);
            FastJEngine.forceCloseGame();
        }
    }

    @Override
    public void reset() {
        super.reset();
        if (client != null && !client.isConnectionClosed()) {
            client.shutdown();
        }
    }
}
