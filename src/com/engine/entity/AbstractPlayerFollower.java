package com.engine.entity;

import com.engine.map.Map;
import com.engine.AbstractGame;
import com.engine.animation.Direction;
import com.engine.data.NoRepeatList;
import com.engine.map.HorizontalMap;

import java.awt.*;

/**
 * Entity that follows player.
 * Takes the same path player took.
 */
public abstract class AbstractPlayerFollower extends AbstractEntity {
    public static volatile int sIterationsPerUpdate=0;

    /**
     * Next target x to follow.
     * @see #putStep(Point)
     */
    protected int targetX;

    /**
     * Next target y.
     * @see #putStep(Point)
     */
    protected int targetY;

    /**
     * Path to player.
     * Following one target at a time.
     * Motified version to make path sorter.
     */
    protected final java.util.List<Point> mPathToPlayer = new NoRepeatList<>() {
        @Override
        public boolean add(Point point) {
            boolean out = super.add(point);
            check();
            return out;
        }

        /**
         * Methods that checks all indexes one-by-one
         * @see #check(int)
         */
        private void check() {
            for (int i = 0; i < size(); i++) {
                check(i);
            }
        }

        /**
         * Returns the element at the specified position in this list.
         *
         * @param index index of the element to return
         * @return the element at the specified position in this list
         * @throws IndexOutOfBoundsException {@inheritDoc}
         */
        @Override
        public Point get(int index) {
            sIterationsPerUpdate++;
            return super.get(index);
        }

        /**
         * Check if data return to the exact point of the one store in the given index.
         * @param i index to point to check.
         */
        private void check(int i) {
            Point ip = get(i);
            int start = i;
            int end = i;
            for (Point point : this) {
                int diff=2;
                int xL=ip.x-diff, xR=ip.x+diff;
                int yL=ip.y-diff, yR=ip.y+diff;
                if (point.x>xL&&point.x<xR&&yL<point.y&&yR>point.y) {
                    end = indexOf(point);
                }
            }
            if (end > start) {
                for (; start < end; start++) {
                    remove(i);
                }
            }
        }
    };

    /**
     * Context constructor.
     * @param context context as an AbstractGame.
     */
    public AbstractPlayerFollower(AbstractGame context) {
        super(context);
        targetX=initialTarget();
    }

    /**
     * Initial target x coordinate.
     * @return returns x coordinate of target as integer.
     */
    public abstract int initialTarget();


    /**
     * Updates entity to the next frame.
     * If entity is not static physics, animation, direction e.t.c. gets updated,
     * if not, nothing gets updated except hitbox.
     * Follows current {@link #targetX}.
     * If targetX reached then follows next taget from {@link #mPathToPlayer}.
     * If it finds any block method {@link #jump()} is being called.
     */
    @Override
    public void update() {
        super.update();
        if(!mPathToPlayer.isEmpty()) {
            int posL=worldX-width;
            int posR=worldX+width;
            if(targetX>posL&&targetX<posR) {
                int diff=width/2;
                Point nextTargetPoint=mPathToPlayer.removeFirst();
                targetX= nextTargetPoint.x;
                targetY=nextTargetPoint.y;
                if(getDirection()==Direction.LEFT)
                    targetX-=diff;
                else
                    targetX+=diff;
            }
        }
        int steps=getCurrentSpeed()*(worldX<targetX?1:-1);
        worldX+=steps;
        moveUnsafely(0, 0); // update hitbox
        if(context.getMap(Map.class).containsCollisionWith(this)) {
            worldX-=steps;
            jump();
            moveUnsafely(0, 0); // update hitbox
        }
        if(!context.getMap(HorizontalMap.class).willEntityTouchGround(this, height)&& targetY<worldY) jump();
        //System.out.println("Iterations per update: "+sIterationsPerUpdate);
        sIterationsPerUpdate=0;
    }

    /**
     * Moves path entity will take.
     * @param stepsH steps to move path.
     */
    public void movePath(int stepsH) {
        mPathToPlayer.replaceAll(point -> {
            Point point1=new Point(point);
            point1.x+=stepsH;
            return point1;
        });
        targetX+=stepsH;
    }

    /**
     * Returns current direction of this.
     * @return direction entity has.
     */
    @Override
    protected final Direction currentDirection() {
        if(targetX>worldX)
            return Direction.RIGHT;
        else
            return Direction.LEFT;
    }

    /**
     * Adds a step to {@link #mPathToPlayer}.
     * @param point point to follow later to reach player.
     */
    public final void putStep(Point point) {
        mPathToPlayer.add(point);
    }

}
