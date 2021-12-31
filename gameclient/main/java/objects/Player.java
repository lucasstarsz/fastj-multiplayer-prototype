package objects;

import tech.fastj.math.Pointf;
import tech.fastj.graphics.game.GameObject;
import tech.fastj.graphics.game.Model2D;
import tech.fastj.graphics.game.Polygon2D;
import tech.fastj.graphics.game.Text2D;

import tech.fastj.resources.models.ModelUtil;
import tech.fastj.systems.control.Scene;
import tech.fastj.systems.control.SimpleManager;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import util.FilePaths;
import util.Fonts;

public class Player extends GameObject {

    private final Model2D playerModel;
    private final Model2D directionalArrow;
    private final Text2D playerIndicator;

    public Player(Model2D playerModel, int playerNumber, boolean isLocalPlayer) {
        this.playerModel = playerModel;
        this.playerIndicator = Text2D.create(isLocalPlayer ? "You" : "P" + playerNumber)
                .withFont(Fonts.DefaultNotoSans)
                .build();
        playerIndicator.translate(new Pointf(25f).subtract(playerIndicator.getCenter()));

        Polygon2D[] directionalArrowMesh = ModelUtil.loadModel(FilePaths.PlayerArrow);
        directionalArrowMesh[0].setFill(((Color) playerModel.getPolygons()[0].getFill()).brighter().brighter().brighter());
        this.directionalArrow = Model2D.fromPolygons(directionalArrowMesh);

        super.setCollisionPath(this.playerModel.getCollisionPath());
    }

    @Override
    public void render(Graphics2D g) {
        if (!shouldRender()) {
            return;
        }

        AffineTransform oldTransform2 = (AffineTransform) g.getTransform().clone();
        g.transform(getTransformation());

        directionalArrow.render(g);
        playerModel.render(g);
        playerIndicator.render(g);

        g.setTransform(oldTransform2);
    }

    @Override
    public void destroy(Scene origin) {
        playerModel.destroy(origin);
        playerIndicator.destroy(origin);
    }

    @Override
    public void destroy(SimpleManager origin) {
        playerModel.destroy(origin);
        playerIndicator.destroy(origin);
    }
}
