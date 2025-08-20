package com.engine.entity;

import com.engine.AbstractGame;
import com.engine.animation.Animation;
import com.engine.animation.Direction;
import com.engine.event.KeyListener;
import com.engine.event.Listener;
import com.engine.map.Map;

import java.awt.event.KeyEvent;

/**
 * Some Implementation of a Player.
 * Side scroller player.
 */
public abstract class AbstractStaticPlayer extends AbstractEntity implements Player {
    // KeyListener handles all events.
    private final StaticPlayerKeyListener mainKeyListener = new StaticPlayerKeyListener();

    /**
     * This gets {@code false} when detect collision, and back {@code true} after moving some pixels.
     * @see #update()
     */
    private boolean mFistMoveTry=true;

    /**
     * Constructor with context.
     * Adds some keyListeners by default.
     * @param context context to pass to super
     */
    public AbstractStaticPlayer(AbstractGame context) {
        super(context);
        addListeners();
    }

    protected void addListeners() {
        context.getKeyListener(KeyListener.class).addListener(KeyEvent.VK_SPACE, mainKeyListener)
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
                .addListener(KeyEvent.VK_KP_RIGHT, mainKeyListener);
    }

    /**
     * Returns current direction of this.
     * @return direction entity has.
     */
    @Override
    protected Direction currentDirection() {
        if(mainKeyListener.left==mainKeyListener.right)
            return Direction.NONE;
        if(mainKeyListener.right)
            return Direction.RIGHT;
        else
            return Direction.LEFT;
    }

    /**
     * Damages entity, decreases {@link #hp}.
     * If {@link #hp} is less or equal to zero, the entity is being killed.
     * Applies knockback (2 times width) to this to the given direction.
     * This method is modified so it moves the world instead of the actual entity.
     * @param direction direction to apply knockback to.
     * @see #kill()
     */
    @Override
    protected void hit(Direction direction, int knockback) {
        damage();
        if(direction!=Direction.NONE) {
            if(direction==Direction.LEFT) knockback*=-1;
            context.getMap(Map.class).moveEntitiesUnsafely(knockback, 0);
            context.moveEntities(knockback, 0);
            if (context.getMap(Map.class).containsCollisionWith(this)) {
                context.getMap(Map.class).moveEntitiesUnsafely(-knockback, 0);
                context.moveEntities(-knockback, 0);
            }
        }
    }

    /**
     * Updates entity to the next frame.
     * If entity is not static physics, animation, direction e.t.c. gets updated,
     * if not, nothing gets updated except hitbox.
     * Moves entity according to pressed keys.
     */
    @Override
    public void update() {
        super.update();
        int speed=getCurrentSpeed();
        if(mainKeyListener.left) moveGame(speed);
        else if(mainKeyListener.right) moveGame(-speed);
    }

    /**
     * Animation to apply when collision is detected.
     * @param direction direction where collision is coming from.
     * @return {@link Animation} implementation to apply.
     */
    protected abstract Animation createCollisionAnimation(Direction direction);

    /**
     * Moves entire map and entities left or right according to stepsH.
     * @param stepsH steps to move entities and tiles.
     */
    private void moveGame(int stepsH) {
        Map map=context.getMap(Map.class);
        map.moveEntitiesUnsafely(stepsH, 0);
        context.moveEntities(stepsH, 0);
        if(map.containsCollisionWith(this)) {
            map.moveEntitiesUnsafely(-stepsH, 0);
            context.moveEntities(-stepsH, 0);
            if(mFistMoveTry) {
                mFistMoveTry = false;
                animate(createCollisionAnimation(getDirection().opposite()));
            }
        } else {
            mFistMoveTry = true;
        }
    }

    /**
     * KeyListener listens to key so movement is available.
     * @see Listener
     * @see java.awt.event.KeyListener
     */
    protected final class StaticPlayerKeyListener implements Listener {
        /**
         * Variables gets {@code true} when the player moves the map {@code right} or {@code left}.
         */
        boolean left, right;

        /**
         * Method gets called when the event starts.
         */
        @Override
        public void keyDown() {
            switch (context.getKeyListener(KeyListener.class).getKeyCode()) {
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
                case KeyEvent.VK_SPACE:
                case KeyEvent.VK_W:
                case KeyEvent.VK_UP:
                case KeyEvent.VK_KP_UP:
                    jump();
            }
        }

        /**
         * Method gets called when the event ends.
         */
        @Override
        public void keyUp() {
            switch (context.getKeyListener(KeyListener.class).getKeyCode()) {
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
            }
        }
    }

}
