package com.engine.animation;

/**
 * Interface represents an animation.
 * An animation takes an implementation of Animatable
 * and does some animation to it.
 * Example implementation {@link SqueezeAnimation}.
 */
public interface Animation {
    /**
     * Updates animation to the next frame.
     * Calls must be synchronized with game loop calls.
     * It's recommended to use <code>AbstractGame.gameTimeMillis()</code>.
     */
    void update();

    /**
     * Sets an animatable implementation.
     * In this instance we do the animation.
     * @param entity animatable to apply animation.
     */
    void setAnimatable(Animatable entity);

    /**
     * Asks if animation is alive.
     * @return Return {@code true} if animation is running, {@code false} otherwise.
     */
    boolean alive();
}
