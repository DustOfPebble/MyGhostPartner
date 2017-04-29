package services.Base;

import android.os.Binder;

public class BaseConnector extends Binder {

    private String LogTag = this.getClass().getSimpleName();

    private BaseCommands Service = null;
    private BaseEvents Listener = null;

    public void RegisterService(BaseCommands Service) { this.Service = Service; }
    public void RegisterListener(BaseEvents Listener) { this.Listener = Listener; }



}
