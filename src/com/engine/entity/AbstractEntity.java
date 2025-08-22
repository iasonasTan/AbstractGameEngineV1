package com.engine.entity;

import com.engine.AbstractGame;
import com.engine.Context;
import com.engine.ManifestManager;
import com.engine.animation.Animatable;
import com.engine.animation.Animation;
import com.engine.animation.Direction;
import com.engine.behavior.Collidable;
import com.engine.behavior.Renderable;
import com.engine.event.Listener;
import com.engine.map.AbstractMap;
import com.engine.map.Map;
import com.engine.view.AbstractGameScreen;
import com.engine.view.DisplayableDrawer;
import org.w3c.dom.Element;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.Function;

/**
 * Superclass of every entity inside the game.<br>
 * Automatically gets Drawer instance by subclass,
 * but you have to add it manually to screen by calling {@link #startRendering()}.<br>
 * Takes whatever implementation it needs by subclass.<br>
 * Adding this entity to the right place is important.<br>
 * <b>WARNING</b> Entity adds it's drawer automatically to the main game screen in the context,
 * but you <i>must</i> call {@link #update()} manually to update the entity.
 * <p><b>WARNING</b> If you want to include the entity to the game manifest you
 * <i>must</i> add this entity to one of these places:<br>
 * 1: {@link AbstractGame} implementation with method {@code addEntity(String id, AbstractEntity entity)}.<br>
 * 2: {@link DefaultEntityManager}, added to game with method {@code addEntityManager(String id, EntityManager<?> eManager}.<br>
 * 3: {@link AbstractMap} implementation on {@link AbstractGame} implementation.<br>
 * </p>
 */
@SuppressWarnings("unused")
public abstract class AbstractEntity implements Animatable, Entity {
    /**
     * Holds entity's weight.
     * @see #handleFall()
     */
    private float mWeight=0.60f;

    /**
     * Listeners that listens to dash events.
     * Method keyDown is called when event starts, and keyUp is called when event ends.
     * @see #addEventListener(EventType, Listener)
     */
    private final java.util.List<Listener> mDashListeners =new ArrayList<>();

    /**
     * Listeners that listens to jump events.
     * Method keyDown is called when event starts, and keyUp is called when event ends.
     * @see #addEventListener(EventType, Listener)
     */
    private final java.util.List<Listener> mJumpListeners =new ArrayList<>();

    /**
     * Listeners that listens to fall events.
     * Method keyDown is called when event starts, and keyUp is called when event ends.
     * @see #addEventListener(EventType, Listener)
     */
    private final java.util.List<Listener> mFallListeners =new ArrayList<>();

    /**
     * current direction of the player.
     * @see #currentDirection()
     */
    private Direction mDirection=Direction.NONE;

    /**
     * Flags if an animation is currently running...
     * True if entity does an animation, false otherwise.
     */
    private boolean mAnimating =false;

    /**
     * Currently running animation of the drawer.
     * Drawers support only one animation at time.
     */
    private Animation mAnimation;

    /**
     * Velocity for Y axis, in each update value of
     * this variable is getting added to {@link #worldY}.
     * @see #startFalling()
     */
    private float mVelocityY =15;

    /**
     * Entity's default speed.
     */
    private int mDefaultSpeed=2;

    /**
     * Entity's current speed.
     * Accessible to subclass via {@link #getCurrentSpeed()}.
     */
    private int mCurrentSpeed =mDefaultSpeed;

    /**
     * Is {@code true} while falling and false while not.
     */
    private boolean mFalling =true;

    /**
     * Is {@code true} while jumping and false while not.
     */
    private boolean mOnJump =false;

    /**
     * Entity's dimension.
     * @see #mHitbox
     */
    protected int worldX, worldY;
    protected int width, height;

    /**
     * Entity's hitbox.
     * @see #updateHitbox(Rectangle)
     */
    private final Rectangle mHitbox;

    /**
     * Is {@code true} if entity is solid, false if not.
     * <p>Changing this variable is not recommended.</p>
     */
    private boolean mIsSolid;

    /**
     * Is {@code true} if entity is static, {@code false} if not.
     * Static entity means that has <bold>no physics</bold> and <bold>no animations</bold>.
     * If entity is static, result of {@link #currentDirection()} will be ignored.<br>
     * If someone tries to set static entity's direction as non {@link Direction#NONE},
     * {@link IllegalArgumentException} will be thrown.
     * @see #setDirection(Direction)
     * @see #update()
     */
    private boolean mStatic;

    /**
     * Entity's related drawer.
     * Gets this by subclass implementation.
     * @see #createDrawer()
     * @see #getDrawer(Class)
     */
    private Drawer mDrawer;

    /**
     * Abstract game instance.
     * Protected visibility and final.
     * Useful when want to access other game components.
     */
    protected final Context context;

    /**
     * Time millis that dash ends.
     * @see #dash(int, long)
     */
    private long mDashEndTime_millis;

    /**
     * Gets {@code false} when entity dies.
     * Not alive entities will be removed from data structures and be garbage-collected.
     * @see #makeGarbage()
     * @see #isGarbage()
     */
    private boolean mIsAlive =true;

    /**
     * Current health of player.
     * Decreases when entity is getting damage.
     * @see #hit(Direction,int)
     */
    protected int hp=4;

    /**
     * Boolean is {@code true} when entity is currently on dash, {@code false} otherwise.
     * Dash will execute only when previous dash ends.
     * @see #dash(int, long)
     */
    private boolean mOnDash=false;

    /**
     * Context constructor.
     * Creates new AbstractEntity and adds it's drawer to main screen.
     * @param context abstract game context.
     */
    public AbstractEntity(Context context) {
        this.context=context;
        mHitbox =new Rectangle();
        R_Config config= initialConfig();
        if(config==null) throw new NullPointerException();
        mIsSolid =config.isSolid();
        mStatic=config.isStatic();
        configVars(config.hitbox());
        updateHitbox(mHitbox);
        System.out.println("[DEBUG] Entity "+this+" spawned!");
    }

    /**
     * Gets distance between this and a point.
     * @param point other point.
     * @return Returns the distance between this entity and the given point.
     */
    public final int distanceFrom(Point point) {
        int diffX=point.x-getWorldX();
        int diffY=point.y-getWorldY();
        return (int)Math.sqrt(diffX*diffX+diffY*diffY);
    }

    /**
     * Sets default speed and current speed to given speed.
     * @param s new default speed.
     */
    public void setDefaultSpeed(int s) {
        mDefaultSpeed =s;
        mCurrentSpeed =s;
    }

    /**
     * Returns the current speed value.
     * @return current speed as an integer.
     */
    public int getCurrentSpeed() {
        return mCurrentSpeed;
    }

    /**
     * Increases the width of animatable by diff.
     * if diff is negative, decrease the width of animatable by diff.
     *
     * @param diff difference from regular size.
     */
    @Override
    public void changeWidth(int diff) {
        width+=diff;
    }

    /**
     * Increases the height of animatable by diff.
     * if diff is negative, decrease the height of animatable by diff.
     *
     * @param diff difference from regular size.
     */
    @Override
    public void changeHeight(int diff) {
        height+=diff;
    }

    /**
     * Increases Animatable's X by given diff.
     * if diff is negative, decrease X by given diff.
     *
     * @param diff difference from old X to new X.
     */
    @Override
    public void changeX(int diff) {
        worldX+=diff;
    }

    /**
     * Increases Animatable's Y by given diff.
     * if diff is negative, decrease Y by given diff.
     *
     * @param diff difference from old Y to new Y.
     */
    @Override
    public void changeY(int diff) {
        worldY+=diff;
    }

    /**
     * Checks whether the entity is currently alive.
     * @return {@code true} if the entity is alive; {@code false} otherwise.
     */
    public boolean isGarbage() {
        return mIsAlive;
    }

    /**
     * Checks the distance between another entity and this.
     * @param other other entity to calculate distance from.
     * @return returns distance between this and given entity as integer.
     */
    public final int distanceFrom(Entity other) {
        return distanceFrom(new Point(other.getPosition()));
    }

    /**
     * Returns current entity position packaged in a {@link Point}.
     * @return returns point includes current worldX and worldY.
     */
    public Point getPosition() {
        return new Point(worldX, worldY);
    }

    /**
     * Sets current position to given position.
     * @param newPosition new position as a {@link Point}
     */
    public void setPosition(Point newPosition) {
        worldX=newPosition.x;
        worldY=newPosition.y;
        updateHitbox(mHitbox);
    }

    /**
     * Damages entity, and decreases its hp;
     * Applies knockback (2 times width) to this to the given direction.
     * @see #damage()
     * @param knockback direction to apply knockback to.
     */
    protected void hit(Direction knockbackDirection, int knockback) {
        damage();
        if(knockbackDirection!=Direction.NONE)
            moveSafely((knockbackDirection==Direction.LEFT?-knockback:knockback), 0);
    }

    @Override
    public boolean isSolid() {
        return mIsSolid;
    }

    // TODO WTF IS WRONG !?
    public <T extends Renderable.Drawer> T getDrawer(Class<T> drawerClass) throws ClassCastException {
        if(mDrawer==null) mDrawer = createDrawer();
        return drawerClass.cast(mDrawer);
    }

    /**
     * Executes {@link #hit(Direction, int)} if entity is alive.
     */
    public final void hitEntity(Direction direction, int knockback) {
        if(isGarbage()) {
            hit(direction, knockback);
        }
    }

    /**
     * Applies damage to entity.
     * If entity's becomes less than 0, then {@link #kill()} is getting called.
     * @see #makeGarbage()
     */
    public void damage() {
        hp--;
        if(hp<=0) makeGarbage();
    }

    /**
     * Returns current hitbox.
     * @return current hitbox as {@link java.awt.Rectangle}
     */
    public Rectangle getHitbox() {
        return mHitbox;
    }

    /**
     * Event to listen type.
     * @see #addEventListener(EventType, Listener)
     */
    public enum EventType {
        DASH,
        JUMP,
        FALL
    }

    /**
     * Adds {@link Listener} for specific actions.
     * the {@code keyDown} method gets called when action starts,
     * while the {@code keyUp} method gets called when actions ends.
     * @see Listener
     * @param type the type of event type to listen for.
     * @param listener listener that listens to event with given type.
     */
    public void addEventListener(EventType type, Listener listener) {
        java.util.List<Listener> listeners=switch (type) {
            case DASH -> mDashListeners;
            case FALL -> mFallListeners;
            case JUMP -> mJumpListeners;
        };
        listeners.add(listener);
        System.out.println("[DEBUG] Added "+type+" listener on entity "+this);
    }

    /**
     * When called entity will start falling and
     * {@link #mVelocityY} will increase.
     * When entity touch another solid <code>Entity</code> it will stop.
     */
    public void startFalling() {
        if(!mFalling) {
            mFallListeners.forEach(Listener::keyDown);
            mVelocityY = 0;
            mFalling = true;
        }
    }

    /**
     * Makes the entity jump.
     * Entity goes UP, {@link #mVelocityY} decreases and entity moves faster.
     * @throws IllegalStateException if the entity is static.
     * @see #mStatic
     * @see #handleJump()
     */
    public void jump() throws IllegalStateException {
        if(mStatic)
            throw new IllegalStateException("Attempting to call jump on a static entity: "+this);
        if(!mOnJump && !mFalling && !mOnDash) {
            mJumpListeners.forEach(Listener::keyDown);
            mCurrentSpeed++;
            mVelocityY =height/7f;
            mOnJump =true;
        }
    }

     /**
     * Called on each frame and updates jump.
     * Decreases {@link #mVelocityY}.
     * If entity collides with another solid entity or reaches top of {@link AbstractGameScreen},
     * then entity stops jumping.
     * @see #handleFall()
     * If entity stops jumping then it starts falling.
     */
    private void handleJump() {
        if(mOnJump) {
            mVelocityY -=0.4f;
            moveUnsafely(0, (int)-mVelocityY);
            context.getMap(Map.class).getColliderOf(this).ifPresent(c -> {
                mVelocityY =0;
                worldY=c.getWorldY()+c.getHeight();
            });
            if(worldY<=0) {
                mVelocityY =0;
                worldY=0;
            }
            // stops jumping
            if(mVelocityY <=0) {
                mOnJump = false;
                mCurrentSpeed=mDefaultSpeed;
                mJumpListeners.forEach(Listener::keyUp);
                startFalling();
            }
        }
    }

    /**
     * Sets weight.
     * @param weight new weight.
     */
    public void setWeight(float weight) {
        this.mWeight=weight;
    }

    /**
     * Handles falling physics of entity.
     * When entity (if solid) reaches another solid entity the process
     * stops and the entity stands in the top of this solid entity.
     */
    private void handleFall() {
        if(mFalling) {
            mVelocityY += mWeight;
            moveUnsafely(0, (int) mVelocityY);
            context.getMap(Map.class).getColliderOf(this).ifPresent(tile -> {
                mFalling =false;
                worldY=tile.getWorldY()-height;
                mFallListeners.forEach(Listener::keyUp);
            });
        }
    }

    /**
     * Checks if there's some solid blocks below this.
     * If not {@link #startFalling()} method is being called.
     * Checks if entity is somehow inside some solid blocks,
     * if yes, this teleports to the top of its collider.
     */
    public void checkGround() {
        context.getMap(Map.class).getColliderOf(this).ifPresent((Entity e) -> {
            if(!mOnJump &&!mFalling) {
                worldY=e.getWorldY()-height;
            }
        });
        if(!mOnJump &&!mFalling) {
            int diff=5;
            worldY+=diff;
            updateHitbox(mHitbox);
            if (!context.getMap(Map.class).hasCollisionWith(this)) {
                startFalling();
            }
            worldY-=diff;
            updateHitbox(mHitbox);
        }
    }

    /**
     * Moves and checks current position.
     * If after movement collision happens with a solid block or entity,
     * it moves back to its original position.
     * @param stepsX steps to try to move horizontal
     * @param stepsY steps to try to move vertically
     * @return {@code true} if entity moved; {@code false} if entity returned
     * because collision detected.
     */
    public boolean moveSafely(int stepsX, int stepsY) {
        moveUnsafely(stepsX, stepsY);
        if(context.getMap(Map.class).hasCollisionWith(this)) {
            moveUnsafely(-stepsX, -stepsY);
            return false;
        }
        return true;
    }

    /**
     * Updates entity to the next frame.
     * If entity is not static physics, animation, direction e.t.c. gets updated,
     * if not, nothing gets updated except hitbox.
     */
    public void update() {
        if(!mStatic) {
            updateAnimation();
            handleFall();
            handleJump();
            mDirection=currentDirection();
            checkGround();

            if(mDashEndTime_millis <AbstractGame.gameTimeMillis()&&mCurrentSpeed>mDefaultSpeed&&mOnDash) {
                mOnDash=false;
                mCurrentSpeed=mDefaultSpeed;
                mDashListeners.forEach(Listener::keyUp);
                System.out.println("[DEBUG] Stopping dash on entity "+this);
            }
        }
        updateHitbox(mHitbox);
    }

    /**
     * If an animation is currently running, animation shows next step.
     * If animations ended, it gets removed.
     */
    public void updateAnimation() {
        if(mAnimation!=null) {
            mAnimation.update();
            if(!mAnimation.alive()) {
                mAnimating=false;
                mAnimation=null;
            }
        }
    }

    /**
     * Animates this entity.
     * Hitbox will be affected.
     * @param anim animation to do.
     */
    public void animate(Animation anim) {
        if(!mAnimating&&anim!=null) {
            mAnimating = true;
            anim.setAnimatable(this);
            mAnimation = anim;
        }
    }

    /**
     * Increases the speed of player by diff
     * and slowly resets it to {@link #mDefaultSpeed} after given millis.
     * @param diff speed to add to {@link #mCurrentSpeed}
     * @param millis speed will be reset to default speed after
     */
    public void dash(int diff, long millis) {
        if(!mOnDash && !mOnJump) {
            mOnDash=true;
            mDashListeners.forEach(Listener::keyDown);
            mCurrentSpeed += diff;
            mDashEndTime_millis = AbstractGame.gameTimeMillis() + millis;
            System.out.println("[DEBUG] Starting dash on entity "+this);
        }
    }

//    /**
//     * Returns related drawer cast to given class.
//     * @param clazz class to cast drawer.
//     * @return Returns {@code drawer} as given class object.
//     * @param <T> Type to cast {@code drawer}.
//     * @throws ClassCastException if {@code drawer} is not an instance of given class.
//     */
//    public final <T extends AbstractDrawer> T getDrawer(Class<T> clazz) throws ClassCastException {
//        if(mDrawer ==null) mDrawer =createDrawer();
//        return clazz.cast(mDrawer);
//    }

    /**
     * Initializes the global fields based on the given {@link java.awt.Rectangle}.
     * @param hitbox given values with by rectangle.
     */
    private void configVars(Rectangle hitbox) {
        worldX=hitbox.x;
        worldY=hitbox.y;
        width=hitbox.width;
        height=hitbox.height;
        updateHitbox(hitbox);
    }

    /**
     * **NOT RECOMMENDED TO USE**
     * Sets current direction to be equal to given direction.
     * @param direction direction to set {@code mDirection} equal to.
     */
    protected void setDirection(Direction direction) {
        this.mDirection=direction;
    }

    /**
     * Returns entity's current hp.
     * @return entity's hp as integer.
     */
    public int getHp() {
        return hp;
    }

    /**
     * Sets entity's hp.
     * @param hp new entity's hp.
     */
    public void setHp(int hp) {
        this.hp=hp;
    }

    /**
     * Kills entity if entity is alive.
     * @see #kill()
     */
    public final void makeGarbage() {
        if(mIsAlive)
            kill();
    }

    /**
     * Kills this.
     */
    protected void kill() {
        mIsAlive = false;
        System.out.println(getDeathMessage());
        context.getDisplay(DisplayableDrawer.class).removeRenderable(this);
    }

    /**
     * Method returns message depending on which way the entity died.
     * @return returns a string containing a message e.g. entity fell out of the world.
     * @throws IllegalStateException if the entity is alive but method is called.
     */
    public String getDeathMessage() throws IllegalStateException {
        if(mIsAlive) throw new IllegalStateException("Entity is currently alive.");
        StringBuilder deathMessageStringBuilder=new StringBuilder("[DEBUG] Entity "+this);
        if(worldY>context.getDisplay(DisplayableDrawer.class).dimension().height) {
            Point p=getPosition();
            deathMessageStringBuilder.append(" fell out of the world! (").append(p.x).append(",")
                    .append(p.y).append(").");
        } else
            deathMessageStringBuilder.append(" got killed!");
        return deathMessageStringBuilder.toString();
    }

    /**
     * Returns entity's current width.
     * @return entity's current width as integer.
     */
    public final int getWidth() {
        return width;
    }

    /**
     * Returns entity's current height.
     * @return entity's current height as {@code integer}.
     */
    public final int getHeight() {
        return height;
    }

    /**
     * Returns entity's current worldX.
     * @return entity's current worldX as {@code integer}.
     */
    public final int getWorldX() {
        return worldX;
    }

    /**
     * Returns entity's current worldY.
     * @return entity's current worldY as {@code integer}.
     */
    public final int getWorldY() {
        return worldY;
    }

    /**
     * Returns entity's current direction.
     * @return entity's current direction as {@link Direction}.
     */
    public final Direction getDirection() {
        return mDirection;
    }

    /**
     * Moves the entity based on given steps.
     * If collision occurs entity does nothing.
     * @param stepsH horizontal movement.
     * @param stepsV vertical movement.
     */
    public void moveUnsafely(int stepsH, int stepsV) {
        worldX += stepsH;
        worldY += stepsV;
        updateHitbox(mHitbox);
    }

    /**
     * Checking if the entity collides with another entity (hitbox based).
     * @param other possible collider.
     * @return returns true if other collides this.
     * @see #updateHitbox(Rectangle)
     */
    public final boolean hasCollisionWith(Collidable other) {
        if(equals(other)||other==null)
            return false;
        return mHitbox.intersects(other.getHitbox());
    }

    /**
     * Returns current direction of this.
     * @return direction entity has.
     */
    protected abstract Direction currentDirection();

    /**
     * Updates hitbox.
     * Subclass must set new bounds to given hitbox.
     * @param hitbox hitbox to update.
     */
    public abstract void updateHitbox(Rectangle hitbox);

    /**
     * Asks for initial config by subclass.
     * @return {@link R_Config} instance that contains configuration for entity.
     */
    public abstract R_Config initialConfig();

    /**
     * Subclass must return a {@link Drawer} implementation.
     * Method gets called to get new drawer when {@code mDrawer} is null.
     * @see Drawer
     * @return implementation of drawer for drawing this entity.
     */
    protected abstract Drawer createDrawer();

    /**
     * Fills the given element with properties that entity wants to restore after {@code createManifest}
     * method is called on {@link ManifestManager} class.
     * Only this class needs to know the names of attributes e.t.c. of the tag.
     * @param element element to fill with properties that entity wants to restore after save.
     * @see #loadFromXmlElement(Element)
     * @see ManifestManager
     */
    public abstract void createXmlElement(Element element);

    /**
     * Loads properties from given xml tag.
     * @param element element from which the data will be restored.
     * @see #createXmlElement(Element)
     * @see ManifestManager
     */
    public abstract void loadFromXmlElement(Element element);

    @Deprecated(forRemoval = true)
    public static <T extends AbstractEntity> T createEntityByXML(Element element, Class<T> clazz, Object... initArgs)
            throws InvocationTargetException, InstantiationException, IllegalAccessException {

        // Array defines the type of the parameters.
        Class<?>[] initArgsTypes=new Class<?>[initArgs.length];
        for (int i = 0; i < initArgs.length; i++) {
            initArgsTypes[i] = initArgs[i].getClass();
        }
        try {
            // create instance of T
            Constructor<T> constructor = clazz.getConstructor(initArgsTypes);
            T entity = constructor.newInstance(initArgs);
            // load given element if not null
            if(element!=null)
                entity.loadFromXmlElement(element);
            return entity;
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Cannot found constructor with given args types.", e);
        }
    }

    /**
     * {@return a string representation of the object}
     * <p>
     * Satisfying this method's contract implies a non-{@code null}
     * result must be returned.
     *
     * @apiNote In general, the
     * {@code toString} method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * It is recommended that all subclasses override this method.
     * The string output is not necessarily stable over time or across
     * JVM invocations.
     * @implSpec The {@code toString} method for class {@code Object}
     * returns a string consisting of the name of the class of which the
     * object is an instance, the at-sign character `{@code @}', and
     * the unsigned hexadecimal representation of the hash code of the
     * object. In other words, this method returns a string equal to the
     * value of:
     * {@snippet lang = java:
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     *}
     * The {@link Objects#toIdentityString(Object)
     * Objects.toIdentityString} method returns the string for an
     * object equal to the string that would be returned if neither
     * the {@code toString} nor {@code hashCode} methods were
     * overridden by the object's class.
     */
    @Override
    public String toString() {
        return String.format("E:%s,x%dy%d,h%d,sol%B,sta%B", getClass().getSimpleName(), worldX, worldY, hashCode(), mIsSolid, mStatic);
    }

    /**
     * Adds drawer to main game screen.
     * Starts drawing entity until entity is killed.
     */
    public Entity startRendering() {
        context.getDisplay(DisplayableDrawer.class).addEntitiesToDraw(this);
        return this;
    }

    /**
     * Initial config for creating an Entity.
     * @param hitbox the initial location and size of the {@code Entity}.
     * @param isSolid {@code true} if the entity solid. A solid entity collides with others.
     * @see com.engine.map.Map#hasCollisionWith(Collidable)
     * @param isStatic {@code true} if the entity static, A {@code static} entity does <i>not</i> fall, do any
     *                             animations or use physics, static entities can be moved only via
     *                             {@link #moveSafely(int, int)} <i>or</i> {@link #moveUnsafely(int, int)}.
     *                             A static entity's direction can be updated only via {@link #setDirection(Direction)}.
     * @see #update()
     */
    public record R_Config(Rectangle hitbox, boolean isSolid, boolean isStatic) {}

    /**
     * Responsible to draw the entity.
     * @see AbstractGameScreen
     */
    public abstract class AbstractDrawer implements Drawer {
        /**
         * If true, hitbox and debug things will be drawn.
         * @see #drawAll(Graphics2D)
         */
        public static boolean sDrawDebug =false;
        /**
         * The delay time on sprite change as milliseconds.
         * @see #nextSprite()  {@link #drawAll(Graphics2D)}
         */
        private long mChangeSpriteDelay_millis =400; // default value

        /**
         * Stores the last time a sprite has changed
         * @see #nextSprite()  {@link #drawAll(Graphics2D)}
         */
        private long mLastSpriteChangeTime_millis =AbstractGame.gameTimeMillis();

        /**
         * Sprites arrays for Directions.
         */
        private final java.util.Map<Direction, Image[]> mSpritesMap=new HashMap<>();

        /**
         * Store the index of the current sprite
         * @see #nextSprite()
         */
        private int mCurrentSprite_idx =0;

        /**
         * Current custom sprite
         */
        private Image mCustomSprite =null;

        /**
         * While greater than current time or game time, drawer will always use {@link #mCustomSprite}
         * If it's equal to -1 drawer will always draw {@link #mCustomSprite} until it's removed.
         */
        private long mCustomSpriteEndTime_millis;

        /**
         * No-args constructor, after creating object you must load sprites with
         * {@link #loadSprites(String, Direction, String...)} method.
         */
        public AbstractDrawer() {
        }

        /**
         * Uses this custom sprite until removed.
         * @param sprite sprite to use.
         * @see #removeCustomSprite()
         */
        public void useOnlyCustomSprite(Image sprite) {
            mCustomSprite=sprite;
            mCustomSpriteEndTime_millis=-1;
        }

        /**
         * Makes drawer use given sprite for given millis.
         * @param sprite sprite to use.
         * @param millis expiration time.
         */
        public void useCustomSprite(Image sprite, long millis) {
            mCustomSprite=sprite;
            mCustomSpriteEndTime_millis=AbstractGame.gameTimeMillis()+millis;
        }

        /**
         * Sets how long will the gap between sprite changes be.
         * @param millis sets how long it will take to change sprites.
         * @see #nextSprite()
         */
        public void setSpriteDelay(long millis) { mChangeSpriteDelay_millis=millis; }

        /**
         * Returns current sprite.
         * @return returns current sprite.
         * @throws ArrayIndexOutOfBoundsException if no sprites are loaded.
         */
        protected Image getCurrentSprite() throws ArrayIndexOutOfBoundsException {
            if(mCustomSprite!=null) return mCustomSprite;
            return mSpritesMap.get(getDirection())[mCurrentSprite_idx];
        }

        /**
         * Loads the next sprite if change sprite delay has ended.
         * if array ends, first sprite is loaded.
         */
        protected final void nextSprite() {
            if(mCustomSpriteEndTime_millis==-1)
                return;
            if(System.currentTimeMillis()>=mLastSpriteChangeTime_millis +mChangeSpriteDelay_millis) {
                mLastSpriteChangeTime_millis=System.currentTimeMillis();
                mCurrentSprite_idx++;
            }
            checkSpriteIdx();
        }

        /**
         * Check the current sprite dix.
         * if it's bigger or equal to the current direction related sprites array,
         * it will set to 0.
         * If using custom sprite forever, check will be ignored.
         * @see #nextSprite()
         * @see #mCurrentSprite_idx
         * @see #mCustomSprite
         */
        private void checkSpriteIdx() {
            if(mCustomSpriteEndTime_millis==-1) // using custom sprite until manually removed.
                return;
            Image[] sprites=mSpritesMap.get(getDirection());
            if(sprites!=null&&sprites.length<=mCurrentSprite_idx) {
                mCurrentSprite_idx = 0;
            }
        }

        /**
         * Erases old sprites and loads new.
         * @param resourcesRoot directory which contains all the sprites.
         * @param resources names of files.
         * @throws IOException when ImageIO.read() fails.
         * @throws IllegalArgumentException when ImageIO.read() fails.
         */
        public final void loadSprites(String resourcesRoot, Direction direction, String... resources) throws IOException, IllegalArgumentException {
            Function<Integer, Image> load= integer -> {
                try {
                    return ImageIO.read(Objects.requireNonNull(getClass().getResource(resourcesRoot + FileSystems.getDefault().getSeparator() + resources[integer])));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            };

            Image[] sprites=new Image[resources.length];
            for (int i = 0; i < sprites.length; i++) {
                sprites[i] = load.apply(i);
            }
            mSpritesMap.put(direction, sprites);
        }

        /**
         * Draws Entity to given graphics.
         * @param graphics graphics to draw the entity.
         */
        protected abstract void drawEntity(Graphics2D graphics);

        /**
         * Draws everything about related entity.
         * @param graphics graphics to draw stuff.
         */
        public final void drawAll(Graphics2D graphics) {
            // return if entity is not visible
            if (getWorldX()+getWidth() < 0 || getWorldX() > context.getDisplay(DisplayableDrawer.class).dimension().width) {
                return;
            }
            nextSprite();
            drawEntity(graphics);
            if(mCustomSprite!=null&&mCustomSpriteEndTime_millis!=-1&& mCustomSpriteEndTime_millis<AbstractGame.gameTimeMillis()) {
                mCustomSprite=null;
            }
            if(sDrawDebug) {
                graphics.setColor(Color.ORANGE);
                graphics.drawRect(worldX, worldY, width, height);
                graphics.setColor(Color.GREEN);
                graphics.drawRect(mHitbox.x, mHitbox.y, mHitbox.width, mHitbox.height);
                graphics.fillOval(worldX+width/2, worldY+height+5, 10, 10);
                drawString(AbstractEntity.this.toString().replace(',', '\n'), graphics, worldX, mHitbox.y);
            }
        }

        /**
         * Draws string to given graphics each line in a black background covering its width and height.
         * Differs line by comma or newline character.
         * First line's {@code top} {@code left} corner is located at the given {@code x} and {@code y}.
         * @param string String to draw at give graphics and coordinates.
         * @param graphics Graphics to draw the string to.
         * @param x X coordinate to draw string at.
         * @param y Y coordinate to draw string at.
         */
        public static void drawString(String string, Graphics2D graphics, int x, int y) {
            FontMetrics fontMetrics= graphics.getFontMetrics();
            int height=fontMetrics.getHeight();
            String[] lines=string.split("\n");
            for(String line: lines) {
                int lineWidth=fontMetrics.stringWidth(line);
                graphics.setColor(Color.BLACK);
                graphics.fillRect(x, y, lineWidth, height);
                y+=height;
                graphics.setColor(Color.WHITE);
                graphics.drawString(line, x, y);
            }
        }

        /**
         * Draws with default configuration in given graphics.
         * @param graphics graphics to draw the entity.
         */
        protected final void defaultDraw(Graphics2D graphics) {
            checkSpriteIdx();
            graphics.drawImage(getCurrentSprite(), worldX, worldY, width, height, null);
        }

        /**
         * Removes custom sprite.
         * Sprite and current sprite index check will continue.
         * @see #checkSpriteIdx()
         */
        public void removeCustomSprite() {
            mCustomSprite=null;
            mCustomSpriteEndTime_millis=0;
        }
    }
}
