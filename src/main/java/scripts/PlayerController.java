package scripts;

import tech.fastj.engine.FastJEngine;
import tech.fastj.math.Pointf;
import tech.fastj.graphics.game.GameObject;

import tech.fastj.input.InputManager;
import tech.fastj.input.keyboard.Keyboard;
import tech.fastj.input.keyboard.KeyboardActionListener;
import tech.fastj.input.keyboard.Keys;
import tech.fastj.input.mouse.MouseActionListener;
import tech.fastj.input.mouse.events.MouseMotionEvent;
import tech.fastj.systems.behaviors.Behavior;

public class PlayerController implements Behavior {

    private final InputManager inputManager;
    private final Runnable playerObserver;

    private KeyboardActionListener keyListener;
    private MouseActionListener mouseListener;
    private Pointf movement;
    private float movementSpeed;
    private float rotation;
    private float rotationSpeed;

    public PlayerController(Runnable playerObserver, InputManager inputManager) {
        this.rotationSpeed = 3f;
        this.movementSpeed = 5f;
        this.inputManager = inputManager;
        this.playerObserver = playerObserver;
    }

    @Override
    public void init(GameObject player) {
        movement = Pointf.origin();
        rotation = 0f;

        keyListener = new KeyboardActionListener() {
            @Override
            public void onKeyDown() {
                movement.reset();
                rotation = 0f;

                if (Keyboard.isKeyDown(Keys.W)) {
                    movement.y -= movementSpeed;
                }
                if (Keyboard.isKeyDown(Keys.S)) {
                    movement.y += movementSpeed;
                }
                if (Keyboard.isKeyDown(Keys.A)) {
                    rotation -= rotationSpeed;
                }
                if (Keyboard.isKeyDown(Keys.D)) {
                    rotation += rotationSpeed;
                }

                if (!movement.equals(Pointf.origin()) || rotation != 0f) {
                    movement.rotate(-(player.getRotationWithin360() + rotation));
                    player.rotate(rotation);
                    player.translate(movement);

                    playerObserver.run();
                }
            }
        };

        inputManager.addKeyboardActionListener(keyListener);

        mouseListener = new MouseActionListener() {
            @Override
            public void onMouseMoved(MouseMotionEvent mouseMotionEvent) {
                Pointf mousePosition = mouseMotionEvent.getMouseLocation();
                Pointf playerCenter = player.getCenter();
                Pointf currentDirection = Pointf.up()
                        .rotate(-player.getRotationWithin360())
                        .add(playerCenter);

                double distanceA = Pointf.distance(currentDirection, mousePosition);
                double distanceB = Pointf.distance(mousePosition, playerCenter);
                double distanceC = Pointf.distance(playerCenter, currentDirection);
                double distanceASquared = distanceA * distanceA;
                double distanceBSquared = distanceB * distanceB;
                double distanceCSquared = distanceC * distanceC;
                float angle = (float) Math.toDegrees(Math.acos(
                        (distanceBSquared + distanceCSquared - distanceASquared) / (2 * distanceB * distanceC)
                ));

                if (!Float.isNaN(angle)) {
                    Pointf newDirection = Pointf.up().rotate(-player.getRotationWithin360() + angle).add(playerCenter);
                    Pointf oppositeNewDirection = Pointf.up().rotate(-player.getRotationWithin360() - angle).add(playerCenter);
                    double newDistance = Pointf.distance(newDirection, mousePosition);
                    double oppositeNewDistance = Pointf.distance(oppositeNewDirection, mousePosition);

                    if (newDistance > oppositeNewDistance) {
                        FastJEngine.log("Rotate {} degrees right", angle);
                        player.setRotation((player.getRotationWithin360() + angle) % 360);
                    } else {
                        FastJEngine.log("Rotate {} degrees left", angle);
                        player.setRotation((player.getRotationWithin360() - angle) % 360);
                    }
                }
            }
        };

        inputManager.addMouseActionListener(mouseListener);
    }

    @Override
    public void update(GameObject player) {
    }

    @Override
    public void destroy() {
        inputManager.removeKeyboardActionListener(keyListener);
        keyListener = null;
        mouseListener = null;
        movement = null;
        movementSpeed = 0f;
        rotation = 0f;
        rotationSpeed = 0f;
    }
}
