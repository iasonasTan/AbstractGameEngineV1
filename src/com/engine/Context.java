package com.engine;

import com.engine.entity.Entity;
import com.engine.entity.Player;
import com.engine.map.Map;
import com.engine.view.DisplayableDrawer;

import java.awt.event.KeyListener;
import java.util.function.Consumer;

public interface Context {
    <T extends Map> T getMap(Class<T> clazz) throws ClassCastException;
    <T extends Player> T getPlayer(Class<T> clazz) throws ClassCastException;
    <T extends DisplayableDrawer> T getDisplay(Class<T> clazz) throws ClassCastException;
    <T extends KeyListener> T getKeyListener(Class<T> clazz) throws ClassCastException;
    void startGameThread();
    void stopGameThread();

    void moveEntities(int knockback, int i);
    void forEachEntity(Consumer<Entity> o);
    <T extends Entity> void forEachEntity(Consumer<T> consumer, Class<T> clazz);
    Context addEntity(String name, Entity projectile);
}
