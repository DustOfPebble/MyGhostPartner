package core.Files;

import java.io.InputStreamReader;


public class ParserJSON {
    private FileEvents Listener = null;
    private InputStreamReader Stream = null;

    public ParserJSON(InputStreamReader Stream, FileEvents Listener) {
        this.Listener = Listener;
        this.Stream = Stream;
    }
}
