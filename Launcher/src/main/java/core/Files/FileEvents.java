package core.Files;


import core.Structures.Statistic;

public interface FileEvents {
    void DaysElapsed(int Nb);
    void Loaded(Statistic Snapshot);
    void Ended(boolean Success);
}
