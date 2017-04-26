package core.service.PhoneEvents;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import core.launcher.application.R;
import core.launcher.application.ServicesKeys;
import core.launcher.application.SmartWatchExtension;
import core.launcher.application.WatchState;
import lib.smartwatch.SmartwatchEvents;

public class PhoneEventsProvider extends Service implements PhoneEventsQueries, SmartwatchEvents {

    private String LogTag = this.getClass().getSimpleName();

    private NotificationManager InfoProvider;
    private Notification.Builder InfoCreator;

    private SmartWatchExtension Watch = null;

    private PhoneEventsAccess Connector=null;

    private PhoneEventsCatcher PhoneEvents = null;

    private int WatchStatus = WatchState.Disconnected;
    private int MissedCallsCount = 0;
    private int MissedMessagesCount =0;

    private Bundle EventSnapshot = null;

    public PhoneEventsProvider(){
        EventSnapshot = new Bundle();
        Connector = new PhoneEventsAccess();
    }

    private void PushSystemNotification() {
        int  Info = -1;
        if (WatchStatus == WatchState.Connected) Info = R.string.Connected;
        if (WatchStatus == WatchState.Disconnected) Info = R.string.Disconnected;

        InfoCreator.setContentText(getText(Info));
        InfoProvider.notify(R.string.ID,InfoCreator.build());
    }

    /**************************************************************
     *  Direct Callbacks implementation from PhoneEventsCatcher
     *  - Missed Calls
     *  - Missed Messages
     **************************************************************/
    public void Call() {
        EventSnapshot.clear();
        MissedCallsCount++;
        EventSnapshot.putInt(ServicesKeys.CallsID, MissedCallsCount);
        EventSnapshot.putInt(ServicesKeys.MessagesID, MissedMessagesCount);
        Watch.push(EventSnapshot);

        Connector.CallsCount(MissedCallsCount);
    }

    public void Message() {
        EventSnapshot.clear();
        MissedMessagesCount++;
        EventSnapshot.putInt(ServicesKeys.CallsID, MissedCallsCount);
        EventSnapshot.putInt(ServicesKeys.MessagesID, MissedMessagesCount);
        Watch.push(EventSnapshot);

        Connector.MessagesCount(MissedMessagesCount);
    }

    /**************************************************************
     *  Callbacks implementation for Service management
     **************************************************************/
    @Override
    public void onCreate(){
        super.onCreate();
        InfoProvider = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        InfoCreator = new Notification.Builder(this);
        InfoCreator.setSmallIcon(R.drawable.phone_events);
        InfoCreator.setContentTitle(getText(R.string.ServicePhoneEventsName));

        Connector.RegisterProvider(this);

        Watch = new SmartWatchExtension(getBaseContext());
        WatchStatus = (Watch.isConnected()? WatchState.Connected: WatchState.Disconnected);

        PushSystemNotification();

        PhoneEvents = new PhoneEventsCatcher(this);
        PhoneEvents.enableReceiver(getBaseContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LogTag, "Starting service ...");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LogTag, "Binding service ...");
        return Connector;
    }

    @Override
    public void onDestroy() {
        Log.d(LogTag, "Service is about to quit !");
        InfoProvider.cancel(R.string.ID);
        super.onDestroy();
    }

    /**************************************************************
     *  Callbacks implementation for incoming commands
     **************************************************************/
    @Override
    public void Query() {
        Connector.CallsCount(MissedCallsCount);
        Connector.MessagesCount(MissedMessagesCount);
    }

    /**************************************************************
     *  Callbacks implementation Smartwatch connection state
     **************************************************************/
    @Override
    public void ConnectedStateChanged(Boolean isConnected) {
        if (isConnected) {
            MissedCallsCount = 0;
            MissedMessagesCount = 0;
            WatchStatus = WatchState.Disconnected;
            PushSystemNotification();
            return;
        }

        WatchStatus = WatchState.Connected;
        PushSystemNotification();
    }


}
