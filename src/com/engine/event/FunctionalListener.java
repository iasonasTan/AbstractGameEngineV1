package com.engine.event;

/**
 * Functional version of {@link Listener}. Use a single method
 * with boolean.
 * {@link #onKey(boolean)} pressed = key down, !pressed = key up.
 * Prefer using it as lambda expression.
 */
@FunctionalInterface
public interface FunctionalListener extends Listener {
    /**
     * Method executes when event starts or ends.
     * @param pressed {@code true} when event starts, {@code false} otherwise.
     */
    void onKey(boolean pressed);

    /**
     * Default implementation of keyDown.
     * Never override this, use {@link Listener}.
     */
    @Override
    default void keyDown() {
        onKey(true);
    }


    /*
     * Default implementation of keyUp.
     * Never override this, use {@link Listener}.
     */
    @Override
    default void keyUp() {
        onKey(false);
    }
}
