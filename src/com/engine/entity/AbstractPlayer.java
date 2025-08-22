package com.engine.entity;

import com.engine.event.DefaultKeyListener;
import com.engine.AbstractGame;
import com.engine.animation.Direction;
import com.engine.event.Listener;

import java.awt.*;
import java.awt.event.KeyEvent;

public abstract class AbstractPlayer extends AbstractEntity implements Player {
    // KeyListener handles all events.
    private final PlayerKeyListener mainKeyListener = new PlayerKeyListener();

    /**
     * Constructor with context.
     * Adds some keyListeners by default.
     * @param context context to pass to super
     */
    public AbstractPlayer(AbstractGame context) {
        super(context);
        addListeners();
    }

    protected void addListeners() {
        context.getKeyListener(DefaultKeyListener.class)
                // UP
                .addListener(KeyEvent.VK_W, mainKeyListener)
                .addListener(KeyEvent.VK_UP, mainKeyListener)
                .addListener(KeyEvent.VK_KP_UP, mainKeyListener)
                // LEFT
                .addListener(KeyEvent.VK_A, mainKeyListener)
                .addListener(KeyEvent.VK_LEFT, mainKeyListener)
                .addListener(KeyEvent.VK_KP_LEFT, mainKeyListener)
                // RIGHT
                .addListener(KeyEvent.VK_D, mainKeyListener)
                .addListener(KeyEvent.VK_RIGHT, mainKeyListener)
                .addListener(KeyEvent.VK_KP_RIGHT, mainKeyListener)
                // DOWN
                .addListener(KeyEvent.VK_S, mainKeyListener)
                .addListener(KeyEvent.VK_DOWN, mainKeyListener)
                .addListener(KeyEvent.VK_KP_DOWN, mainKeyListener);
    }

    /**
     * Updates entity to the next frame.
     * If entity is not static physics, animation, direction e.t.c. gets updated,
     * if not, nothing gets updated except hitbox.
     */
    @Override
    public void update() {
        super.update();
        final int SPEED=getCurrentSpeed();
        Point steps=new Point();
        if(mainKeyListener.left) steps.x -= SPEED;
        else if (mainKeyListener.right) steps.x += SPEED;
        else if (mainKeyListener.up) steps.y -= SPEED;
        else if (mainKeyListener.down) steps.y += SPEED;
        moveSafely(steps.x, steps.y);
    }

    /**
     * Damages entity, and decreases its hp;
     * Applies given knockback to this to the given direction.
     *
     * @param knockbackDirection direction to apply knockback to.
     * @param knockback knockback to apply to given direction.
     * @see #damage()
     */
    @Override
    protected void hit(Direction knockbackDirection, int knockback) {
        damage();
        Point knockbackSteps=new Point();
        switch(knockbackDirection) {
            case RIGHT -> knockbackSteps.x += knockback;
            case LEFT -> knockbackSteps.x -= knockback;
            case UP -> knockbackSteps.y -= knockback;
            case DOWN -> knockbackSteps.y += knockback;
        }
        moveSafely(knockbackSteps.x, knockbackSteps.y);
    }

    /**
     * Returns current direction of this.
     * @return direction entity has.
     */
    @Override
    protected Direction currentDirection() {
        if(mainKeyListener.left) return Direction.LEFT;
        else if(mainKeyListener.right) return Direction.RIGHT;
        else if(mainKeyListener.down) return Direction.DOWN;
        else if(mainKeyListener.up) return Direction.UP;
        else return Direction.NONE;
    }

    /**
     * KeyListener listens to key so movement is available.
     * @see Listener
     * @see java.awt.event.KeyListener
     */
    protected final class PlayerKeyListener implements Listener {
        /**
         * Variables gets {@code true} when the player moves the map {@code right} or {@code left}.
         */
        boolean left, right, up, down;

        /**
         * Method gets called when the event starts.
         */
        @Override
        public void keyDown() {
            switch (context.getKeyListener(DefaultKeyListener.class).getKeyCode()) {
                case KeyEvent.VK_A:
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_KP_LEFT:
                    left=true;
                    break;
                case KeyEvent.VK_D:
                case KeyEvent.VK_RIGHT:
                case KeyEvent.VK_KP_RIGHT:
                    right=true;
                    break;
                case KeyEvent.VK_W:
                case KeyEvent.VK_UP:
                case KeyEvent.VK_KP_UP:
                    up=true;
                    break;
                case KeyEvent.VK_S:
                case KeyEvent.VK_DOWN:
                case KeyEvent.VK_KP_DOWN:
                    down=true;
                    break;
            }
        }

        /**
         * Method gets called when the event ends.
         */
        @Override
        public void keyUp() {
            switch (context.getKeyListener(DefaultKeyListener.class).getKeyCode()) {
                case KeyEvent.VK_A:
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_KP_LEFT:
                    left=false;
                    break;
                case KeyEvent.VK_D:
                case KeyEvent.VK_RIGHT:
                case KeyEvent.VK_KP_RIGHT:
                    right=false;
                    break;
                case KeyEvent.VK_W:
                case KeyEvent.VK_UP:
                case KeyEvent.VK_KP_UP:
                    up=true;
                    break;
                case KeyEvent.VK_S:
                case KeyEvent.VK_DOWN:
                case KeyEvent.VK_KP_DOWN:
                    down=true;
                    break;
            }
        }
    }
}
