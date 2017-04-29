package core.Structures;

public class Node {
    public float x;
    public float y;

    public Node(double x, double y) { this.x =(float) x; this.y = (float) y;}
    public void set(double x, double y) { this.x = (float) x; this.y = (float) y;}
    public Node(float x, float y) { this.x = x; this.y = y;}
    public void set(float x, float y) { this.x = x; this.y = y;}
}

