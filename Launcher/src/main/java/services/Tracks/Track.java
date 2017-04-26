package services.Tracks;


import java.util.ArrayList;

public class Track {

    public Track{}
    int Index = -1;
     ArrayList<Segment> Segments;

    void appendSegment(Node A, Node B)
    {
        Segment Link = new Segment();
        Link.Start = A;
        Link.End = B;
        Segments.add(Link);
    }

    void appendSegments(QList<Segment*> Segments)
    {
        foreach (Segment *segment, Segments)
            this->appendSegment(segment->Start,segment->End);
    }

    int size()
    {
        return Segments.size();
    }

    QPolygonF getGeometry()
    {
        if (Segments.isEmpty()) return QPolygonF();
        return getGeometry(0, Segments.size()-1);
    }
    QPolygonF getGeometry(int First, int Last)
    {
        QPolygonF Polygon;
        if (First < 0) First = 0;
        if (First >= Segments.size()) First = Segments.size() - 1;
        if (Last >= Segments.size()) Last = Segments.size() - 1;
        if (Last < First) return Polygon;

        for(int i=First ; i<=Last; i++) Polygon.append(Segments.at(i)->Start);
        Polygon.append(Segments.at(Last)->End); // Adding final point ...
        return Polygon;
    }
    // ########################################################################################
    void search(QPointF position, double range)
    {
        if (this->isRunning()) return;
        this->Position = position;
        this->Range = range;
        this->start(QThread::LowPriority); // Do not consume heavy CPU
    }
    // ########################################################################################
    QPointF getSearched()
    {
        return this->Position;
    }
    // ########################################################################################
    int getSegment()
    {
        return this->Index;
    }
    // ########################################################################################
    double distanceFromStart()
    {
        if (Index == -1) return 0.0;
        double length = 0.0;
        for(int i=0; i< Index; i++) length += Segments.at(i)->length();
        length+= Segments.at(Index)->fromStart(Position);
        return length;
    }
    // ########################################################################################
    double distanceToEnd()
    {
        if (Index == -1) return this->startToEnd();
        double length = 0.0;
        length+= Segments.at(Index)->fromEnd(Position);
        for(int i=Index+1; i< Segments.size(); i++) length += Segments.at(i)->length();
        return length;
    }
    // ########################################################################################
    double startToEnd()
    {
        double Length = 0.0;
        foreach(Segment* segment, Segments) Length += segment->length();
        return Length;
    }
    // ########################################################################################
// ########################################################################################
    void run()
    {
        // First, try from last search success
        if (this->checkSegment(Index)) { emit this->searchSucceed(); return;}

        // Failed then try some further segments from last result
        if (this->checkSegment(Index+1)) { emit this->searchSucceed(); return; }
        if (this->checkSegment(Index+2)) { emit this->searchSucceed(); return; }
        if (this->checkSegment(Index+3)) { emit this->searchSucceed(); return; }

        // Failed then try previous segment
        if (this->checkSegment(Index-1)) { emit this->searchSucceed(); return; }

        // At least, we do a full search
        bool found = false;
        int i = 0;
        while ((!found) && (i < Segments.size()))
        {
            found = Segments.at(i)->isCatched(Position, Range);
            if (found) Index = i;
            i++;
        }
        if (found) { emit this->searchSucceed(); return; }

        // Finally we failed to find ...
        Index = -1;
        emit this->searchFailed();
    }
    // ########################################################################################
    bool checkSegment(int i)
    {
        if (i < 0) return false;
        if (i >= Segments.size()) return false;
        if (Segments.at(i)->isCatched(Position, Range)) { Index = i; return true; }
        return false;
    }


}
