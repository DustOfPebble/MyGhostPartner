package core.Structures;

import static java.lang.Math.sqrt;

/***********************************************************************
 *  Class containing in meters the displacement from Origin
 ***********************************************************************/

public class Coords2D {

    private static double power(double A) {return A*A;}
    static public double distance(Coords2D A, Coords2D B) { return sqrt(power(A.dx - B.dx) + power(A.dy - B.dy) ); }


    public float dx;
    public float dy;

    public Coords2D(Coords2D Set) { this.dx = Set.dx; this.dy = Set.dy;}
    public void set(Coords2D Set) { this.dx = Set.dx; this.dy = Set.dy;}

    public Coords2D(double x, double y) { this.dx =(float) x; this.dy = (float) y;}
    public void set(double x, double y) { this.dx = (float) x; this.dy = (float) y;}

    public Coords2D(float x, float y) { this.dx = x; this.dy = y;}
    public void set(float x, float y) { this.dx = x; this.dy = y;}


    public Coords2D copy() {return new Coords2D(this);}

}
