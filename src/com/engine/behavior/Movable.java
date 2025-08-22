package com.engine.behavior;

import com.engine.animation.Direction;

import java.awt.*;

public interface Movable {
    /**
     * Moves entity to given pixels, if entity collides with another entity or tile
     * it gets back to its original place.
     * Hitbox gets updated.
     * @param stepsX pixels to move entity horizontal.
     * @param stepsY pixels to move entity vertical.
     * @return return {@code true} if entity moved; {@code false} otherwise.
     */
    boolean moveSafely(int stepsX, int stepsY);

    /**
     * Moves entity to given pixels, if entity collides with another entity or tile...
     * nothing happens...
     * Hitbox gets updated.
     * @param stepsX pixels to move entity horizontal.
     * @param stepsY pixels to move entity vertical.
     */
    void moveUnsafely(int stepsX, int stepsY);

    /**
     * Returns current speed of entity.
     * @return speed as integer.
     */
    int getCurrentSpeed();

    /**
     * Returns entity's direction...
     * @return entity's direction as {@link Direction}...
     */
    Direction getDirection();

    /**
     * Sets entity's position.
     * @param position new entity's position as {@link Point}.
     */
    void setPosition(Point position);

    /**
     * Returns entity's current position.
     * @return position as {@link Point}.
     */
    Point getPosition();
}
