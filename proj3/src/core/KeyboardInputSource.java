package core;

import edu.princeton.cs.algs4.StdDraw;

public class KeyboardInputSource implements InputSource {

    public char getNextKey() {
        return Character.toUpperCase(StdDraw.nextKeyTyped());
    }

    @Override
    public boolean possibleNextInput() {
        return StdDraw.hasNextKeyTyped();
    }
}
