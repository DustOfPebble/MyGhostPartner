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
        this.Center.x = ( A.x + B.x )/2;
        this.Center.y = ( A.y + B.y )/2;

        this.Size.w = abs( A.x - B.x);
        this.Size.h = abs( A.y - B.y);
    }

    public Node TopLeft() { return new Node(Center.x - Size.w/2, Center.y - Size.h/2); }
    public Node BottomLeft() { return new Node(Center.x - Size.w/2, Center.y + Size.h/2); }
    public Node TopRight() { return new Node(Center.x + Size.w/2, Center.y - Size.h/2); }
    public Node BottomRight() { return new Node(Center.x + Size.w/2, Center.y + Size.h/2); }

    public float Top() { return Center.y - Size.h/2; }
    public float Bottom() { return Center.y + Size.h/2; }
    public float Right() { return Center.x + Size.w/2; }
    public float Left() { return Center.x - Size.w/2; }

    public Node Center() {return Center;}
    public Extension Size() {return  Size;}

}
