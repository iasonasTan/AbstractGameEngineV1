package com.engine.entity;

import com.engine.behavior.Collidable;
import com.engine.behavior.Movable;
import com.engine.behavior.Renderable;
import com.engine.behavior.Updatable;

import java.awt.*;

public interface Entity extends Collidable, Movable, Renderable, Updatable {
    /**
     * Returns true or false depending on entity's solidness.
     * @return {@code true} if entity is solid, {@code false} otherwise.
     */
    boolean isSolid();

    /**
     * Returns distance between this and given point...
     * @param point point to check
     * @return distance between this and given point as integer.
     */
    int distanceFrom(Point point);
}
