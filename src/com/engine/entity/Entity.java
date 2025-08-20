package com.engine.entity;

import com.engine.animation.Direction;

import java.awt.*;

public interface Entity {
    /**
     * Creates entity drawer and adds it to main displayable drawer.
     * @return return this.
     */
    Entity startDrawing();

    /**
     * Updates entity.
     * Called by game thread FPS times per sec.
     */
    void update();

    /**
     * Says if entity is garbage, if yes it's manager has to wipe it from memory.
     * @return return {@code true} if entity is garbage; {@code false} otherwise.
     */
    boolean isGarbage();

    /**
     * Makes entity garbage, which means no update OR draw wil happen.
     * It's manager has to remove it after update.
     */
    void makeGarbage();

    /**
     * Returns entity's direction...
     * @return entity's direction as {@link Direction}...
     */
    Direction getDirection();

    /**
     * Returns entity's hitbox as {@link Rectangle}...
     * @return entity's hitbox.
     */
    Rectangle getHitbox();

    /**
     * Returns entity's current worldX.
     * @return worldX as integer.
     */
    int getWorldX();

    /**
     * Returns entity's current worldY.
     * @return worldY as integer.
     */
    int getWorldY();

    /**
     * Returns entity's current width.
     * @return width as integer.
     */
    int getWidth();

    /**
     * Returns entity's current height.
     * @return height as integer.
     */
    int getHeight();

    /**
     * Sets entity's position.
     * @param position new entity's possition as {@link Point}.
     */
    void setPosition(Point position);

    /**
     * Returns entity's current position.
     * @return position as {@link Point}.
     */
    Point getPosition();

    /**
     * Returns current speed of entity.
     * @return speed as integer.
     */
    int getCurrentSpeed();

    /**
     * Returns true or false depending on entity's solidness.
     * @return {@code true} if entity is solid, {@code false} otherwise.
     */
    boolean isSolid();

    /**
     * Moves entity to given pixels, if entity collides with another entity or tile
     * it gets back to its original place.
     * Hitbox gets updated.
     * @param stepsX pixels to move entity horizontal.
     * @param stepsY pixels to move entity vertical.
     * @return return {@code true} if entity moved; {@code false} otherwise.
     */
    boolean moveSafely(int stepsX, int stepsY);

    /**
     * Moves entity to given pixels, if entity collides with another entity or tile...
     * nothing happens...
     * Hitbox gets updated.
     * @param stepsX pixels to move entity horizontal.
     * @param stepsY pixels to move entity vertical.
     */
    void moveUnsafely(int stepsX, int stepsY);

    /**
     * Checks if entity collides with another entity.
     * @param other entity to check collision.
     * @return {@code true} if entity collides with this, {@code false} otherwise...
     */
    boolean hasCollisionWith(Entity other);

    /**
     * Returns distance between this and given point...
     * @param point point to check
     * @return distance between this and given point as integer.
     */
    int distanceFrom(Point point);

    /**
     * Defines a drawer.
     * Drawer is a different part of the game, is the separated drawing logic of the distance.
     */
    interface Drawer {

        /**
         * Draws everything about entity.
         * @param graphics graphics to draw entity to.
         */
        void drawAll(Graphics2D graphics);
    }

    /**
     * Method returns drawer cast to given class.
     * @param drawerClass class to cast drawer before return.
     * @return entity's drawer cast to given class.
     * @param <T> type of drawer implementation.
     * @throws ClassCastException if given class is not assignable from drawer's class.
     */
    <T extends Drawer> T getDrawer(Class<T> drawerClass) throws ClassCastException;
}
