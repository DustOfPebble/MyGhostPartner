package services.Database;

import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

import core.Files.FilesUtils;

import core.Files.LoaderJSON;
import core.Files.LoaderEvents;
import core.Files.FileDefs;
import core.GPS.CoordsGPS;
import core.GPS.EventsGPS;
import core.GPS.CoreGPS;
import core.Settings.Parameters;
import core.Structures.Coords2D;
import core.Structures.Extension;
import core.Structures.Frame;
import core.Structures.Node;
import core.Structures.Sample;
import services.Hub;

public class AccessDB implements EventsGPS, LoaderEvents {
    private static final String LogTag = AccessDB.class.getSimpleName();

    private static final int Clearance = 1; // in meters

    private Hub Owner = null;

    private CoordsGPS Origin = null;
    private Coords2D LastMove = null;
    private int Status;

    private LoaderJSON Loader = null;
    private int NbDays = 0;

    private FilesUtils Repository = null;
    private ArrayList<File> Files = null;
    private int LoadingCount;

    private StorageDB DB = null;

    public AccessDB(Hub Service){
        Owner = Service;
        DB = new StorageDB(new Frame(new Coords2D(0,0),new Extension(20000,20000)));
        Repository = new FilesUtils(Service);
        Repository.CheckDirectory(FileDefs.WorkingSpace);
        Files = Repository.CollectFiles(FileDefs.Signature);
        Status = State.Waiting;
    }

    private void ManageStatus(int Query) {
        if (Query == State.Waiting) {
            if (Status == State.Loading) {
                Origin = null;
                LoadingCount = 0;
                Status = State.Waiting;
                Log.d(LogTag, "State DB [Loading] --> [Waiting]");
                return;
            }
            if (Status == State.Idle) {
                Origin = null;
                LoadingCount = 0;
                Status = State.Waiting;
                Log.d(LogTag, "State DB [Idle] --> [Waiting]");
                return;
            }
        }

        if (Query == State.Loading) {
            if (Status == State.Waiting) {
                if (Origin == null) return;
                Log.d(LogTag, "State DB [Waiting] --> [Loading]");
                Status = State.Loading;
            }
        }

        if (Query == State.Idle) {
            if (Status == State.Loading) {
                if (LoadingCount < Files.size()) {
                    Loader = new LoaderJSON(Files.get(LoadingCount), this);
                    LastMove = null;
                    Bundle Params = Loader.header();
                    NbDays = Params.getInt(FileDefs.Days);
                    Loader.start();
                } else {
                    Status = State.Idle;
                    Log.d(LogTag, "State DB [Loading] --> [Idle]");
                    return;
                }
            }
        }
    }

    /**************************************************************
     *  Forwarded Calls from Service
     **************************************************************/
    public ArrayList<Node> getNodes(Frame Zone) {
        DB.collect(Zone);
        return DB.Collected;
    }

    public void reload() { ManageStatus(State.Waiting); }

    /**************************************************************
     *  Callbacks implementation for CoreGPS Events
     ***************************************************************/
    @Override
    public void UpdatedGPS(CoreGPS Provider){
        // Testing if we are currently free of a Loading process
        if ((Origin == null) && (Loader == null)) {
            Origin = Provider.Origin();
            ManageStatus(State.Loading);
        }

        if (Status == State.Waiting) return;

        if (Origin != Provider.Origin()) {
            DB.clear();
            Origin = null;
            ManageStatus(State.Waiting);
            return;
        }

        if (LastMove == null) LastMove = Provider.Moved();

        Node NodeGPS = new Node();
        NodeGPS.Move = Provider.Moved();
        NodeGPS.Stats = Provider.Statistic(0);

        if (!DB.belongs(NodeGPS)) { Owner.OutOfRange(); return; }

        if (Coords2D.distance(LastMove, NodeGPS.Move) < Clearance) return;
        if (NodeGPS.Stats.Accuracy > Parameters.LowAccuracyGPS) return;
        DB.store(NodeGPS);
    }

    /**************************************************************
     *  Callbacks implementation for Parser Events
     ***************************************************************/
    @Override
    public void loaded(Sample Loaded) {
        if (Status != State.Loading) return;
        Node Snapshot = new Node();
        Snapshot.Stats = Loaded.Statistic(NbDays);
        Snapshot.Move = Loaded.MovedFrom(Origin);
        DB.store(Snapshot);
    }

    @Override
    public void finished(boolean Success) {
        if (Success) Log.d(LogTag, "Read "+ Loader.Count()+ " Nodes.");
        else Log.d(LogTag, "Failed on reading ...");
        LoadingCount++;
        Loader = null;
        ManageStatus(State.Idle);
    }
}
