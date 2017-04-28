package lib.smartwatch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.getpebble.android.kit.Constants;
import com.getpebble.android.kit.PebbleKit;

import java.util.UUID;

public class WatchLink extends BroadcastReceiver {

    private String LogTag = this.getClass().getSimpleName();

    private UUID Signature;
    private Context Runtime;
    private WatchReceiver DataReceiver;
    private WatchEvents Listener = null;

    public WatchLink(Context Provided, String SmartwatchUUID ) {
        Runtime =  Provided;
        Signature = UUID.fromString(SmartwatchUUID);
        PebbleKit.registerPebbleConnectedReceiver(Runtime, this);
        PebbleKit.registerPebbleDisconnectedReceiver(Runtime, this);
    }

    public void setListener(WatchEvents Caller) {
        Listener = Caller;
        DataReceiver = new WatchReceiver(Signature);
        DataReceiver.setListener(Listener);
        PebbleKit.registerReceivedDataHandler(Runtime, DataReceiver);
    }

    public boolean isConnected() {
        return PebbleKit.isWatchConnected(Runtime);
    }

    public void send(WatchBlock Set) {
        if (!isConnected()) return;
        Log.d (LogTag, "Sending "+Set.size()+" keys to Smartwatch...");
        PebbleKit.sendDataToPebble(Runtime, Signature, Set);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context != Runtime) return;
        if (intent == null) return;
        if (Listener == null) return;

        String Event = intent.getAction();
        // Connections Management
        if (Event.equals(Constants.INTENT_PEBBLE_CONNECTED)) Listener.ConnectedStateChanged();
        if (Event.equals(Constants.INTENT_PEBBLE_DISCONNECTED)) Listener.ConnectedStateChanged();
    }
}



