package scenes;

import tech.fastj.engine.FastJEngine;
import tech.fastj.logging.Log;
import tech.fastj.math.Transform2D;
import tech.fastj.graphics.dialog.DialogConfig;
import tech.fastj.graphics.dialog.DialogOptions;
import tech.fastj.graphics.dialog.DialogUtil;
import tech.fastj.graphics.display.FastJCanvas;
import tech.fastj.graphics.display.SimpleDisplay;
import tech.fastj.graphics.game.Text2D;
import tech.fastj.graphics.ui.elements.Button;

import tech.fastj.systems.control.Scene;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;

import core.util.Networking;
import game.ClientMain;
import game.GameManager;
import network.client.Client;
import network.client.ClientConfig;
import network.security.SecureServerConfig;
import network.security.SecureTypes;
import util.Colors;
import util.FilePaths;
import util.Fonts;
import util.SceneNames;
import util.Sizes;

public class MainMenu extends Scene {

    private Button joinMultiplayerButton, settingsButton, exitGameButton;
    private Text2D title;

    public MainMenu() {
        super(SceneNames.MainMenu);
    }

    @Override
    public void load(FastJCanvas canvas) {
        joinMultiplayerButton = new Button(this, canvas.getCanvasCenter().add(0f, canvas.getCanvasCenter().y / 3.625f), Sizes.ButtonSize);
        joinMultiplayerButton.setFill(Colors.Snowy);
        joinMultiplayerButton.setText("Join Multiplayer Lobby");
        joinMultiplayerButton.setFont(Fonts.ButtonTextFont);
        joinMultiplayerButton.translate(joinMultiplayerButton.getCenter().subtract(joinMultiplayerButton.getTranslation()).multiply(-1f));
        joinMultiplayerButton.addOnAction(mouseButtonEvent -> SwingUtilities.invokeLater(() -> {
            String hostname;
            do {
                hostname = DialogUtil.showInputDialog(
                        DialogConfig.create().withTitle("Connection Information")
                                .withPrompt("Please specify the IP address, or host name, of the server you want to connect to.")
                                .withParentComponent(FastJEngine.<SimpleDisplay>getDisplay().getWindow())
                                .build()
                );

                if (hostname == null) {
                    return;
                }

            } while (hostname.isBlank());

            JDialog connectingDialog = null;
            try {
                DialogConfig connectingConfig = DialogConfig.create()
                        .withTitle("Connecting to " + hostname + "...")
                        .withPrompt("Connecting to " + hostname + "...")
                        .withParentComponent(FastJEngine.<SimpleDisplay>getDisplay().getWindow())
                        .build();
                connectingDialog = new JDialog((JFrame) connectingConfig.dialogParent(), connectingConfig.title(), false);
                connectingDialog.setAlwaysOnTop(true);

                BorderLayout layout = new BorderLayout();
                connectingDialog.setLayout(layout);
                JPanel container = new JPanel();

                JLabel connectingMessage = new JLabel((String) connectingConfig.prompt());
                connectingMessage.setVisible(true);
                container.add(connectingMessage, BorderLayout.CENTER);

                connectingDialog.setContentPane(container);
                connectingDialog.pack();
                connectingDialog.setLocationRelativeTo(connectingConfig.dialogParent());
                connectingDialog.setVisible(true);

                FastJEngine.<GameManager>getLogicManager().setClient(new Client(
                        new ClientConfig(hostname, Networking.Port),
                        new SecureServerConfig(
                                FilePaths.PublicGameKey,
                                "sslpublicpassword",
                                SecureTypes.TLSv1_3
                        )
                ));

                connectingDialog.setVisible(false);
                Log.info("connected.");

                Client client = FastJEngine.<GameManager>getLogicManager().getClient();
                int playerNumber = client.in().readInt();
                FastJEngine.<GameManager>getLogicManager().<GameScene>getScene(SceneNames.GameScene).setLocalPlayerNumber(playerNumber);

                FastJEngine.<SimpleDisplay>getDisplay().setTitle("Game: Player " + playerNumber);
                Log.info(GameManager.class, "Ready player {}", playerNumber);

                FastJEngine.<GameManager>getLogicManager().switchScenes(SceneNames.GameScene);
                FastJEngine.<GameManager>getLogicManager().initClient(playerNumber);
                FastJEngine.<GameManager>getLogicManager().runClient();
            } catch (IOException exception) {
                connectingDialog.setVisible(false);

                if (exception instanceof ConnectException && exception.getMessage().startsWith("Connection refused: connect")) {
                    String closedHostname = hostname;
                    SwingUtilities.invokeLater(() -> {
                        DialogConfig connectionError = DialogConfig.create()
                                .withTitle("Connection Error")
                                .withPrompt("The server at " + closedHostname + " is not accepting connections, or is offline.")
                                .withParentComponent(FastJEngine.<SimpleDisplay>getDisplay().getWindow())
                                .build();
                        DialogUtil.showMessageDialog(connectionError);
                    });
                    return;
                }

                if (exception instanceof UnknownHostException) {
                    String closedHostname = hostname;
                    SwingUtilities.invokeLater(() -> {
                        DialogConfig connectionError = DialogConfig.create()
                                .withTitle("Connection Error")
                                .withPrompt("The game couldn't find a server at the host \"" + closedHostname + "\".")
                                .withParentComponent(FastJEngine.<SimpleDisplay>getDisplay().getWindow())
                                .build();
                        DialogUtil.showMessageDialog(connectionError);
                    });
                    return;
                }
                ClientMain.displayException("IO/Certificate Configuration error", exception);
                FastJEngine.forceCloseGame();
            } catch (GeneralSecurityException exception) {
                connectingDialog.setVisible(false);
                ClientMain.displayException("IO/Certificate Configuration error", exception);
                FastJEngine.forceCloseGame();
            }
        }));
        drawableManager.addUIElement(joinMultiplayerButton);

        settingsButton = new Button(this, canvas.getCanvasCenter().add(0f, canvas.getCanvasCenter().y / 2.0f), Sizes.ButtonSize);
        settingsButton.setFill(Colors.Snowy);
        settingsButton.setText("Settings");
        settingsButton.setFont(Fonts.ButtonTextFont);
        settingsButton.translate(settingsButton.getCenter().subtract(settingsButton.getTranslation()).multiply(-1f));
        settingsButton.addOnAction(mouseButtonEvent -> FastJEngine.runAfterUpdate(() -> {
            GameManager gameManager = FastJEngine.getLogicManager();
            gameManager.getCurrentScene().unload(canvas);
            gameManager.switchScenes(SceneNames.Settings);
        }));
        drawableManager.addUIElement(settingsButton);

        exitGameButton = new Button(this, canvas.getCanvasCenter().add(0f, canvas.getCanvasCenter().y / 1.375f), Sizes.ButtonSize);
        exitGameButton.setFill(Colors.Snowy);
        exitGameButton.setText("Exit Game");
        exitGameButton.setFont(Fonts.ButtonTextFont);
        exitGameButton.translate(exitGameButton.getCenter().subtract(exitGameButton.getTranslation()).multiply(-1f));
        exitGameButton.addOnAction(mouseButtonEvent -> SwingUtilities.invokeLater(() -> {
            DialogConfig confirmExitConfig = DialogConfig.create()
                    .withTitle("Exit?")
                    .withPrompt("Are you sure you want to exit?")
                    .withParentComponent(FastJEngine.<SimpleDisplay>getDisplay().getWindow())
                    .build();
            if (DialogUtil.showConfirmationDialog(confirmExitConfig, DialogOptions.YesNoCancel)) {
                FastJEngine.closeGame();
            }
        }));

        title = Text2D.create("Snowball Fight WIP")
                .withFont(Fonts.TitleTextFont)
                .withFill(Color.black)
                .withTransform(canvas.getCanvasCenter().subtract(0f, canvas.getCanvasCenter().y / 2f), Transform2D.DefaultRotation, Transform2D.DefaultScale)
                .build();
        title.translate(title.getCenter().subtract(title.getTranslation()).multiply(-1f));
        drawableManager.addGameObject(title);
    }

    @Override
    public void unload(FastJCanvas canvas) {
        joinMultiplayerButton = null;
        settingsButton = null;
        exitGameButton = null;
        title = null;
    }

    @Override
    public void update(FastJCanvas canvas) {
    }
}
