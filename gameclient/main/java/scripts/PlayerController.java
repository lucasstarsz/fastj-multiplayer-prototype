package scripts;

import tech.fastj.engine.FastJEngine;
import tech.fastj.math.Pointf;
import tech.fastj.graphics.game.GameObject;

import tech.fastj.input.InputManager;
import tech.fastj.input.keyboard.KeyboardActionListener;
import tech.fastj.input.keyboard.Keys;
import tech.fastj.input.keyboard.events.KeyboardStateEvent;
import tech.fastj.input.mouse.MouseActionListener;
import tech.fastj.systems.behaviors.Behavior;

import java.io.IOException;

import network.client.Client;
import core.util.Networking;

public class PlayerController implements Behavior {

    public static final float MovementSpeed = 5f;
    public static final float RotationSpeed = 5f;

    private final InputManager inputManager;
    private final Runnable playerObserver;
    private final int playerNumber;
    private final Client client;

    private KeyboardActionListener keyListener;
    private MouseActionListener mouseListener;
    private Pointf movement;
    private float rotation;

    public PlayerController(Runnable playerObserver, InputManager inputManager, Client client, int playerNumber) {
        this.inputManager = inputManager;
        this.playerObserver = playerObserver;
        this.client = client;
        this.playerNumber = playerNumber;
    }

    @Override
    public void init(GameObject player) {
        movement = Pointf.origin();
        rotation = 0f;

        keyListener = new KeyboardActionListener() {

            @Override
            public void onKeyRecentlyPressed(KeyboardStateEvent keyboardStateEvent) {
                Keys key = keyboardStateEvent.getKey();
                if (key == Keys.W || key == Keys.A || key == Keys.S || key == Keys.D) {
                    try {
                        client.send(Networking.Server.KeyPress, playerNumber, keyboardStateEvent.getKey().name());

                        switch (keyboardStateEvent.getKey()) {
                            case W -> movement.y -= MovementSpeed;
                            case A -> rotation -= RotationSpeed;
                            case S -> movement.y += MovementSpeed;
                            case D -> rotation += RotationSpeed;
                        }
                    } catch (IOException exception) {
                        FastJEngine.error("IO error", exception);
                    }
                }
            }

            @Override
            public void onKeyReleased(KeyboardStateEvent keyboardStateEvent) {
                Keys key = keyboardStateEvent.getKey();
                if (key == Keys.W || key == Keys.A || key == Keys.S || key == Keys.D) {
                    try {
                        client.send(Networking.Server.KeyRelease, playerNumber, keyboardStateEvent.getKey().name());

                        switch (keyboardStateEvent.getKey()) {
                            case W -> movement.y += MovementSpeed;
                            case A -> rotation += RotationSpeed;
                            case S -> movement.y -= MovementSpeed;
                            case D -> rotation -= RotationSpeed;
                        }
                    } catch (IOException exception) {
                        FastJEngine.error("IO error", exception);
                    }
                }
            }
        };

        inputManager.addKeyboardActionListener(keyListener);

//        mouseListener = new MouseActionListener() {
//            @Override
//            public void onMouseMoved(MouseMotionEvent mouseMotionEvent) {
//                Pointf mousePosition = mouseMotionEvent.getMouseLocation();
//                Pointf playerCenter = player.getCenter();
//                Pointf currentDirection = Pointf.up()
//                        .rotate(-player.getRotationWithin360())
//                        .add(playerCenter);
//
//                double distanceA = Pointf.distance(currentDirection, mousePosition);
//                double distanceB = Pointf.distance(mousePosition, playerCenter);
//                double distanceC = Pointf.distance(playerCenter, currentDirection);
//                double distanceASquared = distanceA * distanceA;
//                double distanceBSquared = distanceB * distanceB;
//                double distanceCSquared = distanceC * distanceC;
//                float angle = (float) Math.toDegrees(Math.acos(
//                        (distanceBSquared + distanceCSquared - distanceASquared) / (2 * distanceB * distanceC)
//                ));
//
//                if (!Float.isNaN(angle)) {
//                    Pointf newDirection = Pointf.up().rotate(-player.getRotationWithin360() + angle).add(playerCenter);
//                    Pointf oppositeNewDirection = Pointf.up().rotate(-player.getRotationWithin360() - angle).add(playerCenter);
//                    double newDistance = Pointf.distance(newDirection, mousePosition);
//                    double oppositeNewDistance = Pointf.distance(oppositeNewDirection, mousePosition);
//
//                    if (newDistance > oppositeNewDistance) {
//                        player.setRotation((player.getRotationWithin360() + angle) % 360);
//                    } else {
//                        player.setRotation((player.getRotationWithin360() - angle) % 360);
//                    }
//                }
//            }
//        };
//
//        inputManager.addMouseActionListener(mouseListener);
    }

    public static boolean transformPlayer(Pointf movement, float rotation, GameObject player) {
        if (!movement.equals(Pointf.origin()) || rotation != 0f) {
            Pointf rotatedMovement = new Pointf(movement).rotate(-(player.getRotationWithin360() + rotation));
            player.rotate(rotation);
            player.translate(rotatedMovement);

            return true;
        }

        return false;
    }

    @Override
    public void update(GameObject player) {
        if (transformPlayer(movement, rotation, player)) {
            playerObserver.run();
        }
    }

    @Override
    public void destroy() {
        inputManager.removeKeyboardActionListener(keyListener);
        keyListener = null;
        mouseListener = null;
        movement = null;
        rotation = 0f;
    }
}
