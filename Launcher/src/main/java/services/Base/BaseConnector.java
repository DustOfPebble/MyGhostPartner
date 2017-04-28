package services.Base;

import android.os.Binder;

public class BaseConnector extends Binder {

    private String LogTag = this.getClass().getSimpleName();

    private BaseCommands Service = null;
    private BaseUpdates Listener = null;

    public void RegisterService(BaseCommands Service) { this.Service = Service; }
    public void RegisterListener(BaseUpdates Listener) { this.Listener = Listener; }



}
