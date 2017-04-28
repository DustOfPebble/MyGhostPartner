package services.Track;

import org.xmlpull.v1.XmlPullParser;

public class LoaderGPX implements Runnable  {

    private Track Listener = null;

    public LoaderGPX(Track Listener, String Filename) {
        this.Listener = Listener;


    }

    public void start() {
        this.run();
    }

    @Override
    public void run() {
        // Run in Background priority mode
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        //Listener.appendSegment(N);

 //       XmlPullParser parser = Xml.newPullParser();

        Listener.Loaded();
    }
}