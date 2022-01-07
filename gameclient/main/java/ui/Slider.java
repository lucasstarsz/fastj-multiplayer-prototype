package ui;

import tech.fastj.engine.FastJEngine;
import tech.fastj.logging.Log;
import tech.fastj.math.Maths;
import tech.fastj.math.Pointf;
import tech.fastj.graphics.Boundary;
import tech.fastj.graphics.display.Camera;
import tech.fastj.graphics.game.Polygon2D;
import tech.fastj.graphics.game.RenderStyle;
import tech.fastj.graphics.ui.EventCondition;
import tech.fastj.graphics.ui.UIElement;
import tech.fastj.graphics.util.DrawUtil;

import tech.fastj.input.mouse.Mouse;
import tech.fastj.input.mouse.MouseActionListener;
import tech.fastj.input.mouse.events.MouseButtonEvent;
import tech.fastj.input.mouse.events.MouseMotionEvent;
import tech.fastj.input.mouse.events.MouseWindowEvent;
import tech.fastj.systems.control.Scene;
import tech.fastj.systems.control.SimpleManager;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.util.function.Consumer;

import util.Colors;

public class Slider extends UIElement<MouseButtonEvent> implements MouseActionListener {

    private final Polygon2D sliderBase;
    private final Polygon2D interactiveSliderObject;
    private final float mininum, maximum;

    private float sliderPosition;
    private EventCondition onActionCondition;

    private boolean hasFocus;

    public Slider(Scene origin, Pointf size) {
        super(origin);
        sliderBase = Polygon2D.create(DrawUtil.createBox(new Pointf(0f, size.y / 2f), new Pointf(size.x, 1f)))
                .withFill(Color.darkGray)
                .build();
        interactiveSliderObject = Polygon2D.create(DrawUtil.createBox(
                                Pointf.origin(),
                                new Pointf(25f, size.y)
                        ), RenderStyle.FillAndOutline
                ).withFill(Colors.Snowy.darker().darker())
                .withOutline(Polygon2D.DefaultOutlineStroke, Polygon2D.DefaultOutlineColor)
                .build();
        mininum = sliderBase.getBound(Boundary.TopLeft).x;
        maximum = sliderBase.getBound(Boundary.TopRight).x;

        super.setCollisionPath(DrawUtil.createPath(DrawUtil.createBox(
                sliderBase.getBound(Boundary.TopLeft).subtract(0f, size.y / 2f),
                new Pointf(maximum, interactiveSliderObject.getBound(Boundary.BottomRight).y)
        )));
        onActionCondition = event -> Mouse.getMouseLocation().intersects(this.getCollisionPath());

        origin.inputManager.addMouseActionListener(this);
    }

    public Slider(SimpleManager origin, Pointf size) {
        super(origin);
        sliderBase = Polygon2D.create(DrawUtil.createBox(new Pointf(0f, size.y / 2f), new Pointf(size.x, 1f)))
                .withFill(Color.darkGray)
                .build();
        interactiveSliderObject = Polygon2D.create(DrawUtil.createBox(
                                Pointf.origin(),
                                new Pointf(25f, size.y)
                        ), RenderStyle.FillAndOutline
                ).withFill(Colors.Snowy.darker().darker())
                .withOutline(Polygon2D.DefaultOutlineStroke, Polygon2D.DefaultOutlineColor)
                .build();
        mininum = sliderBase.getBound(Boundary.TopLeft).x;
        maximum = sliderBase.getBound(Boundary.TopRight).x;

        super.setCollisionPath(DrawUtil.createPath(DrawUtil.createBox(
                sliderBase.getBound(Boundary.TopLeft).subtract(0f, size.y / 2f),
                new Pointf(maximum, interactiveSliderObject.getBound(Boundary.BottomRight).y)
        )));
        onActionCondition = event -> Mouse.getMouseLocation().intersects(this.getCollisionPath());

        origin.inputManager.addMouseActionListener(this);
    }

    public float getSliderValue() {
        return Maths.normalize(sliderPosition, mininum, maximum);
    }

    public void setSliderPosition(float percentage) {
        sliderPosition = Maths.denormalize(percentage, mininum, maximum);
        interactiveSliderObject.setTranslation(new Pointf(
                Maths.withinRange(sliderPosition, mininum, maximum),
                0f
        ));
    }

    protected void setOnActionCondition(EventCondition condition) {
        onActionCondition = condition;
    }

    public UIElement<MouseButtonEvent> setOnReleaseAction(Consumer<MouseButtonEvent> action) {
        onActionEvents.clear();
        onActionEvents.add(action);
        return this;
    }

    public UIElement<MouseButtonEvent> addOnReleaseAction(Consumer<MouseButtonEvent> action) {
        onActionEvents.add(action);
        return this;
    }

    @Override
    public void onMouseDragged(MouseMotionEvent mouseMotionEvent) {
        if (hasFocus) {
            Pointf mouseLocation = mouseMotionEvent.getMouseLocation().divide(FastJEngine.getCanvas().getResolutionScale());
            moveSlider(mouseLocation);
        }
    }

    @Override
    public void onMouseReleased(MouseButtonEvent mouseButtonEvent) {
        if (hasFocus) {
            hasFocus = false;
            Pointf mouseLocation = Mouse.getMouseLocation().divide(FastJEngine.getCanvas().getResolutionScale());
            moveSlider(mouseLocation);
            for (Consumer<MouseButtonEvent> onActionEvent : onActionEvents) {
                onActionEvent.accept(mouseButtonEvent);
            }
        }
        if (onActionCondition.condition(mouseButtonEvent)) {
            hasFocus = true;
        }
    }

    @Override
    public void onMousePressed(MouseButtonEvent mouseButtonEvent) {
        hasFocus = onActionCondition.condition(mouseButtonEvent);
    }

    @Override
    public void onMouseClicked(MouseButtonEvent mouseButtonEvent) {
        hasFocus = onActionCondition.condition(mouseButtonEvent);
    }

    @Override
    public void onMouseEntersScreen(MouseWindowEvent mouseWindowEvent) {
        hasFocus = false;
    }

    @Override
    public void onMouseExitsScreen(MouseWindowEvent mouseWindowEvent) {
        hasFocus = false;
    }

    private void moveSlider(Pointf mouseLocation) {
        sliderPosition = Maths.withinRange(
                mouseLocation.x - getTranslation().x,
                mininum,
                maximum
        );
        float interactiveSliderObjectWidth = interactiveSliderObject.getBound(Boundary.TopRight).x - interactiveSliderObject.getBound(Boundary.TopLeft).x;
        interactiveSliderObject.setTranslation(new Pointf(sliderPosition - interactiveSliderObjectWidth / 2f, 0f));
        Log.info("{} {} {}", mouseLocation, sliderPosition, interactiveSliderObject.getTranslation());
    }

    @Override
    public void renderAsGUIObject(Graphics2D g, Camera camera) {
        if (!shouldRender()) {
            return;
        }

        AffineTransform oldTransform = (AffineTransform) g.getTransform().clone();
        g.transform(getTransformation());

        try {
            g.transform(camera.getTransformation().createInverse());
        } catch (NoninvertibleTransformException exception) {
            throw new IllegalStateException(
                    "Couldn't create an inverse transform of " + camera.getTransformation(),
                    exception
            );
        }

        sliderBase.render(g);
        interactiveSliderObject.render(g);

        g.setTransform(oldTransform);
    }

    @Override
    public void destroy(Scene origin) {
        super.destroyTheRest(origin);
        onActionCondition = null;
        sliderPosition = 0f;
        origin.inputManager.removeMouseActionListener(this);
    }

    @Override
    public void destroy(SimpleManager origin) {
        super.destroyTheRest(origin);
        onActionCondition = null;
        sliderPosition = 0f;
        origin.inputManager.removeMouseActionListener(this);
    }
}
