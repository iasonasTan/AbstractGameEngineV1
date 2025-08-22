package com.engine.entity;

import com.engine.AbstractGame;
import com.engine.behavior.Collidable;
import com.engine.behavior.Updatable;

import java.awt.*;
import java.util.*;

/**
 * Class manages entities of type T.
 * @param <T> type of entities that will be managed.
 */
public class DefaultEntityManager<T extends Entity> extends ArrayList<T> implements EntityCollection<T> {
    /**
     * Context as AbstractGame.
     */
    protected final AbstractGame context;

    /**
     * Context constructor.
     * @param context this context.
     */
    public DefaultEntityManager(AbstractGame context) {
        this.context = context;
    }

    /**
     * Checks if the given {@code entity} collides with one of the containing entities.
     * @param entity entity to check.
     * @return {@code true} if given entity collides with one of the containing entities, {@code false} otherwise.
     */
    public final boolean hasCollisionWith(Collidable entity) {
        for(T e: this) {
            if(e instanceof Collidable collidable && collidable.hasCollisionWith(entity))
                return true;
        }
        return false;
    }

    /**
     * Returns an {@link java.util.Optional} containing the entity that collides with the given entity.
     * @param entity entity to check.
     * @return Entity that collides the given entity inside an {@link java.util.Optional}, {@code Optional.empty()} otherwise.
     */
    public final Optional<T> getColliderOf(Collidable entity) {
        for(T e: this) {
            if(e instanceof Collidable collidable && collidable.hasCollisionWith(entity))
                return Optional.of(e);
        }
        return Optional.empty();
    }

    @Override
    public boolean remove(T tile) {
        return super.remove(tile);
    }

    @Override
    public T[] getEntities(T[] arr) {
        return toArray(arr);
    }

    /**
     * Updates all containing entities and removes dead ones.
     */
    public void update() {
        Iterator<T> iter=iterator();
        while(iter.hasNext()) {
            T t=iter.next();
            t.update();
            if(!t.isGarbage()) {
                iter.remove();
            }
        }
    }

    /**
     * Says if entity is garbage, if yes it's manager has to wipe it from memory.
     *
     * @return return {@code true} if entity is garbage; {@code false} otherwise.
     */
    @Override
    public boolean isGarbage() {
        boolean isGarbage=false;
        for (int i = 0; i < size(); i++) {
            isGarbage = isGarbage || get(i).isGarbage();
        }
        return isGarbage;
    }

    /**
     * Makes entity garbage, which means no update OR draw wil happen.
     * It's manager has to remove it after update.
     */
    @Override
    public void makeGarbage() {
        forEach(Updatable::makeGarbage);
    }

    /**
     * Moves all entities unsafely.
     * @param stepsX pixels to move each entity horizontally.
     * @param stepsY pixels to move each entity vertically.
     */
    public void moveUnsafely(int stepsX, int stepsY) {
        for (int i = 0; i < size(); i++) { // avoid ConcurrentModificationException
            get(i).moveUnsafely(stepsX, stepsY);
        }
    }

    /**
     * Moves all containing entities safely.
     * If an entity collides with another, this specific entity will get back
     * to its original position, this will not affect other entities.
     * @param stepsX pixels to move entities horizontally
     * @param stepsY pixels to move entities vertically
     */
    public boolean moveSafely(int stepsX, int stepsY) {
        for(int i=0; i<size(); i++) { // avoid ConcurrentModificationException
            get(i).moveSafely(stepsX, stepsY);
        }
        return true;
    }

    /**
     * Moves all containing entities unsafely and checks if an entity collides with another entity.
     * If an entity has collision with another, <i>all</i> entities will go back to their original position.
     * @param stepsX pixels to move entities horizontally
     * @param stepsY pixels to move entities vertically
     */
    public boolean tryMoveAllSafely(int stepsX, int stepsY) {
        boolean collision=false;
        int collisionIndex=-1;
        for (int i = 0; i < size(); i++) {
            T entity=get(i);
            collision = collision||entity.moveSafely(stepsX, stepsY);
            if(collision) {
                collisionIndex=i;
                break;
            }
        }
        if(collision) {
            // move entities to their original positions
            for (int i = 0; i <= collisionIndex; i++) {
                T entity=get(i);
                entity.moveUnsafely(-stepsX, -stepsY);
            }
        }
        return collision;
    }

    /**
     * Returns the number of entities collides the given entity at the same time.
     * @param entity entity to check.
     * @return number of colliders.
     */
    public int collidersCount(Collidable entity) {
        int out=0;
        for (T t: this) {
            if(t instanceof Collidable collidable && collidable.hasCollisionWith(entity))
                out++;
        }
        return out;
    }

    /**
     * Returns the closest entity to the given entity.
     * And casts it to given class.
     * @param entity entity to find the closest entity.
     * @return returns the closes entity to the given entity cast to given type.
     */
    public final T nearestEntity(Entity entity) {
        T nearest=null;
        int minDistance=Integer.MAX_VALUE;
        for (T e: this) {
            int distance = e.distanceFrom(entity.getPosition());
            if (distance < minDistance) {
                minDistance = distance;
                nearest = e;
            }
        }
        return nearest;
    }

    /**
     * Returns entity's hitbox as {@link Rectangle}...
     *
     * @return entity's hitbox.
     */
    @Override
    public Rectangle getHitbox() {
        throw new RuntimeException("Method is unavailable.");
    }
}
