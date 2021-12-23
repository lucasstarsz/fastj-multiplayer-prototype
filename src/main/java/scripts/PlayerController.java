package scripts;

import tech.fastj.math.Pointf;
import tech.fastj.graphics.game.GameObject;

import tech.fastj.input.InputManager;
import tech.fastj.input.keyboard.Keyboard;
import tech.fastj.input.keyboard.KeyboardActionListener;
import tech.fastj.input.keyboard.Keys;
import tech.fastj.systems.behaviors.Behavior;

public class PlayerController implements Behavior {

    private final InputManager inputManager;
    private final Runnable playerObserver;

    private KeyboardActionListener keyListener;
    private Pointf movement;
    private float movementSpeed;
    private float rotation;
    private float rotationSpeed;

    public PlayerController(Runnable playerObserver, InputManager inputManager) {
        this.rotationSpeed = 3f;
        this.movementSpeed = 3f;
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

                movement.rotate(-(player.getRotationWithin360() + rotation));
                player.rotate(rotation);
                player.translate(movement);

                playerObserver.run();
            }
        };

        inputManager.addKeyboardActionListener(keyListener);
    }

    @Override
    public void update(GameObject player) {
    }

    @Override
    public void destroy() {
        inputManager.removeKeyboardActionListener(keyListener);
        keyListener = null;
        movement = null;
        movementSpeed = 0f;
        rotation = 0f;
        rotationSpeed = 0f;
    }
}
