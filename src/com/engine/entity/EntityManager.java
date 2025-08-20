package com.engine.entity;

import java.util.Optional;
import java.util.function.Consumer;

public interface EntityManager<T extends Entity> {
    boolean add(T tile);
    boolean remove(T tile);
    T remove(int index);
    T[] getEntities(T[] arr);
    void forEach(Consumer<? super T> consumer);
    void updateEntities();

    boolean containsCollisionWith(Entity entity);
    Optional<T> getColliderOf(Entity entity);
    int collidersCount(Entity entity);

    void moveEntitiesUnsafely(int stepsX, int stepsY);
    void moveEntitiesSafely(int stepsX, int stepsY);
    boolean tryMoveAllSafely(int stepsX, int stepsY);
}
