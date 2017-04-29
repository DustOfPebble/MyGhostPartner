package services.Track;

public interface TrackEvents {
    void LoadedGPX(boolean Status);
    void Tracking(int TrackingEvent);
}
