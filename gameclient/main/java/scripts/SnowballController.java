package scripts;

import tech.fastj.engine.FastJEngine;
import tech.fastj.logging.Log;
import tech.fastj.math.Pointf;
import tech.fastj.graphics.game.GameObject;

import tech.fastj.input.keyboard.KeyboardActionListener;
import tech.fastj.input.keyboard.Keys;
import tech.fastj.input.keyboard.events.KeyboardStateEvent;
import tech.fastj.systems.behaviors.Behavior;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import core.util.Networking;
import network.client.Client;
import objects.Player;
import scenes.GameScene;

public class SnowballController implements Behavior {

    public static final int MaxSnowballsCarried = 5;
    private static final int SnowballMakeCooldown = 1000;
    private static final int SnowballThrowCooldown = 500;

    private final GameScene scene;
    private final Client client;

    private ScheduledExecutorService cooldownManager;
    private int snowballCount;
    private volatile boolean currentSnowballMakeCooldown;
    private volatile boolean currentSnowballThrowCooldown;
    private KeyboardActionListener keyListener;
    private final Runnable snowballUpdater;

    public SnowballController(GameScene scene, Client client, Runnable snowballUpdate) {
        this.scene = scene;
        this.client = client;
        this.snowballUpdater = snowballUpdate;
    }

    @Override
    public void init(GameObject gameObject) {
        this.snowballCount = 0;
        this.currentSnowballMakeCooldown = true;
        this.currentSnowballThrowCooldown = true;
        snowballUpdater.run();
        this.cooldownManager = Executors.newScheduledThreadPool(2);

        keyListener = new KeyboardActionListener() {
            @Override
            public void onKeyRecentlyPressed(KeyboardStateEvent keyboardStateEvent) {
                if (scene.isPlayerDead()) {
                    return;
                }

                if (gameObject instanceof Player player) {
                    if (keyboardStateEvent.getKey() == Keys.Space) {
                        if (!currentSnowballThrowCooldown) {
                            Log.debug(GameScene.class, "player {} is still on cooldown.", player.getPlayerNumber());
                            return;
                        }

                        if (snowballCount < 1) {
                            Log.debug("player {} doesn't have any snowballs to throw!", player.getPlayerNumber());
                            return;
                        }

                        FastJEngine.runAfterUpdate(() -> {
                            try {
                                float playerRotation = player.getRotationWithin360();
                                Pointf trajectory = Pointf.up().rotate(-playerRotation);
                                client.send(Networking.Server.CreateSnowball, player.getPlayerNumber(), trajectory.x, trajectory.y, playerRotation);
                                scene.spawnSnowball(player, trajectory, playerRotation);
                                snowballCount--;
                                snowballThrowCooldown();
                                snowballUpdater.run();
                            } catch (IOException exception) {
                                FastJEngine.error("IO error", exception);
                            }
                        });
                    }

                    if (keyboardStateEvent.getKey() == Keys.R) {
                        if (!currentSnowballMakeCooldown) {
                            Log.debug(GameScene.class, "player {} is still on cooldown.", player.getPlayerNumber());
                            return;
                        }

                        createSnowball(player);
                    }
                }
            }
        };
        scene.inputManager.addKeyboardActionListener(keyListener);
    }

    private void snowballMakeCooldown() {
        currentSnowballMakeCooldown = false;
        cooldownManager.schedule(
                () -> {
                    currentSnowballMakeCooldown = true;
                    snowballUpdater.run();
                },
                SnowballMakeCooldown,
                TimeUnit.MILLISECONDS
        );
    }

    private void snowballThrowCooldown() {
        currentSnowballThrowCooldown = false;
        cooldownManager.schedule(
                () -> {
                    currentSnowballThrowCooldown = true;
                    snowballUpdater.run();
                },
                SnowballThrowCooldown,
                TimeUnit.MILLISECONDS
        );
    }

    private void createSnowball(Player player) {
        if (snowballCount == MaxSnowballsCarried) {
            Log.debug(GameScene.class, "player {} can't carry more than {} snowballs!", player.getPlayerNumber(), MaxSnowballsCarried);
            return;
        }

        snowballCount++;
        snowballMakeCooldown();
        snowballUpdater.run();
        Log.debug(GameScene.class, "player {} created a snowball.", player.getPlayerNumber());
    }

    public int getSnowballCount() {
        return snowballCount;
    }

    public boolean isSnowballMakeReady() {
        return currentSnowballMakeCooldown;
    }

    public boolean isSnowballThrowReady() {
        return currentSnowballThrowCooldown;
    }

    @Override
    public void update(GameObject gameObject) {
    }

    @Override
    public void destroy() {
        scene.inputManager.removeKeyboardActionListener(keyListener);
        if (cooldownManager != null) {
            cooldownManager.shutdownNow();
            cooldownManager = null;
        }
        keyListener = null;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        SnowballController snowballController = (SnowballController) other;
        return snowballCount == snowballController.snowballCount
                && currentSnowballMakeCooldown == snowballController.currentSnowballMakeCooldown
                && currentSnowballThrowCooldown == snowballController.currentSnowballThrowCooldown
                && scene.equals(snowballController.scene)
                && client.equals(snowballController.client)
                && Objects.equals(cooldownManager, snowballController.cooldownManager)
                && Objects.equals(keyListener, snowballController.keyListener);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scene, client, cooldownManager, snowballCount, currentSnowballMakeCooldown, currentSnowballThrowCooldown, keyListener);
    }
}
