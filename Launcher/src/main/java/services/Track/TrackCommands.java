package services.Track;

public interface TrackCommands {
    void LoadGPX(String Filename);
    void EnableGPS(boolean Enabled);
    void EnableTracking(boolean Enabled);
    void SetClearance(double Clearance);
}
