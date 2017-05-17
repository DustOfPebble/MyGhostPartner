package services.Track;

import core.Structures.Coords2D;
import static core.Structures.Coords2D.distance;


public class Segment {

    private Coords2D Start, End;

    public Segment(Coords2D A, Coords2D B){
        Start = A;
        End = B;
    }
    public double length() { return distance(Start, End);}

    public boolean intercept(Coords2D Coordinate, double Clearance)
    {
        Coords2D P,A,B,C;
        P = Coordinate;
        A = Start;
        B = End;
        C = projected(P,A,B); // C is orthogonal projection on AB line ...

        if (distance(P,C) > Clearance) return false; // P is out of line vicinity
        double AB = distance(A,B);
        if (( distance(C,A) > AB) && distance(C,B) > AB) return false; // C is outside the segment AB
        return true;
    }

    public double fromStart(Coords2D Coordinate)
    {
        Coords2D P,A,B,C;
        P = Coordinate;
        A = Start;
        B = End;
        C = projected(P,A,B); // C is orthogonal projection on AB line ...

        double AB = distance(A,B);
        double CA = distance(C,A);
        if (distance(C,B) > AB) return (-1.0 * CA); // C is outside the segment AB
        return CA;
    }

    public double toEnd(Coords2D Coordinate)
    {
        Coords2D P,A,B,C;
        P = Coordinate;
        A = Start;
        B = End;
        C = projected(P,A,B); // C is orthogonal projection on AB line ...

        double AB = distance(A,B);
        double CB = distance(C,B);
        if (distance(C,A) > AB) return (-1.0 * CB); // C is outside the segment AB
        return CB;
    }

    private Coords2D projected(Coords2D P, Coords2D A, Coords2D B) {
        Coords2D C = A; // C is the projected Point

        // Case A and B is identical ...
        if ( A == B) return C;

        // Case AB is vertical
        if (( B.dx - A.dx) == 0) return C;

        // Case AB is horizontal
        if ((B.dy - A.dy) == 0) {
            C.dx = P.dx;
            C.dy = A.dy;
            return C;
        }

        // General Case
        float Mab,Mpc, Kab, Kpc; // dy = M dx + K
        Mab = (B.dy - A.dy)/(B.dx - A.dx);
        Kab = A.dy - (Mab * A.dx);
        Mpc = -1 / Mab;
        Kpc = P.dy - (Mpc * P.dx);
        C.dx = ((Kpc - Kab)/(Mab -Mpc));
        C.dy = ((Mpc * P.dx) + Kpc);
        return C;
    }
}
