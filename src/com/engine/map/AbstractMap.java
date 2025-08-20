package com.engine.map;

import com.engine.animation.Direction;
import com.engine.entity.DefaultEntityManager;
import com.engine.AbstractGame;
import com.engine.entity.Entity;
import com.engine.view.DisplayableDrawer;

import java.awt.*;
import java.nio.file.Path;
import java.util.List;

/**
 * Abstract map handler.
 * Manages the map.
 */
public abstract class AbstractMap extends DefaultEntityManager<Tile> implements HorizontalMap {

    /**
     * Context constructor.
     * @param context game containing all useful stuff.
     */
    public AbstractMap(AbstractGame context) {
        super(context);
        context.getDisplay(DisplayableDrawer.class).addEntitiesToDraw(toArray(new Tile[0]));
    }

    /**
     * Initializes tiles.
     * Method can be called outside of class.
     */
    public void createTerrain(Path path) {
        createTerrain(this, path);
    }

    /**
     * Forces the user to create some tiles and add them into the given {@link java.util.List}
     * @param tiles list to add the tiles.
     */
    public abstract void createTerrain(List<Tile> tiles, Path mapFile);

    /**
     * Returns if the give entity will collide with a tile.
     * @param entity the entity to check
     * @param offsetY distance to move entity down, e.g. entity's height is the default value so entity checks for all blocks.
     * @return {@code true} if next ground tile exists in the map, {@code false} otherwise.
     */
    public boolean willEntityTouchGround(Entity entity, int offsetY) {
        if (entity.getDirection() == Direction.NONE) return true;
        final Point originalPosition=new Point(entity.getPosition());
        final int DIFF=entity.getCurrentSpeed()+2;
        int offsetX=(entity.getDirection()==Direction.RIGHT)?DIFF:-DIFF;
        entity.moveUnsafely(offsetX, offsetY);
        boolean nextGroundTileExists = containsCollisionWith(entity); // moved entity still collides with a tile
        entity.setPosition(originalPosition); // reset entity's position
        return nextGroundTileExists;
    }


    /**
     * Returns the position of the top left tile of the map.
     * If map has no tiles, empty point gets returned.
     * @return point containing x and y of first tile.
     */
    public Point getPosition() {
        if (isEmpty()) return new Point();
        Point topLeft = get(0).getPosition();
        for (int i = 1; i < size(); i++) {
            Point p = get(i).getPosition();
            if (p.y < topLeft.y || (p.y == topLeft.y && p.x < topLeft.x)) {
                topLeft = p;
            }
        }
        return topLeft;
    }

    // ORIGINAL METHOD
//    private Point getBlockPosition(int mouseX, int mouseY) {
//        int tileW = MainTile.sDefaultTileSize.width;
//        int tileH = MainTile.sDefaultTileSize.height;
//        Point worldPos=context.map.position();
//
//        int blockY=mouseY/tileH*tileH;
//        int blockX=worldPos.x;
//        while(blockX<mouseX) {
//            blockX+=tileW;
//        }
//        blockX-=tileW;
//
//        return new Point(blockX, blockY);
//    }
}
