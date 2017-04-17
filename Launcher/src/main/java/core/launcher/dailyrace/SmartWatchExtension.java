package core.launcher.heartspy;

import android.content.Context;
import android.os.Bundle;

import lib.smartwatch.SmartwatchBundle;
import lib.smartwatch.SmartwatchEvents;
import lib.smartwatch.SmartwatchManager;

/******************************************************************************
 *  This belongs to main app because it have to customized
 *      - Key values
 *      - Values types
 *  NB: the business logic remains the same whatever datas sent
 ******************************************************************************/
public class SmartWatchExtension implements SmartwatchEvents {

    private SmartwatchManager WatchConnector = null;
    private SmartwatchBundle DataSet = null;
    private Boolean isWatchConnected = false;


    public SmartWatchExtension(Context context) {
        WatchConnector = new SmartwatchManager(context,this, SmartwatchConstants.WatchUUID);
        isWatchConnected = WatchConnector.isConnected();
        DataSet = new SmartwatchBundle();
    }

    void push(Bundle Values) {
        if (!isWatchConnected) return;
        for (String key : Values.keySet()) {
            if (key == SensorStateKeys.UpdatingValue)
                DataSet.update(SmartwatchConstants.SensorBeat, Values.getByte(key));
        }
        if (DataSet.size() == 0) return;
        WatchConnector.send(DataSet);
    }

    @Override
    public void ConnectedStateChanged(Boolean ConnectState) {
        isWatchConnected = ConnectState;
    }

}
