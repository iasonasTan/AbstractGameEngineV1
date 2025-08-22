package com.engine.event;

import java.util.function.Consumer;

public interface KeyHandler extends java.awt.event.KeyListener {
    KeyHandler addListener(int key, Listener listener);
    KeyHandler removeListener(int key);
    KeyHandler addAction(Consumer<Integer> listener);
}
