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

import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
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

        try {
            Font notoSansRegular = Font.createFont(Font.TRUETYPE_FONT, FilePaths.NotoSansRegular);
            Font notoSansBold = Font.createFont(Font.TRUETYPE_FONT, FilePaths.NotoSansBold);
            Font notoSansBoldItalic = Font.createFont(Font.TRUETYPE_FONT, FilePaths.NotoSansBoldItalic);
            Font notoSansItalic = Font.createFont(Font.TRUETYPE_FONT, FilePaths.NotoSansItalic);
            Font notoSansMono = Font.createFont(Font.TRUETYPE_FONT, FilePaths.NotoSansMono);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(notoSansRegular);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(notoSansBold);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(notoSansBoldItalic);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(notoSansItalic);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(notoSansMono);
        } catch (FontFormatException | IOException exception) {
            SwingUtilities.invokeLater(() -> {
                ClientMain.displayException("Couldn't load font files", exception);
                FastJEngine.closeGame();
            });
        }

        String hostname = DialogUtil.showInputDialog(
                DialogConfig.create().withTitle("Connection Information")
                        .withPrompt("Please specify the IP address, or host name, of the server you want to connect to.")
                        .build()
        );

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
                    Log.info(GameManager.class, "Adding player {}", newPlayerNumber);
                    gameScene.addNewPlayer(newPlayerNumber);
                } catch (IOException exception) {
                    SwingUtilities.invokeLater(() -> {
                        ClientMain.displayException("Couldn't receive AddPlayer data", exception);
                        FastJEngine.closeGame();
                    });
                }
            });
            client.addServerAction(Networking.Client.RemovePlayer, client -> {
                try {
                    int removedPlayerNumber = client.in().readInt();
                    Log.info(GameManager.class, "Removing player {}", removedPlayerNumber);
                    gameScene.removePlayer(removedPlayerNumber);
                } catch (IOException exception) {
                    SwingUtilities.invokeLater(() -> {
                        ClientMain.displayException("Couldn't receive RemovePlayer data", exception);
                        FastJEngine.closeGame();
                    });
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
                    SwingUtilities.invokeLater(() -> {
                        ClientMain.displayException("Couldn't receive SyncPlayer data", exception);
                        FastJEngine.closeGame();
                    });
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
                    SwingUtilities.invokeLater(() -> {
                        ClientMain.displayException("Couldn't receive PlayerKeyPress data", exception);
                        FastJEngine.closeGame();
                    });
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
                    SwingUtilities.invokeLater(() -> {
                        ClientMain.displayException("Couldn't receive PlayerKeyRelease data", exception);
                        FastJEngine.closeGame();
                    });
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
            SwingUtilities.invokeLater(() -> {
                ClientMain.displayException("IO/Certificate Configuration error", exception);
                FastJEngine.closeGame();
            });
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
