package core.service.PhoneEvents;

import android.os.Binder;
import android.util.Log;

public class PhoneEventsAccess extends Binder {

    private String LogTag = this.getClass().getSimpleName();

    private PhoneEventsQueries Queries = null;
    private PhoneEventsUpdates Events = null;

    public void RegisterProvider(PhoneEventsQueries Provider) { Queries = Provider; }
    public void RegisterListener(PhoneEventsUpdates Listener) { Events = Listener; }

    public void Query() { Queries.Query(); }

    public void CallsCount(int Count) {
        try { Events.CallsCount(Count);}
        catch (Exception Failed) { Log.d(LogTag, "Failed on MissedCalls event");}
    }

    public void MessagesCount(int Count) {
        try {
            Events.MessagesCount(Count); }
        catch (Exception Failed) { Log.d(LogTag, "Failed on MessagesCount event");}
    }

}
