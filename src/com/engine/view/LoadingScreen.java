package com.engine.view;

import javax.swing.*;
import java.awt.*;

/**
 * Implementation of {@link AbstractScreen}.
 * Takes given text and adds animated ellipsis at the end.
 * Sets given color as background color and foreground color as the opposite of background color.
 */
public class LoadingScreen extends AbstractScreen implements Runnable {
    /**
     * Original text.
     * @see #run()
     */
    private final String mOriginalText;

    /**
     * Text label showing text.
     */
    private final JLabel mTextLabel =new JLabel();

    /**
     * Thread holding animation.
     * @see #run()
     */
    private Thread mThread;

    /**
     * Constructor takes original text and background color.
     * Sets up gui and stuff.
     * Automatically starts {@code mThread}
     * @param mOriginalText text to show before animated ellipsis.
     * @param bColor background color of the screen.
     */
    public LoadingScreen(String mOriginalText, Color bColor) {
        super("PANEL"+mOriginalText);
        this.mOriginalText = mOriginalText;
        getFrame().setUndecorated(true);
        setLayout(new GridBagLayout());
        setBackground(bColor);
        Color foregroundColor=new Color(255-bColor.getRed(), 255-bColor.getBlue(), 255-bColor.getBlue(), bColor.getAlpha());
        mTextLabel.setForeground(foregroundColor);
        mTextLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(mTextLabel);
        startAnimating();
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
     * Also stops animation thread.
     * @see Component#isDisplayable
     */
    @Override
    public void dispose() {
        super.dispose();
        stopAnimation();
    }

    /**
     * Starts thread.
     * Animation is running until {@link #stopAnimation()} is called.
     */
    public void startAnimating() {
        mThread=new Thread(this);
        mThread.start();
    }

    /**
     * Stops thread.
     * Animation resets.
     */
    public void stopAnimation() {
        mThread=null;
        mTextLabel.setText(mOriginalText);
    }

    /**
     * Runs animation, adding and removing dots
     * from text label until user stops animation.
     */
    @Override
    public void run() {
        StringBuilder dots_builder=new StringBuilder(mOriginalText);
        while(mThread!=null) {
            dots_builder.append('.');
            mTextLabel.setText(dots_builder.toString());
            if(dots_builder.length()>3+mOriginalText.length()) {
                dots_builder.delete(0, dots_builder.length());
                dots_builder.append(mOriginalText);
            }
            try{
                Thread.sleep(175);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Method forces user to return a screen size.
     * @return {@link Dimension} which will be set as the size of frame and screen.
     */
    @Override
    public Dimension dimension() {
        return new Dimension(500, 350);
    }

    @Override
    public void gainFocus() {
        setFocusable(true);
        requestFocus();
    }

    /**
     * Method that creates a new {@link JFrame} and sets this as content pane.
     * Sets container's name as the window title.
     *
     * @return the frame.
     */
    @Override
    protected JFrame startOnFrame() {
        JFrame frame=new JFrame();
        frame.setUndecorated(true);
        frame.setContentPane(this);
        frame.setResizable(false);
        frame.setSize(screenSize);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setTitle("LS-"+mOriginalText);
        return frame;
    }
}
