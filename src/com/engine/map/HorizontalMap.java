package com.engine.map;

import com.engine.entity.Entity;

public interface HorizontalMap extends Map {
    boolean willEntityTouchGround(Entity entity, int offsetY);

}
