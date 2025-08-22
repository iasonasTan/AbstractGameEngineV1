package com.engine.view;

import com.engine.Context;
import com.engine.behavior.Renderable;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;
import java.awt.event.KeyListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a window that displays the game.
 * You can create multiple instances of this class but all instances
 * use the same KeyEventHandler.
 * @see #addKeyListener(KeyListener)
 * This automatically displays itself into a panel and gets it's 
 * size by the subclass.
 * @see #dimension()
 */
@SuppressWarnings("unused")
public abstract class AbstractGameScreen extends AbstractScreen implements DisplayableDrawer {
    /**
     * List that holds all {@link Renderable.Drawer}s
     * @see Renderable.Drawer
     */
    private final List<Renderable.Drawer> mDrawers= new ArrayList<>() {
        /**
         * Appends the specified element to the end of this list.
         *
         * @param drawer element to be appended to this list
         * @return {@code true} (as specified by {@link java.util.Collection::add})
         */
        @Override
        public boolean add(Renderable.Drawer drawer) {
            if(drawer==null)
                return false;
            return super.add(drawer);
        }
    };

    /**
     * Context is used to access everything inside the app easily.
     * @see Context
     */
    protected final Context context;

    /**
     * Background image displaying.
     */
    private final Image mBackgroundImage;

    /**
     * Creates a new instance of GameScreen with a context.
     * @see Context
     * initializes keyListener, launches a frame and sets the size.
     * @param context context.
     */
    protected AbstractGameScreen(Context context, String initialTitle) {
        super("Untitled_Game_2");
        this.context = context;
        URL uri=getBackgroundUri();
        if(uri==null)
            mBackgroundImage =null;
        else
            mBackgroundImage =new ImageIcon(uri).getImage();
        setSize(screenSize);
        setName(initialTitle);
    }

    /**
     * Draws things in the top layer of the graphics.
     * <p><b>NOTE: </b>Overriding {@link #paintComponent(Graphics)} will <i>NOT</i> work.</p>
     * @param graphics Panel's graphics to draw things on.
     */
    protected abstract void drawOnTopLayer(Graphics2D graphics);

    /**
     * Method used to get a url to the background image of the screen.
     * @return url to background image, null for no background image.
     */
    protected abstract URL getBackgroundUri();

    /**
     * PaintComponent method used to feed all drawers.
     * Calls the UI delegate's paint method, if the UI delegate
     * is non-<code>null</code>.  We pass the delegate a copy of the
     * <code>Graphics</code> object to protect the rest of the
     * paint code from irrevocable changes
     * (for example, <code>Graphics.translate</code>).
     * <p>
     * If you override this in a subclass you should not make permanent
     * changes to the passed in <code>Graphics</code>. For example, you
     * should not alter the clip <code>Rectangle</code> or modify the
     * transform. If you need to do these operations you may find it
     * easier to create a new <code>Graphics</code> from the passed in
     * <code>Graphics</code> and manipulate it. Further, if you do not
     * invoke super's implementation you must honor the opaque property, that is
     * if this component is opaque, you must completely fill in the background
     * in an opaque color. If you do not honor the opaque property you
     * will likely see visual artifacts.
     * <p>
     * The passed in <code>Graphics</code> object might
     * have a transform other than the identify transform
     * installed on it.  In this case, you might get
     * unexpected results if you cumulatively apply
     * another transform.
     *
     * @param g the <code>Graphics</code> object to protect
     * @see #paint
     * @see ComponentUI
     */
    @Override
    protected final void paintComponent(Graphics g) {
        super.paintComponent(g);
        // create and config two-dimensional graphics
        Graphics2D graphics=(Graphics2D)g;
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        // draw game
        graphics.drawImage(mBackgroundImage, 0, 0, getWidth(), getHeight(), null);
        Renderable.Drawer[] drawers=mDrawers.toArray(new Renderable.Drawer[0]);
        for (Renderable.Drawer drawer: drawers) {
            drawer.drawAll(graphics);
        }
        drawOnTopLayer(graphics);
        g.dispose();
    }

    /**
     * Renders everything in the screen.
     * @see #paintComponent(Graphics)
     */
    @Override
    public void renderEverything() {
        repaint();
        paintImmediately(0, 0, getWidth(), getHeight());
    }

    /**
     * Adds Entity's drawer to {@link #mDrawers} implementation
     * @see Renderable.Drawer
     * @param entities entities to include in drawing process.
     */
    @Override
    public synchronized void addEntitiesToDraw(Renderable... entities) {
        for (Renderable entity : entities) {
            mDrawers.add(entity.getDrawer(Renderable.Drawer.class));
        }
    }

    /**
     * Removes a given entity's drawer from screen.
     * @param entity entity to remove its drawer.
     */
    @Override
    public void removeRenderable(Renderable entity) {
        mDrawers.remove(entity.getDrawer(Renderable.Drawer.class));
    }

    /**
     * Returns all drawers of this screen.
     * @return drawers of this screen as List<> impl.
     */
    @Override
    public List<? extends Renderable.Drawer> getDrawers() {
        return mDrawers;
    }
}
