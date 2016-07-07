package com.dustcloud.dailyrace;

import android.util.Log;

import org.json.JSONObject;

public class Converter {

    private final String LongitudeID = "Long";
    private final String LatitudeID = "Lat";
    private final String SpeedID = "Spd";
    private final String AccuracyID = "Acc";
    private final String AltitudeID = "Alt";
    private final String BearingID = "Bear";
    private final String HeartbeatID = "Heart";

    public Converter() { }

    public String toJSON(SurveyLoader Survey) {
        JSONObject SurveyJSON = new JSONObject();
        try {
            SurveyJSON.put(LongitudeID, Math.floor(Survey.getLongitude() * 1e7) / 1e7);// Cut at 7 digit to save space
            SurveyJSON.put(LatitudeID, Math.floor(Survey.getLatitude() * 1e7) / 1e7);// Cut at 7 digit to save space
            SurveyJSON.put(AccuracyID, Math.floor(Survey.getAccuracy() * 10) / 10); // Cut at 1 digit to save space
            SurveyJSON.put(AltitudeID, Math.floor(Survey.getAltitude() * 10) / 10); // Cut at 1 digit to save space
            SurveyJSON.put(SpeedID, Math.floor(Survey.getSpeed() * 10) / 10); // Cut at 1 digit to save space
            SurveyJSON.put(BearingID, Math.floor(Survey.getBearing() * 10) / 10); // Cut at 1 digit to save space
            if (Survey.getHeartbeat() != -1) SurveyJSON.put(HeartbeatID, Survey.getHeartbeat());
        } catch (Exception JSONBuilder) { Log.d("SurveyLoader", "ToJSON => Error in JSON construction.");}
        return SurveyJSON.toString();
    }

    public SurveyLoader fromJSON(String StringJSON) {
        if (StringJSON == null) return null;
        JSONObject SurveyJSON;
        SurveyLoader Survey = new SurveyLoader();
        try { SurveyJSON = new JSONObject(StringJSON); }
        catch (Exception JSONBuilder)
            { Log.d("SurveyLoader", "SurveyLoader from JSon => Failed to parse ["+StringJSON+"]"); return null;}
        try {
            Survey.setLongitude(SurveyJSON.getDouble(LongitudeID));
            Survey.setLatitude(SurveyJSON.getDouble(LatitudeID));
            Survey.setAltitude( (float)SurveyJSON.getDouble(AltitudeID));
            Survey.setSpeed( (float)SurveyJSON.getDouble(SpeedID));
            Survey.setBearing( (float)SurveyJSON.getDouble(BearingID));
            Survey.setAccuracy( (float)SurveyJSON.getDouble(AccuracyID));
        }
        catch (Exception Missing)
            { Log.d("SurveyLoader", "SurveyLoader from JSon => Missing required value"); return null;}

        try { Survey.setHeartbeat( (short) SurveyJSON.getInt(HeartbeatID));}
        catch (Exception Missing) { Survey.setHeartbeat((short) -1);}
//        Log.d("SurveyLoader","reloaded Heartbeat="+Heartbeat);
        return Survey;
    }

}
