package services.Database;

import android.util.Log;

import java.util.ArrayList;

import core.Files.FilesUtils;

import core.Files.LoaderJSON;
import core.Files.LoaderEvents;
import core.Files.PreSets;
import core.Files.SavedObject;
import core.GPS.CoordsGPS;
import core.GPS.EventsGPS;
import core.GPS.CoreGPS;
import core.Settings.Parameters;
import core.Structures.Coords2D;
import core.Structures.Frame;
import core.Structures.Node;
import core.Structures.Sample;
import services.Hub;

public class AccessDB implements EventsGPS, LoaderEvents {
    private static final String LogTag = AccessDB.class.getSimpleName();

    private int Clearance = 0; // in meters

    private Hub Owner = null;

    private CoordsGPS Origin = null;
    private Coords2D LastMove = null;
    private int Status;

    private LoaderJSON LoadProcess = null;
    private int NbDays = 0;

    private FilesUtils Repository = null;
    private ArrayList<SavedObject> Files = null;
    private int LoadingCount;

    private NodesDB DB = null;

    public AccessDB(Hub Service, int Clearance){
        Owner = Service;
        this.Clearance = Clearance;
        DB = new NodesDB(new Frame(new Coords2D(0,0), Parameters.StorageSize));
        Repository = new FilesUtils(Service);
        Repository.CheckDirectory(PreSets.WorkingSpace);
        Files = Repository.CollectFiles(PreSets.Signature);
        Status = State.Waiting;
    }

    private void StartNewLoader() {
        SavedObject Loading = Files.get(LoadingCount);
        LoadProcess = new LoaderJSON(Loading, this);
        NbDays = Loading.Infos.NbDays;
        LoadProcess.start();
    }

    private void ManageStatus(int Query) {
        if (Query == State.Waiting) {
            if (Status == State.Loading) {
                Origin = null;
                LoadingCount = 0;
                Status = State.Waiting;
                Log.d(LogTag, "State [Loading] --> [Waiting]");
                return;
            }
            if (Status == State.Idle) {
                Origin = null;
                LoadingCount = 0;
                Status = State.Waiting;
                Log.d(LogTag, "State [Idle] --> [Waiting]");
                return;
            }
        }

        if (Query == State.Loading) {
            if (Status == State.Waiting) {
                if (Origin == null) return;
                Log.d(LogTag, "State [Waiting] --> [Loading]");
                Status = State.Loading;
                if (Files.size() == 0) Query = State.Idle;
                else StartNewLoader();
            }
        }

        if (Query == State.Idle) {
            if (Status == State.Loading) {
                if (LoadingCount < Files.size()) {
                    StartNewLoader();
                 } else {
                    Status = State.Idle;
                    Log.d(LogTag, "State [Loading] --> [Idle]");
                    return;
                }
            }
        }
    }

    /**************************************************************
     *  Forwarded Calls from Service
     **************************************************************/
    public ArrayList<Node> getNodes(Frame Zone) {
        DB.clear();
        DB.collect(Zone);
        return NodesDB.Collected;
    }

    public void reload() {
        DB.clear();
        Origin = null;
        LastMove = null;
        ManageStatus(State.Waiting);
    }

    /**************************************************************
     *  Callbacks implementation for CoreGPS Events
     ***************************************************************/
    @Override
    public void UpdatedGPS(CoreGPS Provider){
        // Testing if we are currently free of a Loading process
        if ((LoadProcess != null) && (Status == State.Waiting)) return; // We are waiting for a Loading process...

        if (Origin == null) {
            Origin = Provider.Origin();
            LastMove = Provider.Moved();
            ManageStatus(State.Loading);
            return;
        }

        Node NodeGPS = new Node(Provider.Moved(),Provider.Statistic());
        NodeGPS.Days = 0;

        if (!DB.belongs(NodeGPS)) {
            Owner.NotInZone();
            return;
        }

        if (NodeGPS.Stats.Accuracy > Parameters.LowAccuracyGPS) return;
        if (Coords2D.distance(LastMove, NodeGPS.Move) < Clearance) return;

        LastMove = NodeGPS.Move;
        DB.store(NodeGPS);
    }

    /**************************************************************
     *  Callbacks implementation for Parser Events
     ***************************************************************/
    @Override
    public void loaded(Sample Loaded) {
        if (Status != State.Loading) return;
        Node Snapshot = new Node(Loaded.MovedFrom(Origin),Loaded.Statistic());
        Snapshot.Days = (short)NbDays;
        DB.store(Snapshot);
    }

    @Override
    public void finished(boolean Success) {
        SavedObject Loaded = Files.get(LoadingCount);
        if (Success) Log.d(LogTag, "Loaded "+ Loaded.Infos.NbNodes+ " Nodes from {"+Loaded.Access.getName()+"}");
        else Log.d(LogTag, "Failed on reading ...");
        LoadingCount++;
        LoadProcess = null;
        ManageStatus(State.Idle);
    }
}
