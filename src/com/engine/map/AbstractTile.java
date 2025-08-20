package com.engine.map;

import com.engine.AbstractGame;
import com.engine.Context;
import com.engine.animation.Direction;
import com.engine.entity.AbstractEntity;

import java.awt.*;

/**
 * Tile superclass.
 * Implements {@link #currentDirection()} with its only one right implementation.
 * Initializes default x and y.
 */
public abstract class AbstractTile extends AbstractEntity implements Tile {

    /**
     * Constructor initializes x and y with given x and y.
     * @param context game context.
     * @param cords default coordinates of tile.
     */
    public AbstractTile(Context context, Point cords) {
        this(context);
        worldX=cords.x;
        worldY=cords.y;
    }

    public AbstractTile(Context context) {
        super(context);
    }

    /**
     * Final implementation of method.
     * @return current direction.
     */
    @Override
    protected final Direction currentDirection() {
        return Direction.NONE;
    }
}
