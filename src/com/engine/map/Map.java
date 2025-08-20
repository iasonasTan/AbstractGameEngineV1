package com.engine.map;

import com.engine.Context;
import com.engine.entity.EntityManager;

import java.awt.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Optional;

public interface Map extends EntityManager<Tile> {
    void createTerrain(Path mapFile);
    Point getPosition();

    /**
     * Places a tile of the given type at the specified coordinates within the game grid.
     * <p>
     * The specified tile class must have a public constructor with the signature:
     * {@code (AbstractGame context, Point position)}.
     * </p>
     *
     * @param cords the raw screen coordinates where the tile is to be placed
     * @param tileSize the size of the tile grid block
     * @param tileClass the class of the tile to instantiate; must extend {@code AbstractTile}
     *
     * @throws NoSuchMethodException if the required constructor is not found in the specified tile class
     * @throws InvocationTargetException if the constructor itself throws an exception during instantiation
     * @throws InstantiationException if the tile class is abstract or cannot be instantiated
     * @throws IllegalAccessException if the constructor is inaccessible (e.g. not public)
     */
    static <T extends Tile> T getTile(Context context, Map map, Point cords, Dimension tileSize, Class<T> tileClass)
            throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        // check if block already exists
        if(getBlockAt(map, cords).isPresent()) return null;
        Constructor<? extends Tile> tileConstructor=tileClass.getConstructor(Context.class, Point.class);
        Point position=getBlockPosition(map, cords, tileSize);
        Tile tile=tileConstructor.newInstance(context, position);
        tile.moveUnsafely(0, 0); // update rect
        map.add(tile);
        tile.startDrawing();
        return tileClass.cast(tile);
    }

    /**
     * Returns tile that intersects with given coordinates wrapped in an {@link Optional}.
     * @param blockPos pos to check which block intersects with.
     * @return returns Optional containing block that intersects given point, empty optional if no block found.
     */
    static Optional<Tile> getBlockAt(Map map, Point blockPos) {
        Rectangle rectangle=new Rectangle(blockPos.x-5, blockPos.y-5, 10, 10);
        Tile[] tiles=map.getEntities(new AbstractTile[0]);
        for(Tile tile: tiles) {
            if(tile.getHitbox().intersects(rectangle)) {
                return Optional.of(tile);
            }
        }
        return Optional.empty();
    }

    /**
     * Calculates the top-left position of the tile block that contains the given screen coordinates.
     * <p>
     * This method maps the screen coordinates ({@code x}, {@code y}) to the corresponding tile-aligned
     * world position based on the current map offset and tile size.
     * </p>
     *
     * @param cords the coordinates in screen space
     * @param tileSize the size of a single tile
     * @return a {@link Point} representing the world-space position (top-left corner) of the tile block
     */
    static Point getBlockPosition(Map map, Point cords, Dimension tileSize) {
        Point worldPos = map.getPosition();

        // My logic simplified by ChatGPT
        int blockY = ((cords.y - worldPos.y) / tileSize.height) * tileSize.height + worldPos.y;
        int blockX = ((cords.x - worldPos.x) / tileSize.width) * tileSize.width + worldPos.x;

        return new Point(blockX, blockY);
    }
}
