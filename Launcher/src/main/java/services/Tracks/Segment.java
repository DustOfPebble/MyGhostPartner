package services.Tracks;

import static java.lang.Math.cos;
import static java.lang.Math.toRadians;

public class Segment {

    Node Start, End;

    double radians(double degres) { return (degres * (3.141592 / 180.0)); }
    double meters(double degres) { return (degres * 111132.0); } // 1° Lattitude => 111.132 km at Lat:45°N

    boolean isCatched(Node Coordinate, double Threshold)
    {
        Node P,A,B,C;
        P = Coordinate;
        A = Start;
        B = End;
        C = projected(P,A,B); // C is othogonal projection on AB line ...

        if (distance(P,C) > Threshold) return false; // P is out of line vicinity
        double AB = distance(A,B);
        if (( distance(C,A) > AB) && distance(C,B) > AB) return false; // C is ouside the segment AB
        return true;
    }
    // ########################################################################################
    double fromStart(Node Coordinate)
    {
        Node P,A,B,C;
        P = Coordinate;
        A = Start;
        B = End;
        C = projected(P,A,B); // C is orthogonal projection on AB line ...

        double AB = distance(A,B);
        double CA = distance(C,A);
        if (distance(C,B) > AB) return (-1.0 * CA); // C is ouside the segment AB
        return CA;
    }
    // ########################################################################################
    double fromEnd(Node Coordinate)
    {
        Node P,A,B,C;
        P = Coordinate;
        A = Start;
        B = End;
        C = projected(P,A,B); // C is orthogonal projection on AB line ...

        double AB = distance(A,B);
        double CB = distance(C,B);
        if (distance(C,A) > AB) return (-1.0 * CB); // C is ouside the segment AB
        return CB;
    }
    // ########################################################################################
    double length()
    {
        Node A,B;
        A = Start;
        B = End;
        return ( distance(A, B) );
    }
    // ########################################################################################
// ########################################################################################
    double distance(Node A, Node B)
    { return (  square( \
            qPow( meters(A.x - B.x) * correction(A,B), 2) \
            +       qPow( meters(A.y - B.y)                  , 2) \
              , 0.5) \
          );
    }
    // ########################################################################################
    Node projected(Node P, Node A, Node B)
    {
        Node C = A; // C is the projected Point

        // Case A and B is identical ...
        if ( A == B) return C;

        // Case AB is vertical
        if (( B.x - A.x ) == 0) return C;

        // Case AB is horizontal
        if ((B.y - A.y ) == 0)
        {
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
    // ########################################################################################
    double correction(Node A, Node B)
    { return (cos( toRadians( 0.5*( A.y + B.y ) )));}
// ########################################################################################

}
