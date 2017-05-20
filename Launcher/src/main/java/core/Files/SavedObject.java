package core.Files;

import java.io.File;

public class SavedObject {

    public Descriptor Infos;
    public File Access;

    public SavedObject(File DiskAccess) { Access = DiskAccess; }

}
