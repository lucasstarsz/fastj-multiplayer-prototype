package objects;

import tech.fastj.math.Pointf;
import tech.fastj.graphics.Boundary;
import tech.fastj.graphics.Drawable;
import tech.fastj.graphics.game.GameObject;
import tech.fastj.graphics.game.Model2D;
import tech.fastj.graphics.game.Polygon2D;
import tech.fastj.graphics.game.Text2D;

import tech.fastj.resources.models.ModelUtil;
import tech.fastj.systems.control.Scene;
import tech.fastj.systems.control.SimpleManager;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import util.FilePaths;
import util.Fonts;

public class Player extends GameObject {

    private final Model2D playerModel;
    private final Model2D directionalArrow;
    private final Text2D playerIndicator;
    private final int playerNumber;
    private final boolean isLocalPlayer;
    private final Pointf baseModelTranslation;
    private final Pointf baseIndicatorTranslation;

    public Player(Model2D playerModel, int playerNumber, boolean isLocalPlayer) {
        this.playerModel = playerModel;
        baseModelTranslation = this.playerModel.getTranslation();
        this.playerNumber = playerNumber;
        this.isLocalPlayer = isLocalPlayer;
        this.playerIndicator = Text2D.create(this.isLocalPlayer ? "You" : "P" + this.playerNumber)
                .withFont(Fonts.DefaultNotoSans)
                .build();

        Polygon2D[] directionalArrowMesh = ModelUtil.loadModel(FilePaths.PlayerArrow);
        directionalArrowMesh[0].setFill(playerModel.getPolygons()[0].getFill());
        this.directionalArrow = Model2D.fromPolygons(directionalArrowMesh);

        super.setCollisionPath(this.playerModel.getCollisionPath());

        Pointf playerIndicatorWidth = Pointf.subtract(
                playerIndicator.getBound(Boundary.TopRight),
                playerIndicator.getBound(Boundary.TopLeft)
        );
        Pointf playerModelHeight = Pointf.subtract(
                this.playerModel.getBound(Boundary.BottomLeft),
                this.playerModel.getBound(Boundary.TopLeft)
        );
        baseIndicatorTranslation = this.playerModel.getCenter()
                .add(Pointf.divide(playerIndicatorWidth, 2f))
                .subtract(playerIndicatorWidth.x, playerModelHeight.y / 5f);
        playerIndicator.setTranslation(baseIndicatorTranslation);
    }

    @Override
    public void translate(Pointf translationMod) {
        super.translate(translationMod);
        playerIndicator.translate(translationMod);
    }

    @Override
    public Drawable setTranslation(Pointf setTranslation) {
        super.setTranslation(setTranslation);
        playerModel.setTranslation(baseModelTranslation);
        playerIndicator.setTranslation(Pointf.add(setTranslation, baseIndicatorTranslation));
        return this;
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    public boolean isLocalPlayer() {
        return isLocalPlayer;
    }

    @Override
    public void render(Graphics2D g) {
        if (!shouldRender()) {
            return;
        }

        AffineTransform oldTransform = (AffineTransform) g.getTransform().clone();
        AffineTransform oldTransform2 = (AffineTransform) g.getTransform().clone();
        g.transform(getTransformation());
        directionalArrow.render(g);
        playerModel.render(g);
        g.setTransform(oldTransform);
        g.translate(playerModel.getTranslation().x, playerModel.getTranslation().y);
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
