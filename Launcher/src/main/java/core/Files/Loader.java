package core.Files;

import android.os.Bundle;

public class Loader {
    static public final int waiting = 01;
    static public final int running = 02;
    static public final int finished = 03;

    public Loader() {}
    public void start() {}
    public int Count() { return 0;}
    public Bundle header() {return null;}
    public int Status() { return waiting;}
}