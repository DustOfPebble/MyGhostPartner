package com.dustcloud.dailyrace;

import android.util.Log;

import org.json.JSONObject;

import java.util.Calendar;

public class TimeStamps {
    private int Year;
    private int Month;
    private int Day;

    private final String YearID="Year";
    private final String MonthID="Month";
    private final String DayID="Day";

    private long MS_PER_DAY = 24 * 60 * 60 * 1000;

    private Calendar Now;

    public TimeStamps() {
        Now = Calendar.getInstance();
    }

    public String getNowToJSON() {
        JSONObject TimeJSON = new JSONObject();
        try {
            TimeJSON.put(YearID, Now.get(Calendar.YEAR));
            TimeJSON.put(MonthID, Now.get(Calendar.MONTH)+1);// Month is from 0 to 11
            TimeJSON.put(DayID, Now.get(Calendar.DAY_OF_MONTH));
        } catch (Exception JSONBuilder) {}
        Log.d("TimeStamps", " Now JSon from TimeStamps =" + TimeJSON.toString());
        return TimeJSON.toString();
    }

    public int getDaysAgoFromJSON(String TimeString) {
        if (TimeString == null) return -1;
        JSONObject TimeJSON;
        try { TimeJSON = new JSONObject(TimeString); } catch (Exception JSONBuilder)
        { Log.d("TimeStamps", "From JSon => Failed to parse"); return -1;}
        try {
            Year = TimeJSON.getInt(YearID);
            Month = TimeJSON.getInt(MonthID);
            Day = TimeJSON.getInt(DayID);
        }
        catch (Exception Missing) { Log.d("TimeStamps", "From JSon =>  Missing fields"); return -1;}

        Calendar Before = Calendar.getInstance();
        Before.set(Calendar.DAY_OF_MONTH,Day);
        Before.set(Calendar.MONTH,Month-1); // Month is from 0 to 11
        Before.set(Calendar.YEAR, Year);

        return (int) ((Now.getTimeInMillis() - Before.getTimeInMillis())/MS_PER_DAY);
    }

}
