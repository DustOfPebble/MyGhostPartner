package services.Recorder;

import java.util.Calendar;

import core.Files.FilesUtils;
import core.Files.PreSets;
import core.GPS.CoreGPS;
import core.GPS.EventsGPS;
import core.Settings.Parameters;
import core.Structures.Coords2D;
import core.Structures.Sample;
import services.Hub;

public class AccessLogs implements EventsGPS {

    private static String LogTag = AccessLogs.class.getSimpleName();
    private int Clearance = 5; // in meters

    private FilesUtils Repository = null;
    private LogsWriter Logger = null;

    private Coords2D LastMove = null;

    public AccessLogs(Hub Service, int Clearance) {
        Repository = new FilesUtils(Service);
        Repository.CheckDirectory(PreSets.WorkingSpace);
        this.Clearance = Clearance;
    }

    private void CreateLog() { Logger = new LogsWriter(Calendar.getInstance());  }

    /**************************************************************
     *  Forwarded calls from Service
     **************************************************************/
    public void Log(int Mode) {
        if (Mode == Modes.Create) {
            CreateLog();
            return;
        }

        if (Mode == Modes.Finish)
            if (Logger == null) return;
            Logger.flush(Repository);
            Logger = null;
    }

    /**************************************************************
     *  Callbacks implementation for CoreGPS Events
     ***************************************************************/
    @Override
    public void UpdatedGPS(CoreGPS Provider){
        if (Logger == null) return;

        Sample Fields = Provider.ToSample();
        if (Fields.Accuracy > Parameters.LowAccuracyGPS) return;

        if (LastMove == null) {
            LastMove = Provider.Moved();
            Logger.append(Fields);
        }

        if (Coords2D.distance(LastMove, Provider.Moved()) < Clearance) return;

        Logger.append(Fields);
        LastMove = Provider.Moved();
    }
}