package ui;

import tech.fastj.engine.FastJEngine;
import tech.fastj.logging.Log;
import tech.fastj.math.Pointf;
import tech.fastj.graphics.display.Camera;
import tech.fastj.graphics.game.Polygon2D;
import tech.fastj.graphics.game.RenderStyle;
import tech.fastj.graphics.ui.EventCondition;
import tech.fastj.graphics.ui.UIElement;
import tech.fastj.graphics.util.DrawUtil;

import tech.fastj.input.mouse.Mouse;
import tech.fastj.input.mouse.MouseAction;
import tech.fastj.input.mouse.MouseButtons;
import tech.fastj.input.mouse.events.MouseButtonEvent;
import tech.fastj.systems.control.Scene;
import tech.fastj.systems.control.SimpleManager;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import util.Colors;

public class ArrowedButton extends UIElement {

    public static final String DefaultText = "";
    public static final Paint DefaultFill = Color.lightGray;
    public static final Font DefaultFont = new Font("Tahoma", Font.PLAIN, 16);

    private final List<String> options;
    private int selectedOption;
    private final Polygon2D arrowLeft, arrowRight;

    private Paint paint;
    private Path2D.Float renderPath;

    private Font font;
    private String text;
    private Rectangle2D.Float textBounds;
    private boolean hasMetrics;
    private final EventCondition eventCondition = event -> (
            Mouse.interactsWith(ArrowedButton.this.arrowLeft, MouseAction.Press) || Mouse.interactsWith(ArrowedButton.this.arrowRight, MouseAction.Press)
    ) && Mouse.isMouseButtonPressed(MouseButtons.Left);

    public ArrowedButton(Scene origin, Pointf location, Pointf initialSize, List<String> options, int selectedOption) {
        super(origin);
        super.setOnActionCondition(eventCondition);

        Pointf[] buttonCoords = DrawUtil.createBox(Pointf.origin(), initialSize);
        renderPath = DrawUtil.createPath(buttonCoords);
        super.setCollisionPath(renderPath);

        Pointf[] arrowLeftCoords = {
                new Pointf(location.x + initialSize.x / 9.5f, (buttonCoords[3].y - buttonCoords[0].y) / 2f),
                new Pointf((location.x + initialSize.x / 5f), buttonCoords[1].y + (buttonCoords[2].y - buttonCoords[1].y) / 4f),
                new Pointf((location.x + initialSize.x / 5f), buttonCoords[2].y - (buttonCoords[2].y - buttonCoords[1].y) / 4f)
        };
        arrowLeft = Polygon2D.create(arrowLeftCoords, RenderStyle.FillAndOutline)
                .withFill(Colors.Snowy.darker())
                .withOutline(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), Color.black)
                .build();
        Pointf[] arrowRightCoords = {
                new Pointf(location.x + initialSize.x * .9f, (buttonCoords[3].y - buttonCoords[0].y) / 2f),
                new Pointf(location.x + initialSize.x - (location.x + initialSize.x) / 5f, buttonCoords[1].y + (buttonCoords[2].y - buttonCoords[1].y) / 4f),
                new Pointf(location.x + initialSize.x - (location.x + initialSize.x) / 5f, buttonCoords[2].y - (buttonCoords[2].y - buttonCoords[1].y) / 4f)
        };
        arrowRight = Polygon2D.create(arrowRightCoords, RenderStyle.FillAndOutline)
                .withFill(Colors.Snowy.darker())
                .withOutline(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), Color.black)
                .build();
        Log.info("{} {}", arrowLeft, arrowRight);

        this.paint = DefaultFill;
        this.font = DefaultFont;
        this.text = DefaultText;

        translate(location);
        setMetrics(FastJEngine.getCanvas().getGraphics());
        this.options = options;
        this.selectedOption = selectedOption;
        setText(options.get(selectedOption));
    }

    public ArrowedButton(SimpleManager origin, Pointf location, Pointf initialSize, List<String> options, int selectedOption) {
        super(origin);
        super.setOnActionCondition(eventCondition);

        Pointf[] buttonCoords = DrawUtil.createBox(Pointf.origin(), initialSize);
        renderPath = DrawUtil.createPath(buttonCoords);
        super.setCollisionPath(renderPath);

        Pointf[] arrowLeftCoords = {
                new Pointf(location.x + initialSize.x / 9.5f, (buttonCoords[3].y - buttonCoords[0].y) / 2f),
                new Pointf((location.x + initialSize.x / 5f), buttonCoords[1].y + (buttonCoords[2].y - buttonCoords[1].y) / 4f),
                new Pointf((location.x + initialSize.x / 5f), buttonCoords[2].y - (buttonCoords[2].y - buttonCoords[1].y) / 4f)
        };
        arrowLeft = Polygon2D.create(arrowLeftCoords, RenderStyle.FillAndOutline)
                .withFill(Colors.Snowy)
                .withOutline(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), Color.black)
                .build();
        Pointf[] arrowRightCoords = {
                new Pointf(location.x + initialSize.x * .9f, (buttonCoords[3].y - buttonCoords[0].y) / 2f),
                new Pointf(location.x + initialSize.x - (location.x + initialSize.x) / 5f, buttonCoords[1].y + (buttonCoords[2].y - buttonCoords[1].y) / 4f),
                new Pointf(location.x + initialSize.x - (location.x + initialSize.x) / 5f, buttonCoords[2].y - (buttonCoords[2].y - buttonCoords[1].y) / 4f)
        };
        arrowRight = Polygon2D.create(arrowRightCoords, RenderStyle.FillAndOutline)
                .withFill(Colors.Snowy)
                .withOutline(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), Color.black)
                .build();
        Log.info("{} {}", arrowLeft, arrowRight);

        this.paint = DefaultFill;
        this.font = DefaultFont;
        this.text = DefaultText;

        translate(location);
        setMetrics(FastJEngine.getCanvas().getGraphics());
        this.options = options;
        this.selectedOption = selectedOption;
        setText(options.get(selectedOption));
    }

    @Override
    public void translate(Pointf translationMod) {
        super.translate(translationMod);
        arrowLeft.translate(translationMod);
        arrowRight.translate(translationMod);
    }

    public Paint getFill() {
        return paint;
    }

    public ArrowedButton setFill(Paint paint) {
        this.paint = paint;
        return this;
    }

    public String getText() {
        return text;
    }

    public ArrowedButton setText(String text) {
        this.text = text;
        setMetrics(FastJEngine.getCanvas().getGraphics());
        return this;
    }

    public Font getFont() {
        return font;
    }

    public ArrowedButton setFont(Font font) {
        this.font = font;
        setMetrics(FastJEngine.getCanvas().getGraphics());
        return this;
    }

    public int getSelectedOption() {
        return selectedOption;
    }

    @Override
    public void onMousePressed(MouseButtonEvent mouseButtonEvent) {
        if (!eventCondition.condition(mouseButtonEvent)) {
            return;
        }
        if (mouseButtonEvent.getMouseButton() != MouseEvent.BUTTON1) {
            return;
        }

        Pointf mouseLocation = Mouse.getMouseLocation();
        if (mouseLocation.intersects(arrowLeft.getCollisionPath())) {
            selectedOption--;
            if (selectedOption < 0) {
                selectedOption += options.size();
            }
        } else if (mouseLocation.intersects(arrowRight.getCollisionPath())) {
            selectedOption++;
            if (selectedOption >= options.size()) {
                selectedOption -= options.size();
            }
        }

        setText(options.get(selectedOption));
        super.onMousePressed(mouseButtonEvent);
    }

    @Override
    public void renderAsGUIObject(Graphics2D g, Camera camera) {
        if (!shouldRender()) {
            return;
        }

        AffineTransform oldTransform = (AffineTransform) g.getTransform().clone();
        Paint oldPaint = g.getPaint();
        Font oldFont = g.getFont();

        g.transform(getTransformation());

        try {
            g.transform(camera.getTransformation().createInverse());
        } catch (NoninvertibleTransformException exception) {
            throw new IllegalStateException(
                    "Couldn't create an inverse transform of " + camera.getTransformation(),
                    exception
            );
        }

        Rectangle2D.Float renderCopy = (Rectangle2D.Float) renderPath.getBounds2D();

        g.setPaint(paint);
        g.fill(renderCopy);
        g.setPaint(Color.black);
        g.draw(renderCopy);

        if (!hasMetrics) {
            setMetrics(g);
        }

        g.setFont(font);
        g.drawString(text, textBounds.x, textBounds.y * 1.5f);

        g.setPaint(oldPaint);
        g.setFont(oldFont);
        g.setTransform(oldTransform);

        arrowLeft.render(g);
        arrowRight.render(g);

        g.setPaint(oldPaint);
        g.setFont(oldFont);
        g.setTransform(oldTransform);
    }

    @Override
    public void destroy(Scene origin) {
        paint = null;
        renderPath = null;
        super.destroyTheRest(origin);
    }

    @Override
    public void destroy(SimpleManager origin) {
        paint = null;
        renderPath = null;
        super.destroyTheRest(origin);
    }

    private void setMetrics(Graphics2D g) {
        hasMetrics = false;

        FontMetrics fm = g.getFontMetrics(font);

        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();
        Rectangle2D.Float renderPathBounds = (Rectangle2D.Float) renderPath.getBounds2D();

        textBounds = new Rectangle2D.Float(
                (renderPathBounds.width - textWidth) / 2f,
                textHeight,
                textWidth,
                textHeight
        );

        if (renderPathBounds.width < textBounds.width) {
            float diff = (textBounds.width - renderPathBounds.width) / 2f;
            renderPathBounds.width = textBounds.width;
            textBounds.x += diff;
        }

        if (renderPathBounds.height < textBounds.height) {
            renderPathBounds.height = textBounds.height;
        }

        g.dispose();
        hasMetrics = true;
    }
}
