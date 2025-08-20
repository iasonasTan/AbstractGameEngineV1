package com.engine.animation;

import com.engine.AbstractGame;

/**
 * Example implementation of Animation.
 * Squeeze animation on entities.
 */
public final class SqueezeAnimation implements Animation {
    private Animatable mAnimatable;

    // Orientation to squeeze.
    private final Orientation mOrientation;

    // Current direction of animatable entity.
    private Direction mDirection=Direction.NONE;

    // Default with and height of animatable entity. common.
    private int mEntityDefaultWidth;
    private int mEntityDefaultHeight;

    // Gap to add on sides.
    private final int SIDES_GAP = 2;

    // End of half of animation.
    private final long HALF_OF_TIME;

    // Animation end time.
    private final long END_TIME;

    public SqueezeAnimation(Orientation type, Direction direction) {
        this(type);
        if(type==Orientation.VERTICAL)
            throw new IllegalArgumentException();
        this.mDirection =direction;
    }

    public SqueezeAnimation(Orientation type) {
        mOrientation =type;
        long mStartTime = AbstractGame.gameTimeMillis();
        long TOTAL_TIME = 250;
        END_TIME = mStartTime + TOTAL_TIME;
        HALF_OF_TIME = mStartTime + TOTAL_TIME /2;
    }

    @Override
    public void setAnimatable(Animatable entity) {
        this.mAnimatable = entity;
        mEntityDefaultWidth = entity.getWidth();
        mEntityDefaultHeight = entity.getHeight();
    }

    @Override
    public boolean alive() {
        return AbstractGame.gameTimeMillis()< END_TIME;
    }

    private void updateVertical(boolean ON_PHASE_A) {
        if(ON_PHASE_A) {
            mAnimatable.changeHeight(-SIDES_GAP);
            mAnimatable.changeY(SIDES_GAP);
        } else if (mAnimatable.getHeight()<mEntityDefaultHeight) {
            mAnimatable.changeHeight(SIDES_GAP);
            mAnimatable.changeY(-SIDES_GAP);
        }
    }

    private void updateHorizontal(boolean ON_PHASE_A) {
        if(ON_PHASE_A) {
            mAnimatable.changeWidth(-SIDES_GAP);
            if(mDirection ==Direction.RIGHT)
                mAnimatable.changeX(SIDES_GAP);
        } else if (mAnimatable.getWidth()<mEntityDefaultWidth) {
            mAnimatable.changeWidth(SIDES_GAP);
            if(mDirection ==Direction.RIGHT)
                mAnimatable.changeX(-SIDES_GAP);
        }
    }

    @Override
    public void update() {
        if(mAnimatable==null)
            throw new IllegalStateException("Define animatable first.");
        final long CURR_T=AbstractGame.gameTimeMillis();
        final boolean ON_PHASE_A=CURR_T< HALF_OF_TIME;
        switch (mOrientation) {
            case VERTICAL -> updateVertical(ON_PHASE_A);
            case HORIZONTAL -> updateHorizontal(ON_PHASE_A);
        }
    }
}
