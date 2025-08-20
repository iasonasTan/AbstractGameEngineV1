package com.engine.event;

/**
 * Listener for listening events.
 * Method includes different methods for start and end of events.
 * Also used in key events.
 * @see KeyListener
 */
public interface Listener {
    /**
     * Method gets called when the event starts.
     */
    void keyDown();

    /**
     * Method gets called when the event ends.
     */
    void keyUp();
}
