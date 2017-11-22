package ch.ethz.inf.vs.hostControllerLib.bluetooth;

import android.os.Handler;
import android.os.Looper;

public class LooperThread extends Thread{

    public Handler handler;

    public void run() {
        Looper.prepare();

        handler = new Handler() {
        };

        Looper.loop();
    }
}