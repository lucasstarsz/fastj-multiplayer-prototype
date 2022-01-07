package scenes;

import tech.fastj.engine.FastJEngine;
import tech.fastj.math.Pointf;
import tech.fastj.math.Transform2D;
import tech.fastj.graphics.display.FastJCanvas;
import tech.fastj.graphics.display.RenderSettings;
import tech.fastj.graphics.game.Text2D;
import tech.fastj.graphics.ui.elements.Button;

import tech.fastj.systems.control.Scene;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import game.GameManager;
import game.MusicManager;
import ui.ArrowedButton;
import ui.Slider;
import util.Colors;
import util.Drawables;
import util.Fonts;
import util.SceneNames;
import util.Scenes;
import util.Sizes;

public class Settings extends Scene {

    private Slider musicLevel;
    private Text2D musicSliderText;
    private Button backToMainMenuButton;

    private ArrowedButton antialiasButton;
    private Text2D antialiasText;
    private static final Map<Integer, RenderSettings> AntialiasOptions = Map.of(
            0, RenderSettings.Antialiasing.Enable,
            1, RenderSettings.Antialiasing.Disable
    );
    private static final List<String> AntialiasOptionsList = List.of(AntialiasOptions.get(0).valueString, AntialiasOptions.get(1).valueString);

    private ArrowedButton alphaInterpolationButton;
    private Text2D alphaInterpolationText;
    private static final Map<Integer, RenderSettings> AlphaInterpolationOptions = Map.of(
            0, RenderSettings.AlphaInterpolationQuality.High,
            1, RenderSettings.AlphaInterpolationQuality.Low
    );
    private static final List<String> AlphaInterpolationOptionsList = List.of(AlphaInterpolationOptions.get(0).valueString, AlphaInterpolationOptions.get(1).valueString);

    public Settings() {
        super(SceneNames.Settings);
    }

    @Override
    public void load(FastJCanvas canvas) {
        musicLevel = new Slider(this, Sizes.NormalSlider);
        musicLevel.addOnReleaseAction(mouseButtonEvent -> {
            MusicManager musicManager = FastJEngine.<GameManager>getLogicManager().getMusicManager();
            musicManager.setMusicLevel(musicLevel.getSliderValue());
        });
        musicLevel.setSliderPosition(MusicManager.InitialAudioLevel);
        musicLevel.translate(canvas.getCanvasCenter().subtract(0f, canvas.getCanvasCenter().y / 2.0f));
        musicLevel.translate(musicLevel.getCenter().subtract(musicLevel.getTranslation()).multiply(-1f));
        musicSliderText = Drawables.centered(Text2D.create("Music Volume")
                .withFont(Fonts.ButtonTextFont)
                .withFill(Color.black)
                .withTransform(musicLevel.getTranslation(), Transform2D.DefaultRotation, Transform2D.DefaultScale)
                .build()
        );
        musicSliderText.translate(new Pointf(-musicSliderText.width(), musicSliderText.height()).divide(1.5f));
        drawableManager.addUIElement(musicLevel);
        drawableManager.addGameObject(musicSliderText);

        antialiasButton = new ArrowedButton(this, Pointf.origin(), Sizes.SmallButton, AntialiasOptionsList, 0);
        antialiasButton.translate(canvas.getCanvasCenter().subtract(0f, canvas.getCanvasCenter().y / 3f));
        antialiasButton.translate(antialiasButton.getCenter().subtract(antialiasButton.getTranslation()).multiply(-1f));
        antialiasButton.setFill(Colors.Snowy);
        antialiasButton.addOnAction(mouseButtonEvent -> {
            RenderSettings renderSetting = AntialiasOptions.get(antialiasButton.getSelectedOption());
            canvas.modifyRenderSettings(renderSetting);
        });
        antialiasText = Drawables.centered(Text2D.create("Antialiasing")
                .withFont(Fonts.ButtonTextFont)
                .withFill(Color.black)
                .withTransform(antialiasButton.getTranslation(), Transform2D.DefaultRotation, Transform2D.DefaultScale)
                .build()
        );
        antialiasText.translate(new Pointf(-antialiasText.width(), antialiasText.height() / 1.5f));
        drawableManager.addUIElement(antialiasButton);
        drawableManager.addGameObject(antialiasText);

        alphaInterpolationButton = new ArrowedButton(this, Pointf.origin(), Sizes.SmallButton, AlphaInterpolationOptionsList, 0);
        alphaInterpolationButton.translate(canvas.getCanvasCenter().add(0f, -canvas.getCanvasCenter().y / 6f));
        alphaInterpolationButton.translate(alphaInterpolationButton.getCenter().subtract(alphaInterpolationButton.getTranslation()).multiply(-1f));
        alphaInterpolationButton.setFill(Colors.Snowy);
        alphaInterpolationButton.addOnAction(mouseButtonEvent -> {
            RenderSettings renderSetting = AlphaInterpolationOptions.get(alphaInterpolationButton.getSelectedOption());
            canvas.modifyRenderSettings(renderSetting);
        });
        alphaInterpolationText = Drawables.centered(Text2D.create("Alpha Interpolation")
                .withFont(Fonts.ButtonTextFont)
                .withFill(Color.black)
                .withTransform(alphaInterpolationButton.getTranslation(), Transform2D.DefaultRotation, Transform2D.DefaultScale)
                .build()
        );
        alphaInterpolationText.translate(new Pointf(-alphaInterpolationText.width() * 0.75f, alphaInterpolationText.height() / 1.5f));
        drawableManager.addUIElement(alphaInterpolationButton);
        drawableManager.addGameObject(alphaInterpolationText);

        backToMainMenuButton = Drawables.centered(new Button(this, canvas.getCanvasCenter().add(0f, canvas.getCanvasCenter().y / 1.5f), Sizes.NormalButton)
                .setFill(Colors.Snowy)
                .setText("Back to Main Menu")
                .setFont(Fonts.ButtonTextFont)
        );
        backToMainMenuButton.addOnAction(mouseButtonEvent -> Scenes.switchScene(SceneNames.MainMenu, false));
        drawableManager.addUIElement(backToMainMenuButton);
    }

    @Override
    public void unload(FastJCanvas canvas) {
        musicLevel = null;
        musicSliderText = null;
        backToMainMenuButton = null;

        antialiasButton = null;
        antialiasText = null;

        alphaInterpolationButton = null;
        alphaInterpolationText = null;
    }

    @Override
    public void update(FastJCanvas canvas) {
    }
}
