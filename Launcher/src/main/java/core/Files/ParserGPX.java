package core.Files;

import java.io.InputStreamReader;


public class ParserGPX {
    private FileEvents Listener = null;
    private InputStreamReader Stream = null;

    public ParserGPX(InputStreamReader Stream, FileEvents Listener) {
        this.Listener = Listener;
        this.Stream = Stream;
    }
}
