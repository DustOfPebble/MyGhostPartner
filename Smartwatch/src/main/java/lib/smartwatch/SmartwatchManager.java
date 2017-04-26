package lib.smartwatch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.getpebble.android.kit.Constants;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.UUID;

public class SmartwatchManager extends BroadcastReceiver {

    private String LogTag = this.getClass().getSimpleName();

    private UUID Identifier;
    private Context SavedContext;
    private PebbleKit.PebbleDataReceiver DataReceiver;
    private SmartwatchEvents Listener;

    public SmartwatchManager(Context ProvidedContext, SmartwatchEvents Caller, String SmartwatchUUID ) {
        Listener = Caller;
        SavedContext =  ProvidedContext;
        Identifier = UUID.fromString(SmartwatchUUID);
        PebbleKit.registerPebbleConnectedReceiver(SavedContext, this);
        PebbleKit.registerPebbleDisconnectedReceiver(SavedContext, this);

        DataReceiver = new PebbleKit.PebbleDataReceiver(Identifier) {
            @Override
            public void receiveData(Context context, int Id, PebbleDictionary DataBlock) {
                PebbleKit.sendAckToPebble(context, Id);
                Long Data = DataBlock.getInteger(0);
                if (Data != null) Log.d(LogTag, "Received value["+Data.intValue()+"]");
                Listener.requestUpdate();
            }
        };
        PebbleKit.registerReceivedDataHandler(SavedContext, DataReceiver);
    }

    public boolean isConnected() {
        return PebbleKit.isWatchConnected(SavedContext);
    }

    public void send(SmartwatchBundle DataSet) {
        if (!isConnected()) return;
        Log.d (LogTag, "Sending "+DataSet.size()+" keys to Smartwatch...");
        PebbleKit.sendDataToPebble(SavedContext, Identifier, DataSet);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context != SavedContext) return;
        if (intent == null) return;
        String Event = intent.getAction();
        // Connections Management
        if (Event.equals(Constants.INTENT_PEBBLE_CONNECTED)) Listener.ConnectedStateChanged();
        if (Event.equals(Constants.INTENT_PEBBLE_DISCONNECTED)) Listener.ConnectedStateChanged();
    }
}



