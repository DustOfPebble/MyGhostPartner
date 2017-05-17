package services;

import core.GPS.CoreGPS;

public interface Signals {

    void TrackLoaded(boolean Success);
    void TrackEvent(int Distance);

    void UpdateBPM(int Value);

    void UpdateGPS(CoreGPS Provider);

    void OutOfRange();
}