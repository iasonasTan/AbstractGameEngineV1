package com.engine.behavior;

import com.engine.entity.Entity;

import java.awt.*;

public interface Renderable {
    /**
     * Creates entity drawer and adds it to main displayable drawer.
     * @return return this.
     */
    Entity startRendering();

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
    <T extends Renderable.Drawer> T getDrawer(Class<T> drawerClass) throws ClassCastException;

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
}
