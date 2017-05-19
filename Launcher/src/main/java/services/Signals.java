package services;

import core.GPS.CoreGPS;

public interface Signals {

    void TrackLoaded(boolean Success);
    void TrackEvent(int Distance);

    void UpdatedSensor(int Value);

    void UpdatedGPS(CoreGPS Provider);

    void OutOfRange();
}