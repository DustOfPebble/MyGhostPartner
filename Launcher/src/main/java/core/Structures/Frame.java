package core.Structures;

import static java.lang.Math.abs;

public class Frame {

    private Node Center;
    private Extension Size;

    public Frame(Node Center, Extension Size ) {
        this.Center = Center;
        this.Size = Size;
    }

    public Frame (Node A, Node B) {
        this.Center.dx = ( A.dx + B.dx)/2;
        this.Center.dy = ( A.dy + B.dy)/2;

        this.Size.w = abs( A.dx - B.dx);
        this.Size.h = abs( A.dy - B.dy);
    }

    public Node TopLeft() { return new Node(Center.dx - Size.w/2, Center.dy - Size.h/2); }
    public Node BottomLeft() { return new Node(Center.dx - Size.w/2, Center.dy + Size.h/2); }
    public Node TopRight() { return new Node(Center.dx + Size.w/2, Center.dy - Size.h/2); }
    public Node BottomRight() { return new Node(Center.dx + Size.w/2, Center.dy + Size.h/2); }

    public float Top() { return Center.dy - Size.h/2; }
    public float Bottom() { return Center.dy + Size.h/2; }
    public float Right() { return Center.dx + Size.w/2; }
    public float Left() { return Center.dx - Size.w/2; }

    public Node Center() {return Center;}
    public Extension Size() {return  Size;}

}
