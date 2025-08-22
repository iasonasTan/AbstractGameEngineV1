package com.engine.map;

import com.engine.behavior.Collidable;
import com.engine.behavior.Movable;

public interface HorizontalMap extends Map {
    <T extends Movable & Collidable> boolean willEntityTouchGround(T entity, int offsetY);

}
