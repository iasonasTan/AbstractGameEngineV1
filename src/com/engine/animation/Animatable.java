package com.engine.animation;

/**
 * Something that can be animated can implement this interface
 * and be passed to an {@link Animation}.
 * @see Animation
 */
public interface Animatable {
    /**
     * Increases the width of animatable by diff.
     * if diff is negative, decrease the width of animatable by diff.
     * @param diff difference from regular size.
     */
    void changeWidth(int diff);

    /**
     * Increases the height of animatable by diff.
     * if diff is negative, decrease the height of animatable by diff.
     * @param diff difference from regular size.
     */
    void changeHeight(int diff);

    /**
     * Returns the current width as an integer.
     * @return returns the width of Animatable.
     */
    int getWidth();

    /**
     * Returns the current height as an integer.
     * @return returns the height of Animatable.
     */
    int getHeight();

    /**
     * Increases Animatable's X by given diff.
     * if diff is negative, decrease X by given diff.
     * @param diff difference from old X to new X.
     */
    void changeX(int diff);

    /**
     * Increases Animatable's Y by given diff.
     * if diff is negative, decrease Y by given diff.
     * @param diff difference from old Y to new Y.
     */
    void changeY(int diff);

    /**
     * Returns animatable's current worldX as integer.
     * @return worldX as integer.
     */
    int getWorldX();

    /**
     * Returns animatable's current worldY as integer.
     * @return worldY as integer.
     */
    int getWorldY();
}
