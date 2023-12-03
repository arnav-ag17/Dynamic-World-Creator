package core;

import edu.princeton.cs.algs4.StdDraw;
import tileengine.TERenderer;
import tileengine.TETile;
import tileengine.Tileset;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class Engine {
    private TERenderer ter = new TERenderer();
    public static final int WIDTH = 80;
    public static final int HEIGHT = 30;
    private TETile[][] gameState;
    private TETile[][] darkGameState;
    private Random r;
    private int xPoint;
    private int yPoint;
    private KeyboardInputSource inputSource;
    private boolean lights;

    private boolean savedMovements = false;

    private final double dX = 0.5;
    private final double bY = 0.6;
    private final double lY = 0.4;
    private final double qY = 0.3;

    private final int KEYCODE = 83;
    private final int PEN_SIZE = 30;

    private boolean game;
    private boolean menu;

    private String avatarName = "Avatar";



    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        game = false;
        menu = true;
        String seed = "";
        String movements = "";
        boolean needsupdate = true;
        inputSource = new KeyboardInputSource();
        while (true) {
            while (menu) {
                if (needsupdate) {
                    mainScreen();
                    StdDraw.show();
                    needsupdate = false;
                }
                char currentKey = getNextKey();
                if (currentKey == 'N') {
                    ArrayList<String> ls = nParse();
                    seed = ls.get(0);
                    movements = ls.get(1);
                    game = true;
                    menu = false;
                    initializeGame(seed);
                    ter.initialize(WIDTH, HEIGHT + 7, 0, 7);
                    ter.renderFrame(gameState);
                    updateHUD(seed, avatarName);
                    needsupdate = true;
                } else if (currentKey == 'L') {
                    String[] loadedData = loadGameState();
                    seed = loadedData[0];
                    movements = loadedData[1];
                    if (!seed.isEmpty()) {
                        game = true;
                        menu = false;
                        initializeGame(seed);
                        for (char move : movements.toCharArray()) {
                            movement(move);
                        }
                        needsupdate = true;
                        savedMovements = false;
                    }
                } else if (currentKey == 'Z') {
                    avatarName = enterAvatarName();
                    needsupdate = true;
                }
            }
            while (game) {
                if (needsupdate) {
                    gameState = interactWithInputString(seed + movements);
                    ter.renderFrame(gameState);
                    updateHUD(seed, avatarName);
                    StdDraw.show();
                    needsupdate = false;
                }
                if (StdDraw.hasNextKeyTyped()) {
                    char currentKey = getNextKey();
                    movements += currentKey;
                    movement(currentKey);
                    updateHUD(seed, avatarName);
                    if (currentKey == ':') {
                        char nextKey = getNextKey();  // Wait for the next key after ':'
                        if (Character.toUpperCase(nextKey) == 'Q') {
                            saveGameState(seed, movements);  // Save the game state
                            menu = true;          // Set menu to true to return to the main menu
                            game = false;         // Set game to false to exit the game loop
                            savedMovements = true;
                            needsupdate = true;
                            break;                // Break out of the loop
                        } else {
                            movements += nextKey; // If the next key isn't 'Q', continue the game
                        }
                    }
                }
                StdDraw.show();
            }
            StdDraw.pause(500);
            StdDraw.enableDoubleBuffering();
        }
    }

    private void initializeGame(String seed) {
        Long nSeed = Long.parseLong(seed);
        r = new Random(nSeed);
        setTiles();
        List<Room> rooms = new ArrayList<>();
        generateRooms(rooms);
        arrangeRooms(rooms);
        buildHallways(rooms);
        addWalls(gameState);

        xPoint = r.nextInt(WIDTH);
        yPoint = r.nextInt(HEIGHT);
        while (gameState[xPoint][yPoint] != Tileset.FLOOR) {
            xPoint = r.nextInt(WIDTH);
            yPoint = r.nextInt(HEIGHT);
        }
        createAvatar();
        ter.initialize(WIDTH, HEIGHT + 7, 0, 7);
        ter.renderFrame(gameState);
    }

    private void setTiles() {
        gameState = new TETile[WIDTH][HEIGHT];
        StdDraw.setCanvasSize(WIDTH * 16, (HEIGHT + 3) * 16); // Adjust the scale factor as needed
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                gameState[x][y] = Tileset.NOTHING;
            }
        }
    }


    private void updateHUD(String seed, String name) {
        StdDraw.setPenColor(Color.WHITE);
        Font smallFont = new Font("Monaco", Font.BOLD, 14);
        StdDraw.setFont(smallFont);
        StdDraw.textLeft(1, HEIGHT + 4, "Avatar: " + name);
        StdDraw.textLeft(1, HEIGHT + 5, "Seed: " + seed);
        int mouseX = (int) StdDraw.mouseX();
        int mouseY = ((int) StdDraw.mouseY()) - 7;
        if (mouseX >= 0 && mouseX < gameState.length && mouseY >= 0 && mouseY < gameState[0].length) {
            TETile mouseTile = gameState[mouseX][mouseY];
            StdDraw.textLeft(1, HEIGHT + 3, "Tile: " + mouseTile.description());
        } else {
            StdDraw.textLeft(1, HEIGHT + 3, "Tile: nothing");
        }
    }

    private ArrayList<String> nParse() {
        ArrayList<String> data = new ArrayList<>();
        String seed = "";
        String displaySeed = "";
        String movements = "";
        drawFrame("");
        while (!StdDraw.isKeyPressed(KEYCODE)) {
            if (StdDraw.hasNextKeyTyped()) {
                char current = StdDraw.nextKeyTyped();
                if (Character.isDigit(current)) {
                    seed += Character.toString(current);
                } else {
                    movements += Character.toUpperCase(current);
                    savedMovements = true;
                }
                displaySeed += Character.toString(current);
                drawFrame(displaySeed);
            }
        }
        data.add(seed);
        data.add(movements);
        return data;
    }

    private void saveGameState(String seed, String movements) {
        try {
            FileWriter writer = new FileWriter("gameState.txt");
            writer.write(seed + "\n");
            String allMoves = movements;
            String[] loadedData = loadGameState();
            if (savedMovements && loadedData[1] != null) {
                allMoves = loadedData[1] + movements;
            }
            writer.write(allMoves + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String[] loadGameState() {
        File file = new File("gameState.txt");
        if (!file.exists()) {
            return null;
        }
        String[] loadedData = new String[2];
        try {
            BufferedReader in = new BufferedReader(new FileReader(file));

            loadedData[0] = in.readLine();
            loadedData[1] = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return loadedData;
    }

    private void movement(char currentKey) {
        int xp = xPoint;
        int yp = yPoint;
        if (currentKey == 'W') {
            moveAvatar(0, 1);
        } else if (currentKey == 'A') {
            moveAvatar(-1, 0);
        } else if (currentKey == 'S') {
            moveAvatar(0, -1);
        } else if (currentKey == 'D') {
            moveAvatar(1, 0);
        } else if (currentKey == 'O') {
            if (!lights) {
                turnOffLights();
                lights = true;
            } else {
                lights = false;
            }
            moveAvatar(0, 0);
        }
        if (lights) {
            turnOffLights();
            ter.renderFrame(darkGameState);
        }
        else {ter.renderFrame(gameState);}
                if (lights) {
                    turnOffLights();
                    ter.renderFrame(darkGameState);
                } else {
                    ter.renderFrame(gameState);
                }
        if (xp != xPoint || yp != yPoint) {
            System.out.print(currentKey);
        }
    }

    private void turnOffLights() {
        darkGameState = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                darkGameState[x][y] = Tileset.NOTHING;
            }
        }
        for (int x = xPoint - 3; x < xPoint + 3; x++) {
            for (int y = yPoint - 3; y < yPoint + 3; y++) {
                if (0 <= y && y < HEIGHT && 0 <= x && x < WIDTH) {
                    darkGameState[x][y] = gameState[x][y];
                }
            }
        }
    }

    private void drawFrame(String s) {
        /* Take the input string S and display it at the center of the screen,
         * with the pen settings given below. */
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font fontBig = new Font("Ariel", Font.BOLD, PEN_SIZE);
        StdDraw.setFont(fontBig);
        StdDraw.text(dX, bY, "your current seed:");
        StdDraw.text(dX, dX, s);
        StdDraw.text(dX, lY, "click (s) when you're done");
        StdDraw.show();
    }

    private void mainScreen() {
        StdDraw.enableDoubleBuffering();
        StdDraw.clear();
        StdDraw.setCanvasSize(WIDTH * 16, HEIGHT * 16);
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font fontBig = new Font("Ariel", Font.BOLD, PEN_SIZE);
        StdDraw.setFont(fontBig);
        StdDraw.text(dX, bY, "Welcome to BYOW! Please choose an option below");
        StdDraw.text(dX, dX, "new game (n)");
        StdDraw.text(dX, lY, "load game (l)");
        StdDraw.text(dX, qY, "quit game (q)");
        StdDraw.text(dX, 0.2, "change avatar name (z)");
    }

    private void moveAvatar(int xChange, int yChange) {
        int xPos = xPoint;
        int yPos = yPoint;
        if (0 < (xPos + xChange) && (xPos + xChange) < WIDTH && 0 < (yPos + yChange) && (yPos + yChange) < HEIGHT) {
            if (gameState[xPos + xChange][yPos + yChange].equals(Tileset.FLOOR)) {
                gameState[xPos][yPos] = Tileset.FLOOR;
                xPoint = xPos + xChange;
                yPoint = yPos + yChange;
                gameState[xPoint][yPoint] = Tileset.AVATAR;
            }
        }
    }

    private char getNextKey() {
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                return inputSource.getNextKey();
            }
        }
    }


    private String enterAvatarName() {
        String newName = "";
        StdDraw.clear(Color.BLACK);
        StdDraw.text(dX, 0.5, "Enter Avatar Name (press ',' to confirm)");
        StdDraw.show();

        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char curr = StdDraw.nextKeyTyped();
                if (curr == ',') {
                    break;
                }
                newName += curr;
                StdDraw.clear(Color.BLACK);
                StdDraw.text(dX, 0.5, "Enter Avatar Name (press ',' to confirm)");
                StdDraw.text(dX, 0.45, newName);
                StdDraw.show();
                StdDraw.pause(100);
            }
        }
        return newName;
    }


    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww"). The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, running both of these:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        // passed in as an argument, and return a 2D tile representation of the
        // world that would have been drawn if the same inputs had been given
        // to interactWithKeyboard().
        //
        // See proj3.byow.InputDemo for a demo of how you can make a nice clean interface
        // that works for different input types.

        String seed = "";
        String movements = "";
        char[] array = input.toCharArray();

        for (int i = 0; i < array.length; i++) {
            char c = array[i];
            if (Character.isDigit(c)) {
                System.out.print(c);
                seed += Character.toString(c);
            } else if (Character.toUpperCase(c) == 'L') {
                System.out.println(c);
                String[] loadedData = loadGameState();
                if (loadedData == null || loadedData[0].isEmpty()) {
                    seed = "0";
                } else {
                    seed = loadedData[0];
                    movements = loadedData[1];
                }
                savedMovements = false;
            } else if (c == ':' && i + 1 < array.length) {
                System.out.println(c);
                char nextKey = array[i + 1];  // Wait for the next key after ':'
                if (Character.toUpperCase(nextKey) == 'Q') {
                    System.out.println(nextKey);
                    saveGameState(seed, movements);  // Save the game state
                    savedMovements = true;
                }
            } else {
                if (Character.toUpperCase(c) != 'Q') {
                    movements += Character.toUpperCase(c);
                    savedMovements = true;
                }
            }
        }

        if (gameState == null && !seed.equals("")) {
            initializeGame(seed);
        }
        for (char move : movements.toCharArray()) {
            movement(move);
        }

        return gameState;

    }

    private void arrangeRooms(List<Room> rooms) {
        boolean[] sorted = new boolean[rooms.size()];
        for (int i = 0; i < rooms.size(); i++) {
            if (!sorted[i]) {
                List<Room> cluster = clusterRooms(i, rooms, sorted);
                updateRoomList(cluster, rooms, sorted);
            }
        }
    }

    private List<Room> clusterRooms(int index, List<Room> rooms, boolean[] sorted) {
        List<Room> cluster = new ArrayList<>();
        Room currentRoom = rooms.get(index);
        cluster.add(currentRoom);
        sorted[index] = true;

        while (cluster.size() < rooms.size()) {
            Room closestRoom = null;
            double closestDistance = Double.POSITIVE_INFINITY;
            for (int j = 0; j < rooms.size(); j++) {
                if (!sorted[j]) {
                    Room candidate = rooms.get(j);
                    double distance = calculateDistance(currentRoom, candidate);
                    if (distance < closestDistance) {
                        closestRoom = candidate;
                        closestDistance = distance;
                    }
                }
            }
            if (closestRoom != null) {
                currentRoom = closestRoom;
                cluster.add(currentRoom);
                sorted[rooms.indexOf(currentRoom)] = true;
            }
        }
        return cluster;
    }

    private void updateRoomList(List<Room> cluster, List<Room> rooms, boolean[] sorted) {
        int startIndex = rooms.indexOf(cluster.get(0));
        for (int j = 0; j < cluster.size(); j++) {
            rooms.set(startIndex + j, cluster.get(j));
        }
    }

    private double calculateDistance(Room a, Room b) {
        double dx = a.getCoord()[0] - b.getCoord()[0];
        double dy = a.getCoord()[1] - b.getCoord()[1];
        return Math.sqrt(dx * dx + dy * dy);
    }

    private void generateRooms(List<Room> rooms) {
        int numRooms = r.nextInt(5, 15);
        for (int room = 0; room < numRooms; room++) {
            int width = r.nextInt(3, 9);
            int height = r.nextInt(3, 9);
            int xCord = Math.max(r.nextInt(WIDTH - width), 1);
            int yCord = Math.max(r.nextInt(HEIGHT - height), 1);
            Room rm = new Room(xCord, yCord, height, width, gameState);
            rooms.add(rm);
        }
    }

    private void addWalls(TETile[][] world) {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                for (int xoff = -1; xoff <= 1; xoff++) {
                    for (int yoff = -1; yoff <= 1; yoff++) {
                        if (world[x][y] == Tileset.FLOOR) {
                            if (world[x + xoff][y + yoff] != Tileset.FLOOR) {
                                world[x + xoff][y + yoff] = Tileset.WALL;
                            }
                        }
                    }
                }
            }
        }
    }

    private void buildHallways(List<Room> rooms) {
        // can change this method to create more random hallways between rooms
        // must use a disjoint set to moveAvatar for completion (if size of djs is 1)
        int xPoint1 = rooms.get(0).getCoord()[0];
        int yPoint1 = rooms.get(0).getCoord()[1];
        for (int index = 1; index < rooms.size(); index++) {
            int xPoint2 = rooms.get(index).getCoord()[0];
            int yPoint2 = rooms.get(index).getCoord()[1];
            buildHallway(xPoint1, yPoint1, xPoint2, yPoint2);
            xPoint1 = rooms.get(index).getCoord()[0];
            yPoint1 = rooms.get(index).getCoord()[1];
        }
    }

    private void buildHallway(int xP1, int yP1, int xP2, int yP2) {
        buildHorizontal(xP1, yP1, xP2 - xP1, yP2);

    }

    private void buildHorizontal(int xP1, int yP1, int xOffset, int yP2) {
        int hallwayWidth = r.nextInt(1, 2);
        for (int x = xP1; x < (xP1 + xOffset); x++) {
            for (int y = yP1; y < (yP1 + hallwayWidth); y++) {
                gameState[x][y] = Tileset.FLOOR;
            }
        }
        for (int x = xP1; x > (xP1 + xOffset); x--) {
            for (int y = yP1; y < (yP1 + hallwayWidth); y++) {
                gameState[x][y] = Tileset.FLOOR;
            }
        }
        buildVertical(xP1 + xOffset, yP1, yP2 - yP1);
    }

    private void buildVertical(int xPoint1, int yPoint1, int yOffset) {
        int hallwayWidth = r.nextInt(1, 2);
        for (int y = yPoint1; y < (yPoint1 + yOffset); y++) {
            for (int x = xPoint1; x < (xPoint1 + hallwayWidth); x++) {
                gameState[x][y] = Tileset.FLOOR;
            }
        }
        for (int y = yPoint1; y > (yPoint1 + yOffset); y--) {
            for (int x = xPoint1; x < (xPoint1 + hallwayWidth); x++) {
                gameState[x][y] = Tileset.FLOOR;
            }
        }
    }

    private void createAvatar() {
        gameState[xPoint][yPoint] = Tileset.AVATAR;
    }
}
