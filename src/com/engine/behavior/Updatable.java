package com.engine.behavior;

public interface Updatable {
    /**
     * Updates entity.
     * Called by game thread FPS times per sec.
     */
    void update();

    /**
     * Says if entity is garbage, if yes it's manager has to wipe it from memory.
     * @return return {@code true} if entity is garbage; {@code false} otherwise.
     */
    boolean isGarbage();

    /**
     * Makes entity garbage, which means no update OR draw wil happen.
     * It's manager has to remove it after update.
     */
    void makeGarbage();
}
