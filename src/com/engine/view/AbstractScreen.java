package com.engine.view;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractScreen extends JPanel implements Displayable {
    /**
     * Screen's frame.
     * @see #getFrame()
     */
    public final JFrame mFrame;

    /**
     * Screen's size.
     * @see #getPreferredSize()
     */
    public final Dimension screenSize;

    /**
     * Constructor taking frame and screen name.
     * @param title screen and frame name.
     */
    public AbstractScreen(String title) {
        setName(title);
        screenSize= dimension();
        mFrame=startOnFrame();
    }

    /**
     * Sets screen visible or hidden.
     * @param v  true to make the component visible; false to
     *          make it invisible
     */
    public void setFrameVisible(boolean v) {
        mFrame.setVisible(v);
    }

    /**
     * Returns frame related this panel.
     * @return {@link JFrame} pre-configured and containing this panel.
     */
    public JFrame getFrame() {
        return mFrame;
    }

    /**
     * Method forces user to return a screen size.
     * @return {@link Dimension} which will be set as the size of frame and screen.
     */
    public abstract Dimension dimension();

    /**
     * Method that creates a new {@link JFrame} and sets this as content pane.
     * Sets container's name as the window title.
     * @return the frame.
     */
    protected abstract JFrame startOnFrame();

    /**
     * Sets panel and frame as visible.
     * @see #dispose()
     */
    public void display() {
        setVisible(true);
        getFrame().setVisible(true);
    }

    /**
     * Releases all the native screen resources used by this
     * {@code Window}, its subcomponents, and all of its owned
     * children. That is, the resources for these {@code Component}s
     * will be destroyed, any memory they consume will be returned to the
     * OS, and they will be marked as undisplayable.
     * <p>
     * The {@code Window} and its subcomponents can be made displayable
     * again by rebuilding the native resources with a subsequent call to
     * {@code pack} or {@code show}. The states of the recreated
     * {@code Window} and its subcomponents will be identical to the
     * states of these objects at the point where the {@code Window}
     * was disposed (not accounting for additional modifications between
     * those actions).
     * <p>
     * <b>Note</b>: When the last displayable window
     * within the Java virtual machine (VM) is disposed of, the VM may
     * terminate.  See <a href="doc-files/AWTThreadIssues.html#Autoshutdown">
     * AWT Threading Issues</a> for more information.
     * @see Component#isDisplayable
     */
    public void dispose() {
        getFrame().dispose();
    }
}
