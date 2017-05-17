package services.Recorder;

import android.util.Log;

import java.io.File;
import core.Files.FilesUtils;
import core.Files.FileDefs;
import core.GPS.CoreGPS;
import core.GPS.EventsGPS;
import core.Settings.Parameters;
import core.Structures.Coords2D;
import core.Structures.Sample;
import services.Hub;


public class AccessLogs implements EventsGPS {

    private static String LogTag = AccessLogs.class.getSimpleName();
    private static final int Clearance = 1; // in meters

    private FilesUtils Repository = null;
    private File Sink = null;
    private LogsWriter Logger = null;

    private Coords2D LastMove = null;

    public AccessLogs(Hub Service) {
        Repository = new FilesUtils(Service);
    }

    private void CreateLog() {
        String LogFile = Repository.Now(FileDefs.Signature);
        Sink = Repository.CreateFile(LogFile);
        Logger = new LogsWriter();
    }

    /**************************************************************
     *  Forwarded calls from Service
     **************************************************************/
    public void Log(int Mode) {
        if (Mode == Modes.New) {
            if (Sink != null) Logger.write(Sink);
            CreateLog();
        }

        if (Mode == Modes.Continue)
            if (Sink == null)  CreateLog();
    }

    /**************************************************************
     *  Callbacks implementation for CoreGPS Events
     ***************************************************************/
    @Override
    public void UpdatedGPS(CoreGPS Provider){
        if (LastMove == null) LastMove = Provider.Moved();
        if (Coords2D.distance(LastMove, Provider.Moved()) < Clearance) return;
        Sample Fields = Provider.ToSample();
        if (Fields.Accuracy > Parameters.LowAccuracyGPS) return;
        Log.d(LogTag, "Logging Sample with Accuracy["+Fields.Accuracy+"]");
        Logger.append(Fields);
    }
}