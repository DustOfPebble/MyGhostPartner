package core.Files;

import android.util.Log;
import org.json.JSONObject;

import core.Structures.Sample;

public class LibJSON {
    static private final String LogTag = LibJSON.class.getSimpleName();

    static private final String LongitudeID = "LON";
    static private final String LatitudeID = "LAT";
    static private final String SpeedID = ">>";
    static private final String AccuracyID = "<*>";
    static private final String AltitudeID = "^";
    static private final String BearingID = "+";
    static private final String HeartbeatID = "HB";

    static private final String YearID="Year";
    static private final String MonthID="Month";
    static private final String DayID="Day";
    static private final String NameID="Name";

    static public Sample fromJSON(String StringJSON){
        Sample Snapshot = new Sample();
        JSONObject JSON;
        try { JSON = new JSONObject(StringJSON); }
        catch (Exception JSONBuilder)
        {
            Log.d(LogTag, "fromString[JSON] => Failed to parse ["+StringJSON+"]");
            return Snapshot;
        }

        try {
            Snapshot.Longitude = JSON.getDouble(LongitudeID);
            Snapshot.Latitude = JSON.getDouble(LatitudeID);
            Snapshot.Altitude = JSON.getDouble(AltitudeID);
            Snapshot.Speed = (float)JSON.getDouble(SpeedID);
            Snapshot.Bearing = (float)JSON.getDouble(BearingID);
            Snapshot.Accuracy = (float)JSON.getDouble(AccuracyID);
        }
        catch (Exception Missing)
        {
            Log.d(LogTag, "Sample from JSON => Missing required value ["+StringJSON+"]");
            return Snapshot;
        }

        try { Snapshot.Heartbeat = (byte) JSON.getInt(HeartbeatID);}
        catch (Exception Missing) { Snapshot.Heartbeat = (short) -1; }

        return Snapshot;
    }

    static public  String toJSON(Sample Snapshot){
        JSONObject JSON =  new JSONObject();
        try {
            JSON.put(LongitudeID, Math.floor(Snapshot.Longitude * 1e7) / 1e7);// Cut at 7 digit to save space
            JSON.put(LatitudeID, Math.floor(Snapshot.Latitude * 1e7) / 1e7);// Cut at 7 digit to save space
            JSON.put(SpeedID, Math.floor(Snapshot.Speed * 10) / 10); // Cut at 1 digit to save space
            JSON.put(AccuracyID, Math.floor(Snapshot.Accuracy * 10) / 10); // Cut at 1 digit to save space
            JSON.put(BearingID, Math.floor(Snapshot.Bearing * 10) / 10); // Cut at 1 digit to save space
            JSON.put(AltitudeID, Math.floor(Snapshot.Altitude * 10) / 10); // Cut at 1 digit to save space
            if (Snapshot.Heartbeat != -1) JSON.put(HeartbeatID, Snapshot.Heartbeat);
        } catch (Exception JSONBuilder)
        { Log.d(LogTag, "Sample to JSON => Error in JSON construction.");}

        return JSON.toString();
     }

    static public String DescriptorToJSON(Descriptor Details) {
        JSONObject JSON = new JSONObject();
        try {
            JSON.put(YearID, Details.Year);
            JSON.put(MonthID, Details.Month);// Month is from 0 to 11
            JSON.put(DayID, Details.Day);
            JSON.put(NameID, Details.Name);
        }
        catch (Exception JSONBuilder)
        {Log.d(LogTag, "Infos to JSON => Error in JSON construction.");}
        return JSON.toString();
    }

    static Descriptor DescriptorFromJSON(String StringJSON) {
        if (StringJSON == null) return null;
        Descriptor Details = new Descriptor();
        JSONObject JSON;
        try { JSON = new JSONObject(StringJSON); }
        catch (Exception JSONBuilder)
        { Log.d(LogTag, "Infos from JSon => Failed to parse"); return null;}
        try {
            Details.Year = JSON.getInt(YearID);
            Details.Month = JSON.getInt(MonthID);
            Details.Day = JSON.getInt(DayID);
            Details.Name = JSON.getString(NameID);
        }
        catch (Exception Missing) { Log.d(LogTag, "Infos from JSon => Missing fields"); return null;}

        return Details;
    }


}
