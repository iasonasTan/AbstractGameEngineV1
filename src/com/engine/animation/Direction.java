package com.engine.animation;

/**
 * Direction of an entity.
 * Used in entity and animation.
 */
public enum Direction {
    LEFT,
    RIGHT,
    NONE,
    UP,
    DOWN;

    /**
     * Gives the opposite direction of this.
     * @return returns the opposite direction of this.
     */
    public Direction opposite() {
        return switch(this) {
            case LEFT -> RIGHT;
            case RIGHT -> LEFT;
            case NONE -> NONE;
            case UP -> DOWN;
            case DOWN -> UP;
        };
    }
}
