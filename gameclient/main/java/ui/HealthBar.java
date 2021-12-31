package ui;

import tech.fastj.math.Pointf;
import tech.fastj.graphics.Boundary;
import tech.fastj.graphics.display.Camera;
import tech.fastj.graphics.game.Model2D;
import tech.fastj.graphics.game.Polygon2D;
import tech.fastj.graphics.game.Text2D;
import tech.fastj.graphics.ui.UIElement;
import tech.fastj.graphics.util.DrawUtil;

import tech.fastj.input.mouse.events.MouseButtonEvent;
import tech.fastj.resources.models.ModelUtil;
import tech.fastj.systems.control.Scene;
import tech.fastj.systems.control.SimpleManager;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;

import util.FilePaths;

public class HealthBar extends UIElement {

    private final int maxHealth;
    private int healthRemaining;
    private final Model2D healthModel;
    private final Text2D healthText;

    public HealthBar(Scene origin, Color healthColor, int healthRemaining) {
        super(origin);
        this.maxHealth = healthRemaining;
        this.healthRemaining = maxHealth;

        this.healthModel = Model2D.fromPolygons(ModelUtil.loadModel(FilePaths.HealthBar));
        this.healthModel.getPolygons()[1].setFill(healthColor);
        this.healthText = Text2D.fromText(String.format("%d/%d", healthRemaining, 100));
        this.healthText.translate(new Pointf(50f, 15f).subtract(healthText.getCenter()));

        super.setCollisionPath(healthModel.getCollisionPath());
    }

    public HealthBar(SimpleManager origin, Color healthColor, int healthRemaining) {
        super(origin);
        this.maxHealth = healthRemaining;
        this.healthRemaining = maxHealth;

        this.healthModel = Model2D.fromPolygons(ModelUtil.loadModel(FilePaths.HealthBar));
        this.healthModel.getPolygons()[1].setFill(healthColor);
        this.healthText = Text2D.fromText(String.format("%d/%d", healthRemaining, 100));
        this.healthText.translate(new Pointf(50f, 15f).subtract(healthText.getCenter()));

        super.setCollisionPath(healthModel.getCollisionPath());
    }

    public Model2D getHealthModel() {
        return healthModel;
    }

    public int getHealthRemaining() {
        return healthRemaining;
    }

    public boolean modifyHealthRemaining(int healthModifier) {
        healthRemaining = Math.max(healthModifier + healthRemaining, 0);

        Polygon2D health = healthModel.getPolygons()[1];
        Pointf[] newHealthMesh = DrawUtil.createBox(health.getBound(Boundary.TopLeft), new Pointf(healthRemaining, 29f));
        health.modifyPoints(newHealthMesh, false, false, false);

        return healthRemaining == 0;
    }

    @Override
    public void onMousePressed(MouseButtonEvent mouseButtonEvent) {
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

        healthModel.render(g);

        g.setTransform(oldTransform);
    }

    @Override
    public void destroy(Scene origin) {
        super.destroyTheRest(origin);
    }

    @Override
    public void destroy(SimpleManager origin) {
        super.destroyTheRest(origin);
    }
}
