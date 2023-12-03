package core;


import tileengine.TETile;
import tileengine.Tileset;

public class Room {
    private int[] coord = new int[2];
    private int height;
    private int width;

    public Room(int x, int y, int height, int width, TETile[][] gameState) {
        coord[0] = x;
        coord[1] = y;
        this.height = height;
        this.width = width;
        createRoom(gameState);
    }

    private void createRoom(TETile[][] gameState) {
        for (int x = coord[0]; x < (coord[0] + width); x++) {
            for (int y = coord[1]; y < (coord[1] + height); y++) {
                if ((x < gameState.length - 1) && (y < gameState[0].length - 1)) {
                    gameState[x][y] = Tileset.FLOOR;
                }
            }
        }
    }

    public int[] getCoord() {
        return coord;
    }
}

