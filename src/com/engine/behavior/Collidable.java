package com.engine.behavior;

import java.awt.*;

public interface Collidable {
    /**
     * Checks if entity collides with another entity.
     * @param other entity to check collision.
     * @return {@code true} if entity collides with this, {@code false} otherwise...
     */
    default boolean hasCollisionWith(Collidable other) {
        return other.getHitbox().intersects(getHitbox());
    }

    /**
     * Returns entity's hitbox as {@link Rectangle}...
     * @return entity's hitbox.
     */
    Rectangle getHitbox();
}
