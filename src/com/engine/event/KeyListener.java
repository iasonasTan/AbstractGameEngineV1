package com.engine.event;

import com.engine.data.UniqueInsertMap;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Implementation of {@link java.awt.event.KeyListener}, used for easy event handling.
 * Uses methods {@link #addListener(int, Listener)}, {@link #removeEvent(int)} to add/remove events.
 * Class allows only one event per key.
 * You only have to create an instance, add it to your panel as {@code keyListener} and add events via {@link #addListener(int, Listener)}
 */
public final class KeyListener implements java.awt.event.KeyListener {
    /**
     * Map to store keys and their related events.
     * @see UniqueInsertMap
     */
    private final UniqueInsertMap<Integer, Listener> events= new UniqueInsertMap<>();

    /**
     * List to store all actions to execute when whatever key is pressed.
     * @see #addAction(Consumer)
     */
    private final List<Consumer<Integer>> mEveryEventActions =new ArrayList<>();

    /**
     * last pressed key's code.
     */
    private int mLastPressedKeyCode;

    /**
     * Adds action to {@link #mEveryEventActions} list.
     * @param action action to add.
     * @see #mEveryEventActions
     */
    public void addAction(Consumer<Integer> action) {
        mEveryEventActions.add(action);
    }

    /**
     * Add event and action to related key.
     * @param key keycode of action-related key (use codes from {@link java.awt.event.KeyEvent}).
     * @param action action to execute when key is pressed.
     * @return returns this, making chain calls available.
     */
    public KeyListener addListener(int key, Listener action) {
        events.putPair(key, action);
        return this;
    }

    /**
     * Removes event from key.
     * @param key key's keycode, from {@link java.awt.event.KeyEvent}
     * @return returns this, making chain calls available.
     */
    public KeyListener removeEvent(int key) {
        events.remove(key);
        return this;
    }

    /**
     * Invoked when a key has been pressed.
     * See the class description for {@link KeyEvent} for a definition of
     * a key pressed event.
     * @param e the event to be processed
     */
    @Override
    public void keyPressed(KeyEvent e) {
        mLastPressedKeyCode =e.getKeyCode();
        mEveryEventActions.forEach(a -> a.accept(mLastPressedKeyCode));
        Listener listener =events.get(mLastPressedKeyCode);
        if(listener !=null) listener.keyDown();
    }

    /**
     * Invoked when a key has been released.
     * See the class description for {@link KeyEvent} for a definition of
     * a key released event.
     *
     * @param e the event to be processed
     */
    @Override
    public void keyReleased(KeyEvent e) {
        mLastPressedKeyCode =e.getKeyCode();
        Listener listener =events.get(mLastPressedKeyCode);
        if(listener !=null) listener.keyUp();
    }

    @Override public void keyTyped(KeyEvent e) {}

    /**
     * Returns last pressed key's code.
     * @return last pressed key's code as integer.
     */
    public int getKeyCode() {
        return mLastPressedKeyCode;
    }
}
