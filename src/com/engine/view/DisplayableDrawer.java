package com.engine.view;

import com.engine.behavior.Renderable;

import java.util.List;

public interface DisplayableDrawer extends Displayable {
    void renderEverything();
    void addEntitiesToDraw(Renderable... entities);
    void removeRenderable(Renderable entity);
    List<? extends Renderable.Drawer> getDrawers();
}
