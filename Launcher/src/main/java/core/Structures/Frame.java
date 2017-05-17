package core.Structures;

import static java.lang.Math.abs;

public class Frame {

    private Coords2D Center;
    private Extension Size;

    public Frame(Coords2D Center, Extension Size ) {
        this.Center = Center;
        this.Size = Size;
    }

    public Frame (Coords2D A, Coords2D B) {
        Center = new Coords2D( (A.dx + B.dx)/2, (A.dy + B.dy)/2 );
        Size = new Extension( abs(A.dx - B.dx), abs(A.dy - B.dy));
    }

    public Coords2D TopLeft() { return new Coords2D(Center.dx - Size.w/2, Center.dy - Size.h/2); }
    public Coords2D BottomLeft() { return new Coords2D(Center.dx - Size.w/2, Center.dy + Size.h/2); }
    public Coords2D TopRight() { return new Coords2D(Center.dx + Size.w/2, Center.dy - Size.h/2); }
    public Coords2D BottomRight() { return new Coords2D(Center.dx + Size.w/2, Center.dy + Size.h/2); }

    public float Top() { return Center.dy - Size.h/2; }
    public float Bottom() { return Center.dy + Size.h/2; }
    public float Right() { return Center.dx + Size.w/2; }
    public float Left() { return Center.dx - Size.w/2; }

    public Coords2D Center() {return Center;}
    public Extension Size() {return  Size;}

}
