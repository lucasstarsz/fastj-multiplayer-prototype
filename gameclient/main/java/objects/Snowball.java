package objects;

import tech.fastj.engine.FastJEngine;
import tech.fastj.logging.Log;
import tech.fastj.math.Maths;
import tech.fastj.math.Pointf;
import tech.fastj.graphics.Drawable;
import tech.fastj.graphics.game.GameObject;
import tech.fastj.graphics.game.Model2D;

import tech.fastj.resources.models.ModelUtil;
import tech.fastj.systems.behaviors.Behavior;
import tech.fastj.systems.control.Scene;
import tech.fastj.systems.control.SimpleManager;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import scene.GameScene;
import util.FilePaths;
import util.Tags;

public class Snowball extends GameObject implements Behavior {

    public static final int SnowballTempDamage = 10;
    public static final int SnowballHitDamage = 15;
    private static final float MinDistance = 700f;
    private static final float MaxDistance = 1000f;
    private static final float TravelSpeed = 25f;

    private final GameScene scene;
    private final Pointf travelMovement;
    private final Model2D snowballModel;
    private final int playerNumber;

    private float life;

    public Snowball(Pointf trajectory, float rotation, Player player, GameScene scene) {
        snowballModel = Model2D.fromPolygons(ModelUtil.loadModel(FilePaths.Snowball));

        this.life = Maths.random(MinDistance, MaxDistance);
        this.travelMovement = trajectory.multiply(TravelSpeed);
        this.playerNumber = player.getPlayerNumber();

        this.scene = scene;
        super.setCollisionPath(snowballModel.getCollisionPath());
        rotate(rotation);
        translate(player.getCenter());
        Log.debug("Created snowball moving at a trajectory of {} with life starting at {}", trajectory, life);
    }

    @Override
    public void init(GameObject gameObject) {
    }

    @Override
    public void update(GameObject gameObject) {
        if (life <= 0f) {
            return;
        }

        translate(travelMovement);

        Drawable player = scene.getAllWithTag(Tags.LocalPlayer).get(0);
        if (player instanceof Player localPlayer && playerNumber != localPlayer.getPlayerNumber()) {
            if (localPlayer.collidesWith(this)) {
                FastJEngine.runAfterUpdate(() -> this.destroy(scene));
                if (scene.isPlayerDead()) {
                    return;
                }
                Log.info(Snowball.class, "Snowball from player {} hit player {}", playerNumber, scene.getLocalPlayerNumber());
                scene.playerTakeSnowballDamage(playerNumber);
            }
        }

        life -= Math.abs(travelMovement.x) + Math.abs(travelMovement.y);
        if (life <= 0f) {
            FastJEngine.runAfterUpdate(() -> destroy(scene));
        }
    }

    @Override
    public void render(Graphics2D g) {
        if (!shouldRender()) {
            return;
        }

        AffineTransform oldTransform2 = (AffineTransform) g.getTransform().clone();
        g.transform(getTransformation());

        snowballModel.render(g);

        g.setTransform(oldTransform2);
    }

    @Override
    public void destroy(Scene origin) {
        super.destroyTheRest(origin);
        life = 0f;
    }

    @Override
    public void destroy(SimpleManager origin) {
        super.destroyTheRest(origin);
        life = 0f;
    }
}
