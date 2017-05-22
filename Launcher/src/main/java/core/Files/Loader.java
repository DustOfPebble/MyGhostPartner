package core.Files;

public class Loader {
    static public final int waiting = 01;
    static public final int running = 02;
    static public final int finished = 03;

    public Loader() {}
    public void start() {}
    public int Status() { return waiting;}
}