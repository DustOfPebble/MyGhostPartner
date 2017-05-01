package core.Structures;

public class Node {
    public float dx;
    public float dy;

    public Node(double x, double y) { this.dx =(float) x; this.dy = (float) y;}
    public void set(double x, double y) { this.dx = (float) x; this.dy = (float) y;}
    public Node(float x, float y) { this.dx = x; this.dy = y;}
    public void set(float x, float y) { this.dx = x; this.dy = y;}
}

