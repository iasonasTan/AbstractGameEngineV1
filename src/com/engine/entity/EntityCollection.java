package com.engine.entity;

import com.engine.behavior.Collidable;
import com.engine.behavior.Updatable;

import java.util.Optional;
import java.util.function.Consumer;

public interface EntityCollection<T extends Entity> extends Updatable, Collidable {
    // collection
    boolean add(T tile);
    boolean remove(T tile);
    T remove(int index);
    T[] getEntities(T[] arr);
    void forEach(Consumer<? super T> consumer);

    // collision
    Optional<T> getColliderOf(Collidable entity);
    int collidersCount(Collidable entity);

    // moving
    boolean tryMoveAllSafely(int stepsX, int stepsY);
    void moveUnsafely(int sX, int sY);
    boolean moveSafely(int sX, int sY);
}
