package services.Track;

import static java.lang.Math.cos;
import static java.lang.Math.sqrt;

public class Segment {

    Node Start, End;

    public Segment(Node A, Node B){
        Start = A;
        End = B;
    }
    public double length() { return distance(Start, End);}

    double distance(Node A, Node B) {
        return sqrt(power(meters(A.x - B.x) * correction(A,B)) + power(meters(A.y - B.y) ) );
    }

    public boolean intercept(Node Coordinate, double Clearance)
    {
        Node P,A,B,C;
        P = Coordinate;
        A = Start;
        B = End;
        C = projected(P,A,B); // C is orthogonal projection on AB line ...

        if (distance(P,C) > Clearance) return false; // P is out of line vicinity
        double AB = distance(A,B);
        if (( distance(C,A) > AB) && distance(C,B) > AB) return false; // C is outside the segment AB
        return true;
    }

    public double fromStart(Node Coordinate)
    {
        Node P,A,B,C;
        P = Coordinate;
        A = Start;
        B = End;
        C = projected(P,A,B); // C is orthogonal projection on AB line ...

        double AB = distance(A,B);
        double CA = distance(C,A);
        if (distance(C,B) > AB) return (-1.0 * CA); // C is outside the segment AB
        return CA;
    }

    public double toEnd(Node Coordinate)
    {
        Node P,A,B,C;
        P = Coordinate;
        A = Start;
        B = End;
        C = projected(P,A,B); // C is orthogonal projection on AB line ...

        double AB = distance(A,B);
        double CB = distance(C,B);
        if (distance(C,A) > AB) return (-1.0 * CB); // C is outside the segment AB
        return CB;
    }

    private Node projected(Node P, Node A, Node B) {
        Node C = A; // C is the projected Point

        // Case A and B is identical ...
        if ( A == B) return C;

        // Case AB is vertical
        if (( B.x - A.x ) == 0) return C;

        // Case AB is horizontal
        if ((B.y - A.y ) == 0) {
            C.x = P.x;
            C.y = A.y;
            return C;
        }

        // General Case
        float Mab,Mpc, Kab, Kpc; // y = M x + K
        Mab = (B.y - A.y)/(B.x - A.x);
        Kab = A.y - (Mab * A.x);
        Mpc = -1 / Mab;
        Kpc = P.y - (Mpc * P.x);
        C.x = ((Kpc - Kab)/(Mab -Mpc));
        C.y = ((Mpc * P.x) + Kpc);
        return C;
    }

    /************************************************************************
     * Helpers functions...
     ************************************************************************/
    private double power(double A) {return A*A;}
    private double radians(double degres) { return (degres * (3.141592 / 180.0)); }
    private double meters(double degres) { return (degres * 111132.0); } // 1° Latitude => 111.132 km at Lat:45°N
    private double correction(Node A, Node B)  { return (cos( radians( 0.5*( A.y + B.y ) )));}
}
