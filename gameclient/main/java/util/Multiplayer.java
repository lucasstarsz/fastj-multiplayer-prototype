package util;

import tech.fastj.engine.FastJEngine;
import tech.fastj.logging.Log;
import tech.fastj.graphics.dialog.DialogConfig;
import tech.fastj.graphics.display.SimpleDisplay;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import core.util.FilePathUtil;
import core.util.Networking;
import game.ClientMain;
import game.GameManager;
import network.client.Client;
import network.client.ClientConfig;
import network.security.SecureServerConfig;
import network.security.SecureTypes;
import scenes.GameScene;

public class Multiplayer {

    public static void connect() {
        SwingUtilities.invokeLater(() -> {
            String hostname = Dialogs.userInput(DialogConfig.create().withTitle("Connection Information")
                    .withPrompt("Please specify the IP address, or host name, of the server you want to connect to.")
                    .withParentComponent(FastJEngine.<SimpleDisplay>getDisplay().getWindow())
                    .build()
            );

            DialogConfig connectingConfig = DialogConfig.create().withTitle("Connecting...")
                    .withPrompt("Connecting to " + hostname + "...")
                    .withParentComponent(FastJEngine.<SimpleDisplay>getDisplay().getWindow())
                    .build();

            JDialog connectingDialog = new JDialog((JFrame) connectingConfig.dialogParent(), connectingConfig.title(), true);
            JLabel connectingMessage = new JLabel((String) connectingConfig.prompt());
            connectingDialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            connectingDialog.add(connectingMessage);
            connectingDialog.pack();
            connectingDialog.setLocationRelativeTo(connectingConfig.dialogParent());

            ExecutorService clientJoin = Executors.newSingleThreadExecutor();
            Future<Client> futureClient = clientJoin.submit(() -> setupClient(connectingDialog, hostname));
            connectingDialog.setVisible(true);

            GameManager gameManager = FastJEngine.getLogicManager();
            Client client;
            try {
                client = futureClient.get();
            } catch (InterruptedException | ExecutionException exception) {
                gameManager.setClient(null);
                ClientMain.displayException("", exception);
                return;
            }

            int playerNumber;
            try {
                playerNumber = client.in().readInt();
            } catch (IOException exception) {
                client.shutdown();
                gameManager.setClient(null);
                ClientMain.displayException("", exception);
                return;
            }

            GameScene gameScene = gameManager.getScene(SceneNames.GameScene);
            gameScene.setLocalPlayerNumber(playerNumber);

            SimpleDisplay simpleDisplay = FastJEngine.getDisplay();
            simpleDisplay.setTitle("Game: Player " + playerNumber);
            Log.info(GameManager.class, "Ready player {}", playerNumber);

            Scenes.switchScene(SceneNames.GameScene, false);
            gameManager.initClient(playerNumber);
            gameManager.runClient();
        });
    }

    private static Client setupClient(JDialog connectingDialog, String hostname) {
        try {
            Client client = new Client(new ClientConfig(hostname, Networking.Port), new SecureServerConfig(
                    FilePathUtil.streamResource(FilePaths.class, FilePaths.PublicGameKey),
                    "sslpublicpassword",
                    SecureTypes.TLSv1_3
            ));

            connectingDialog.setVisible(false);
            Log.info("connected.");
            return client;
        } catch (IOException exception) {
            GameManager gameManager = FastJEngine.getLogicManager();
            connectingDialog.setVisible(false);
            Client client = gameManager.getClient();

            if (client != null) {
                client.shutdown();
                gameManager.setClient(null);
            }

            if (exception instanceof ConnectException && exception.getMessage().startsWith("Connection refused")) {
                Dialogs.message(DialogConfig.create().withTitle("Connection Error")
                        .withPrompt("The server at " + hostname + " is not accepting connections, or is offline.")
                        .withParentComponent(FastJEngine.<SimpleDisplay>getDisplay().getWindow())
                        .build()
                );
                return null;
            }

            if (exception instanceof UnknownHostException) {
                Dialogs.message(DialogConfig.create().withTitle("Connection Error")
                        .withPrompt("A game server couldn't be found at the host \"" + hostname + "\".")
                        .withParentComponent(FastJEngine.<SimpleDisplay>getDisplay().getWindow())
                        .build()
                );
                return null;
            }

            ClientMain.displayException("IO error", exception);
            return null;
        } catch (GeneralSecurityException exception) {
            GameManager gameManager = FastJEngine.getLogicManager();
            connectingDialog.setVisible(false);

            Client client = gameManager.getClient();
            if (client != null) {
                client.shutdown();
                gameManager.setClient(null);
            }

            ClientMain.displayException("Certificate Configuration error", exception);
            return null;
        }
    }
}
