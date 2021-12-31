package scenes;

import tech.fastj.engine.FastJEngine;
import tech.fastj.logging.Log;
import tech.fastj.math.Pointf;
import tech.fastj.math.Transform2D;
import tech.fastj.graphics.Boundary;
import tech.fastj.graphics.display.FastJCanvas;
import tech.fastj.graphics.game.Text2D;
import tech.fastj.graphics.ui.elements.Button;

import tech.fastj.systems.control.Scene;

import java.awt.Color;

import game.GameManager;
import game.MusicManager;
import ui.Slider;
import util.Colors;
import util.Fonts;
import util.SceneNames;
import util.Sizes;

public class Settings extends Scene {

    private Slider musicLevel;
    private Text2D musicSliderText;
    private Button backToMainMenuButton;

    public Settings() {
        super(SceneNames.Settings);
    }

    @Override
    public void load(FastJCanvas canvas) {
        musicLevel = new Slider(this, Sizes.SliderSize);
        musicLevel.setSliderPosition(MusicManager.InitialAudioLevel);
        musicLevel.translate(canvas.getCanvasCenter().subtract(100f, canvas.getCanvasCenter().y / 2.0f));
        musicLevel.translate(musicLevel.getCenter().subtract(musicLevel.getTranslation()).multiply(-1f));
        musicLevel.addOnDragAction(mouseMotionEvent -> {
            MusicManager musicManager = FastJEngine.<GameManager>getLogicManager().getMusicManager();
            musicManager.setMusicLevel(musicLevel.getSliderValue());
        });
        drawableManager.addUIElement(musicLevel);

        musicSliderText = Text2D.create("Audio:")
                .withFont(Fonts.ButtonTextFont)
                .withFill(Color.black)
                .withTransform(musicLevel.getTranslation(), Transform2D.DefaultRotation, Transform2D.DefaultScale)
                .build();
        musicSliderText.translate(new Pointf(
                -musicSliderText.getBound(Boundary.TopRight).subtract(musicSliderText.getBound(Boundary.TopLeft)).x,
                musicSliderText.getBound(Boundary.BottomRight).subtract(musicSliderText.getBound(Boundary.TopRight)).y / 1.5f
        ));
        musicSliderText.translate(musicSliderText.getCenter().subtract(musicSliderText.getTranslation()).multiply(-1f));
        Log.info(musicSliderText.toString());
        drawableManager.addGameObject(musicSliderText);

        backToMainMenuButton = new Button(this, canvas.getCanvasCenter().add(0f, canvas.getCanvasCenter().y / 1.5f), Sizes.ButtonSize);
        backToMainMenuButton.setFill(Colors.Snowy);
        backToMainMenuButton.setText("Back to Main Menu");
        backToMainMenuButton.setFont(Fonts.ButtonTextFont);
        backToMainMenuButton.translate(backToMainMenuButton.getCenter().subtract(backToMainMenuButton.getTranslation()).multiply(-1f));
        backToMainMenuButton.addOnAction(mouseButtonEvent -> FastJEngine.runAfterUpdate(() -> {
            GameManager gameManager = FastJEngine.getLogicManager();
            gameManager.switchScenes(SceneNames.MainMenu);
        }));
        drawableManager.addUIElement(backToMainMenuButton);
    }

    @Override
    public void unload(FastJCanvas canvas) {
    }

    @Override
    public void update(FastJCanvas canvas) {
    }
}
