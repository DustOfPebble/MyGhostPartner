package core.Files;

import android.util.Log;
import org.json.JSONObject;

import java.util.Calendar;

import core.Structures.Sample;

public class LibJSON {
    static final String LogTag = LibJSON.class.getSimpleName();

    static final String LongitudeID = "LON";
    static final String LatitudeID = "LAT";
    static final String SpeedID = ">>";
    static final String AccuracyID = "<*>";
    static final String AltitudeID = "^";
    static final String BearingID = "+";
    static final String HeartbeatID = "HB";

    static final String YearID="Year";
    static final String MonthID="Month";
    static final String DayID="Day";

    static public Sample fromStringJSON(String StringJSON){
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
            Log.d(LogTag, "Sample from JSON => Missing required value");
            return Snapshot;
        }

        try { Snapshot.Heartbeat = (byte) JSON.getInt(HeartbeatID);}
        catch (Exception Missing) { Snapshot.Heartbeat = (short) -1; }

        return Snapshot;
    }

    static public  String toStringJSON(Sample Snapshot){
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

    static public String DateToJSON(Calendar Date) {
        JSONObject JSON = new JSONObject();
        try {
            JSON.put(YearID, Date.get(Calendar.YEAR));
            JSON.put(MonthID, Date.get(Calendar.MONTH)+1);// Month is from 0 to 11
            JSON.put(DayID, Date.get(Calendar.DAY_OF_MONTH));
        }
        catch (Exception JSONBuilder)
        {Log.d(LogTag, "Date to JSON => Error in JSON construction.");}
        return JSON.toString();
    }

    static public Calendar DateFromJSON(String StringJSON) {
        if (StringJSON == null) return null;
        JSONObject JSON;
        int Year, Month, Day;

        try { JSON = new JSONObject(StringJSON); }
        catch (Exception JSONBuilder)
        { Log.d(LogTag, "Date from JSon => Failed to parse"); return null;}
        try {
            Year = JSON.getInt(YearID);
            Month = JSON.getInt(MonthID);
            Day = JSON.getInt(DayID);
        }
        catch (Exception Missing) { Log.d(LogTag, "Date from JSon => Missing fields"); return null;}

        Calendar Date = Calendar.getInstance();
        Date.set(Calendar.DAY_OF_MONTH,Day);
        Date.set(Calendar.MONTH,Month-1); // Month is from 0 to 11
        Date.set(Calendar.YEAR, Year);

        return Date;
    }


}
