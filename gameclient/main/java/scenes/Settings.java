package scenes;

import tech.fastj.engine.FastJEngine;
import tech.fastj.math.Pointf;
import tech.fastj.math.Transform2D;
import tech.fastj.graphics.Boundary;
import tech.fastj.graphics.display.FastJCanvas;
import tech.fastj.graphics.display.RenderSettings;
import tech.fastj.graphics.game.Text2D;
import tech.fastj.graphics.ui.elements.Button;

import tech.fastj.input.mouse.events.MouseButtonEvent;
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
    private static final Map<String, RenderSettings> AntialiasOptions = Map.of(
            "On", RenderSettings.Antialiasing.Enable,
            "Off", RenderSettings.Antialiasing.Disable
    );
    private static final Map<Integer, String> AntialiasArrowMap = Map.of(
            0, "On",
            1, "Off"
    );
    private static final List<String> AntialiasOptionsList = List.of(AntialiasArrowMap.get(0), AntialiasArrowMap.get(1));

    private ArrowedButton alphaInterpolationButton;
    private Text2D alphaInterpolationText;
    private static final Map<String, RenderSettings> AlphaInterpolationOptions = Map.of(
            "High", RenderSettings.AlphaInterpolationQuality.High,
            "Medium", RenderSettings.AlphaInterpolationQuality.Low
    );
    private static final Map<Integer, String> AlphaInterpolationArrowMap = Map.of(
            0, "High",
            1, "Medium"
    );
    private static final List<String> AlphaInterpolationOptionsList = List.of(AlphaInterpolationArrowMap.get(0), AlphaInterpolationArrowMap.get(1));

    public Settings() {
        super(SceneNames.Settings);
    }

    @Override
    public void load(FastJCanvas canvas) {
        musicLevel = new Slider(this, Sizes.NormalSlider) {
            @Override
            public void onMouseReleased(MouseButtonEvent mouseButtonEvent) {
                MusicManager musicManager = FastJEngine.<GameManager>getLogicManager().getMusicManager();
                musicManager.setMusicLevel(musicLevel.getSliderValue());
            }
        };
        musicLevel.setSliderPosition(MusicManager.InitialAudioLevel);
        musicLevel.translate(canvas.getCanvasCenter().subtract(0f, canvas.getCanvasCenter().y / 2.0f));
        musicLevel.translate(musicLevel.getCenter().subtract(musicLevel.getTranslation()).multiply(-1f));
        drawableManager.addUIElement(musicLevel);

        musicSliderText = Drawables.centered(Text2D.create("Music Volume")
                .withFont(Fonts.ButtonTextFont)
                .withFill(Color.black)
                .withTransform(musicLevel.getTranslation(), Transform2D.DefaultRotation, Transform2D.DefaultScale)
                .build()
        );
        musicSliderText.translate(new Pointf(
                -musicSliderText.getBound(Boundary.TopRight).subtract(musicSliderText.getBound(Boundary.TopLeft)).x / 1.5f,
                musicSliderText.getBound(Boundary.BottomRight).subtract(musicSliderText.getBound(Boundary.TopRight)).y / 1.5f
        ));
        drawableManager.addGameObject(musicSliderText);

        antialiasButton = new ArrowedButton(this, Pointf.origin(), Sizes.SmallButton, AntialiasOptionsList, 0);
        antialiasButton.translate(canvas.getCanvasCenter().subtract(0f, canvas.getCanvasCenter().y / 3f));
        antialiasButton.translate(antialiasButton.getCenter().subtract(antialiasButton.getTranslation()).multiply(-1f));
        antialiasButton.setFill(Colors.Snowy);
        antialiasButton.addOnAction(mouseButtonEvent -> {
            RenderSettings renderSetting = AntialiasOptions.get(AntialiasArrowMap.get(antialiasButton.getSelectedOption()));
            canvas.modifyRenderSettings(renderSetting);
        });
        drawableManager.addUIElement(antialiasButton);

        antialiasText = Drawables.centered(Text2D.create("Antialiasing")
                .withFont(Fonts.ButtonTextFont)
                .withFill(Color.black)
                .withTransform(antialiasButton.getTranslation(), Transform2D.DefaultRotation, Transform2D.DefaultScale)
                .build()
        );
        antialiasText.translate(new Pointf(
                -antialiasText.getBound(Boundary.TopRight).subtract(antialiasText.getBound(Boundary.TopLeft)).x,
                antialiasText.getBound(Boundary.BottomRight).subtract(antialiasText.getBound(Boundary.TopRight)).y / 1.5f
        ));
        drawableManager.addGameObject(antialiasText);

        alphaInterpolationButton = new ArrowedButton(this, Pointf.origin(), Sizes.SmallButton, AlphaInterpolationOptionsList, 0);
        alphaInterpolationButton.translate(canvas.getCanvasCenter().add(0f, -canvas.getCanvasCenter().y / 6f));
        alphaInterpolationButton.translate(alphaInterpolationButton.getCenter().subtract(alphaInterpolationButton.getTranslation()).multiply(-1f));
        alphaInterpolationButton.setFill(Colors.Snowy);
        alphaInterpolationButton.addOnAction(mouseButtonEvent -> {
            RenderSettings renderSetting = AlphaInterpolationOptions.get(AlphaInterpolationArrowMap.get(alphaInterpolationButton.getSelectedOption()));
            canvas.modifyRenderSettings(renderSetting);
        });
        drawableManager.addUIElement(alphaInterpolationButton);

        alphaInterpolationText = Drawables.centered(Text2D.create("Alpha Interpolation")
                .withFont(Fonts.ButtonTextFont)
                .withFill(Color.black)
                .withTransform(alphaInterpolationButton.getTranslation(), Transform2D.DefaultRotation, Transform2D.DefaultScale)
                .build()
        );
        alphaInterpolationText.translate(new Pointf(
                -alphaInterpolationText.getBound(Boundary.TopRight).subtract(alphaInterpolationText.getBound(Boundary.TopLeft)).x * 0.75f,
                alphaInterpolationText.getBound(Boundary.BottomRight).subtract(alphaInterpolationText.getBound(Boundary.TopRight)).y / 1.5f
        ));
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
