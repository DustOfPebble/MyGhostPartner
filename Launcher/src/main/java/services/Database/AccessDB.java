package services.Database;

import android.os.Handler;
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

public class AccessDB implements EventsGPS, LoaderEvents, Runnable {
    private static final String LogTag = AccessDB.class.getSimpleName();

    private int Clearance = 0; // in meters

    private Handler SyncState;
    private int ExpectedState;
    private int CurrentState;

    private Hub Owner = null;

    private CoordsGPS Origin = null;
    private Coords2D LastMove = null;

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
        CurrentState = State.Waiting;
        SyncState = new Handler();
    }

    private void StartNewLoader() {
        SavedObject Loading = Files.get(LoadingCount);
        LoadProcess = new LoaderJSON(Loading, this);
        NbDays = Loading.Infos.NbDays;
        LoadProcess.start();
    }

    private void ManageStatus(int Query) {
        if (Query == State.Waiting) {
            if (CurrentState == State.Loading) {
                Origin = null;
                LoadingCount = 0;
                CurrentState = State.Waiting;
                Log.d(LogTag, "State [Loading] --> [Waiting]");
                return;
            }
            if (CurrentState == State.Idle) {
                Origin = null;
                LoadingCount = 0;
                CurrentState = State.Waiting;
                Log.d(LogTag, "State [Idle] --> [Waiting]");
                return;
            }
        }

        if (Query == State.Loading) {
            if (CurrentState == State.Waiting) {
                if (Origin == null) return;
                Log.d(LogTag, "State [Waiting] --> [Loading]");
                CurrentState = State.Loading;
                if (Files.size() == 0) Query = State.Idle;
                else StartNewLoader();
            }
        }

        if (Query == State.Idle) {
            if (CurrentState == State.Loading) {
                if (LoadingCount < Files.size()) {
                    StartNewLoader();
                 } else {
                    CurrentState = State.Idle;
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
        ExpectedState = State.Waiting;
        SyncState.post(this);
        Log.d(LogTag, "Requesting DB state to [Waiting]");
    }

    /**************************************************************
     *  Callbacks implementation for CoreGPS Events
     ***************************************************************/
    @Override
    public void UpdatedGPS(CoreGPS Provider){
        // Testing if we are currently free of a Loading process
        if ((LoadProcess != null) && (CurrentState == State.Waiting)) return; // We are waiting for a Loading process...

        if (Origin == null) {
            Origin = Provider.Origin();
            LastMove = Provider.Moved();
            ExpectedState = State.Loading;
            SyncState.post(this);
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
        if (CurrentState != State.Loading) return;
        Node Snapshot = new Node(Loaded.MovedFrom(Origin),Loaded.Statistic());
        Snapshot.Days = (short)NbDays;
        DB.store(Snapshot);
    }

    @Override
    public void finished(boolean Success) {
        SavedObject Loaded = Files.get(LoadingCount);
        if (Success) Log.d(LogTag, "Loaded "+ Loaded.Infos.NbNodes+ " Nodes from {"+Loaded.Access.getName()+"}");
        else Log.d(LogTag, "Failed while reading {"+Loaded.Access.getName()+"}");
        LoadingCount++;
        LoadProcess = null;
        ExpectedState = State.Idle;
        SyncState.post(this);
    }

    @Override
    public void run() {
        ManageStatus(ExpectedState);
    }
}
