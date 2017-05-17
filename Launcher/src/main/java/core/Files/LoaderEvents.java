package core.Files;

import core.Structures.Sample;

public interface LoaderEvents {
    void loaded(Sample Stored);
    void finished(boolean Success);
}
