package com.engine.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;

public interface Displayable {
    void display();
    void dispose();
    JFrame getFrame();
    Dimension dimension();
    void addKeyListener(KeyListener listener);
    void gainFocus();
}
