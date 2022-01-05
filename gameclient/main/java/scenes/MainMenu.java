package scenes;

import tech.fastj.math.Transform2D;
import tech.fastj.graphics.display.FastJCanvas;
import tech.fastj.graphics.game.Text2D;
import tech.fastj.graphics.ui.elements.Button;

import tech.fastj.systems.control.Scene;

import java.awt.Color;

import util.Colors;
import util.Dialogs;
import util.Drawables;
import util.Fonts;
import util.Multiplayer;
import util.SceneNames;
import util.Scenes;
import util.Sizes;

public class MainMenu extends Scene {

    private Button joinMultiplayerButton, settingsButton, exitGameButton;
    private Text2D title;

    public MainMenu() {
        super(SceneNames.MainMenu);
    }

    @Override
    public void load(FastJCanvas canvas) {
        joinMultiplayerButton = Drawables.centered(new Button(this, canvas.getCanvasCenter().add(0f, canvas.getCanvasCenter().y / 3.625f), Sizes.NormalButton)
                .setFill(Colors.Snowy)
                .setText("Join Multiplayer Lobby")
                .setFont(Fonts.ButtonTextFont)
        );
        joinMultiplayerButton.addOnAction(mouseButtonEvent -> Multiplayer.connect());
        drawableManager.addUIElement(joinMultiplayerButton);

        settingsButton = Drawables.centered(new Button(this, canvas.getCanvasCenter().add(0f, canvas.getCanvasCenter().y / 2.0f), Sizes.NormalButton)
                .setFill(Colors.Snowy)
                .setText("Settings")
                .setFont(Fonts.ButtonTextFont)
        );
        settingsButton.addOnAction(mouseButtonEvent -> Scenes.switchScene(SceneNames.Settings, true));
        drawableManager.addUIElement(settingsButton);

        exitGameButton = Drawables.centered(new Button(this, canvas.getCanvasCenter().add(0f, canvas.getCanvasCenter().y / 1.375f), Sizes.NormalButton)
                .setFill(Colors.Snowy)
                .setText("Exit Game")
                .setFont(Fonts.ButtonTextFont)
        );
        exitGameButton.addOnAction(mouseButtonEvent -> Dialogs.confirmExit());

        title = Drawables.centered(Text2D.create("Snowball Fight WIP")
                .withFont(Fonts.TitleTextFont)
                .withFill(Color.black)
                .withTransform(canvas.getCanvasCenter().subtract(0f, canvas.getCanvasCenter().y / 2f), Transform2D.DefaultRotation, Transform2D.DefaultScale)
                .build()
        );
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
