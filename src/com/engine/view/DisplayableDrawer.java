package com.engine.view;

import com.engine.entity.Entity;

import java.util.List;

public interface DisplayableDrawer extends Displayable {
    void renderEverything();
    void addEntitiesToDraw(Entity... entities);
    void removeEntity(Entity entity);
    List<? extends Entity.Drawer> getDrawers();
}
