package com.engine;

import com.engine.data.UniqueInsertMap;
import com.engine.entity.*;
import com.engine.event.KeyListener;
import com.engine.map.AbstractMap;
import com.engine.view.AbstractGameScreen;
import com.engine.view.DisplayableDrawer;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Abstract Game.
 * Gets implementations by using abstract methods. e.g. {@link #createGameScreen()}.
 * This class defines some instances of common classes via abstract methods.
 */
@SuppressWarnings("unused")
public abstract class AbstractGame implements Runnable, Context {
    /**
     * Game-related timer system.
     * Timer pauses when game stops.
     * @see #run()
     */
    private static long smGameTime = 0, smFromStartTime = 0, smPauseTime = 0;

    /**
     * Flag is {@code true} while game thread is running, {@code false} otherwise.
     */
    private volatile boolean mRunning;

    /**
     * A {@link UniqueInsertMap} that holds {@code entities} in game.
     * @see #getEntity(String, Class)
     * @see #addEntity(String, AbstractEntity)
     */
    private final UniqueInsertMap<String, Entity> mEntities = new UniqueInsertMap<>();

    /**
     * Map that holds {@code entity managers} in game.
     * User may want to add custom entity managers except of entities.
     * Added entities will be updated and drawn.
     * You can access added entity managers using method {@link #getEntityManager(String)}
     * or {@link #getEntityManager(String, Class)} to cast before return.
     */
    private final UniqueInsertMap<String, EntityManager<? extends Entity>> mEntityManagers=new UniqueInsertMap<>();

    /**
     * Default game screen.
     * @see AbstractGameScreen
     */
    private final DisplayableDrawer mDisplay;

    /**
     * Main key listener.
     * @see KeyListener
     */
    private final KeyListener mKeyListener;

    /**
     * MapHandler related to current game.
     * @see Map
     */
    private final com.engine.map.Map mMap;

    /**
     * Main player inside game.
     * Initialized one time with {@link #createPlayer()}
     * Accessible via {@link #getPlayer(Class)} by context wrappers.
     */
    private final Player mPlayer;

    /**
     * Current fps.
     * @see #setFPS(int)
     * Must restart game thread for changes to take effect.
     */
    private int mFPS = 60; // default value

    /**
     * Game thread holding loop.
     */
    private Thread mGameThread;

    /**
     * Main no-args constructor.
     * Initializes a player and common instances.
     * Adds keyListener.
     */
    public AbstractGame() {
        mKeyListener = createKeyHandler();
        mDisplay = createGameScreen();
        mPlayer = createPlayer();
        mPlayer.startDrawing();
        mMap = createMapHandler();

        mDisplay.addKeyListener(mKeyListener);
        mDisplay.gainFocus();
    }

    /**
     * Abstract method used to get {@link AbstractStaticPlayer} implementation from subclass.
     * @return Subclass's {@code Player} implementation of abstract class.
     */
    protected abstract Player createPlayer();

    /**
     * Abstract method used to get {@link AbstractMap} implementation from subclass.
     *
     * @return Subclass's {@code MapHandler} implementation of abstract class.
     */
    protected abstract com.engine.map.Map createMapHandler();

    /**
     * Abstract method used to get {@link AbstractGameScreen} implementation from subclass.
     * @return Subclass's {@code GameScreen} implementation of abstract class.
     */
    protected abstract DisplayableDrawer createGameScreen();

    /**
     * Abstract method used to get {@link KeyListener} instance from subclass.
     * @return Subclass's {@code KeyListener} instance.
     */
    protected abstract KeyListener createKeyHandler();

    /**
     * Adds an entity to the map.
     * if name is already in use entity will not be added.
     * @param name name of entity, useful to find entity later if needed.
     * @param entity entity implementation to add.
     * @return returns this, allowing chain calls.
     */
    public Context addEntity(String name, Entity entity) {
        mEntities.putPair(name, entity);
        return this;
    }

    /**
     * Starts updating given entity manager.
     * @param id manager id.
     * @param entityManager entity manger to start updating.
     */
    public void addEntityManager(String id, EntityManager<? extends Entity> entityManager) {
        mEntityManagers.putPair(id, entityManager);
    }

    /**
     * Returns entity manager with given id.
     * @param id entity manager's id.
     * @return Optional containing EntityManager OR null.
     * @see #mEntityManagers
     */
    public final Optional<EntityManager<?>> getEntityManager(String id) {
        return Optional.ofNullable(mEntityManagers.get(id));
    }

    @Override
    public <T extends DisplayableDrawer> T getDisplay(Class<T> clazz) throws ClassCastException {
        return clazz.cast(mDisplay);
    }

    @Override
    public <T extends java.awt.event.KeyListener> T getKeyListener(Class<T> clazz) throws ClassCastException {
        return clazz.cast(mKeyListener);
    }

    /**
     * Returns an {@link Optional} containing the {@link DefaultEntityManager} of the given type and ID, if it exists.
     * @param id the ID of the entity manager to retrieve
     * @param clazz the expected class of the entity manager.
     * @param <T> the type of entity manager.
     * @return an {@link Optional} containing the entity manager if found.
     * @throws ClassCastException if found {@code EntityManager} cannot be cast to given type.
     * @see #mEntityManagers
     */
    public final <T extends EntityManager<? extends Entity>> Optional<T> getEntityManager(String id, Class<T> clazz) throws ClassCastException {
        return Optional.ofNullable(clazz.cast(mEntityManagers.get(id)));
    }

    /**
     * Updates all entities in game and removed dead ones.
     */
    private void updateEverything() {
        forEachEntity(Entity::update);
        forEachEntityManager(EntityManager::updateEntities);
        getMap(com.engine.map.Map.class).updateEntities();
        mPlayer.update();

        // removed dead entities
        mEntities.values().removeIf(entity -> !entity.isGarbage());
    }

    /**
     * Starts game thread.
     * runs {@link #run()} on a new thread.
     * renders and updates {@link #mFPS} times per second.
     */
    public void startGameThread() {
        System.out.println("[DEBUG] Game started...");
        mGameThread = new Thread(this);
        mRunning=true;
        mGameThread.start();
    }

    @Override
    public <T extends com.engine.map.Map> T getMap(Class<T> clazz) throws ClassCastException {
        return clazz.cast(mMap);
    }

    /**
     * Returns if game is currently running.
     * @return {@code true} if game thread is currently running, {@code false} otherwise.
     */
    public boolean isRunning() {
        return mRunning;
    }

    /**
     * Stops rendering and updating.
     */
    public void stopGameThread() {
        System.out.println("[DEBUG] Game stopped...");
        mRunning=false;
        mGameThread = null;
    }

    /**
     * game loop.
     * updates and renders {@link #mFPS} times per second.
     *
     * @see #startGameThread()
     */
    @Override
public void run() {
        long currentTime;
        long previousTime;
        long elapsedTime;
        long delay;
        final long FRAME_RATE = 1000 / mFPS;
        smFromStartTime += System.currentTimeMillis() - smPauseTime;
        System.out.println("[DEBUG] Game loop started.");

        while (mRunning) {
            previousTime = System.currentTimeMillis();

            updateEverything();
            mDisplay.renderEverything();

            smGameTime = System.currentTimeMillis() - smFromStartTime;

            currentTime = System.currentTimeMillis();
            elapsedTime = currentTime - previousTime;
            delay = elapsedTime - FRAME_RATE;

            try {
                // noinspection all
                Thread.sleep(Math.max(Math.abs(delay), 0));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("[DEBUG] Game loop stopped.");
        smPauseTime = System.currentTimeMillis();
    }

    /**
     * Returns game time.
     * @return time since begin of the game. except
     * @see #smGameTime
     */
    public static long gameTimeMillis() {
        return smGameTime;
    }

    /**
     * Returns player and automatically casts it to given type.
     * @param clazz actual type of player.
     * @return player cast to given type.
     * @param <T> actual type of player.
     * @throws ClassCastException if player isn't instance of given class.
     */
    public <T extends Player> T getPlayer(Class<T> clazz) throws ClassCastException {
        return clazz.cast(mPlayer);
    }

    /**
     * Sets game fps to given fps.
     * Must restart game loop for changes to take effect.
     * @param fps new game fps.
     */
    public void setFPS(int fps) {
        mFPS = fps;
    }

    /**
     * Returns Entity with given id wrapped in an {@link Optional}.
     * @param name entity's id.
     * @return optional containing entity or null.
     */
    public final Optional<Entity> getEntity(String name) {
        return Optional.ofNullable(mEntities.get(name));
    }

    /**
     * Returns entity from game and casts into given type.
     * @param name id of the entity.
     * @param clazz class to cast the entity while passing.
     * @return returns entity cast to given type wrapped in an optional.
     * @param <T> type to cast entity.
     * @throws ClassCastException if entity cannot be cast to given type.
     */
    public final <T extends AbstractEntity> Optional<T> getEntity(String name, Class<T> clazz) throws ClassCastException {
        return Optional.ofNullable(clazz.cast(mEntities.get(name)));
    }

    /**
     * Moves all entities horizontal and vertical depending on the given steps.
     * @param stepsV steps to move entities vertical.
     * @param stepsH steps to move entities horizontal.
     */
    public void moveEntities(int stepsV, int stepsH) {
        mEntities.forEach((_, v) -> v.moveUnsafely(stepsV, stepsH));
    }

    /**
     * Iterates through all {@link EntityManager} instances and applies the given action.
     * @param entityManagerConsumer the action to perform on each {@code EntityManager}.
     */
    public final synchronized void forEachEntityManager(Consumer<EntityManager<? extends Entity>> entityManagerConsumer) {
        EntityManager<?>[] entityManagers=mEntityManagers.values().toArray(new EntityManager[0]);
        for (EntityManager<?> entityManager: entityManagers) {
            entityManagerConsumer.accept(entityManager);
        }
    }

    /**
     * Iterates through all {@link AbstractEntity} instances and applies the given action.
     * @param consumer the action to perform on each {@code AbstractEntity}.
     */
    public final synchronized void forEachEntity(Consumer<Entity> consumer) {
        Entity[] entities=mEntities.values().toArray(new AbstractEntity[0]);
        for (Entity entity: entities) {
            consumer.accept(entity);
        }
    }

    /**
     * Iterates through all {@link AbstractEntity} instances,
     * if is instanceof given type given action gets applied to it.
     * @param consumer the action to perform on each cast instance.
     * @param type     required type to apply given action on instances.
     * @param <T>      required type to cast and apply.
     */
    public final synchronized <T extends Entity> void forEachEntity(Consumer<T> consumer, Class<T> type) {
        forEachEntity(e -> {
            if (type.isInstance(e))
                consumer.accept(type.cast(e));
        });
    }

    /**
     * Counts the number of entities that are instances of the given class.
     * @param clazz the class to match against entity types.
     * @return the number of entities that are instances of the given class.
     */
    public int countEntitiesOfType(Class<?> clazz) {
        Collection<Entity> entities=mEntities.values();
        int counter=0;
        for (Entity entity: entities) {
            if(clazz.isInstance(entity)) {
                counter++;
            }
        }
        return counter;
    }

    /**
     * Returns entities map.
     * @return entities map as unmodifiable {@link Map}.
     */
    public final Map<String, Entity> getEntitiesMap() {
        return Collections.unmodifiableMap(mEntities);
    }

    /**
     * Returns entity managers map.
     * @return entity managers map as unmodifiable {@link Map}.
     */
    public final Map<String, EntityManager<?>> getEntityManagersMap() {
        return Collections.unmodifiableMap(mEntityManagers);
    }

    /**
     * Returns the configured target FPS of the game.
     * <p><b>WARNING:</b> This does <i>not</i> return the current
     * frame rate the game is running at, but simply the value stored
     * in {@link #mFPS}.
     * <p>If you change the FPS using {@link #setFPS(int)}, you must
     * restart the game thread for the change to take effect.
     *
     * @return the value of {@code mFPS}
     * @see #setFPS(int)
     * @see #startGameThread()
     */
    public int getFPS() {
        return mFPS;
    }

    /**
     * Checks for collision between given entity and x entity added to this context.
     * Check inside all entities and all entities in all entity managers.
     * @param entity entity to check if collides with another entity in this context.
     * @return {@code true} if collision is detected between given entity and x entity of game; {@code false} otherwise.
     */
    public boolean collidesWithXEntity(AbstractEntity entity) {
        for (Entity e: mEntities.values()) {
            if(e.hasCollisionWith(entity)) {
                return true;
            }
        }
        for(EntityManager<?> entityManager: mEntityManagers.values()) {
            if(entityManager.containsCollisionWith(entity)) {
                return true;
            }
        }
        return false;
    }

}
