package lib.smartwatch;

import android.content.Context;
import android.util.Log;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.UUID;

public class WatchReceiver extends PebbleKit.PebbleDataReceiver {

    private String LogTag = this.getClass().getSimpleName();
    private WatchEvents Listener = null;

    protected WatchReceiver(UUID subscribedUuid) {
        super(subscribedUuid);
    }

    public void setListener(WatchEvents Listener){
        this.Listener = Listener;
    }

        @Override
    public void receiveData(Context context, int Id, PebbleDictionary Received) {
        PebbleKit.sendAckToPebble(context, Id);
        Long Data = Received.getInteger(0);
        if (Data != null) Log.d(LogTag, "Received value["+Data.intValue()+"]");
        Listener.requestUpdate();
    }
}
