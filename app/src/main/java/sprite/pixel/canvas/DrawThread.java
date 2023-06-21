package sprite.pixel.canvas;


import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class DrawThread extends Thread {

    private SurfaceHolder mySurfaceHolder1;
    private DrawPanel myPanel1;
    private boolean myRun1 = false;
    private int mMode;
    public static final int STATE_PAUSE = 2;
    public static final int STATE_RUNNING = 4;

    public DrawThread(SurfaceHolder surfaceHolder, DrawPanel drawPanel) {
        mySurfaceHolder1 = surfaceHolder;
        myPanel1 = drawPanel;
    }

    public void setRunning(boolean run) {
        myRun1 = run;
    }

    public SurfaceHolder getSurfaceHolder() {
        return mySurfaceHolder1;
    }

    public void pause() {
        synchronized (mySurfaceHolder1) {
            if (mMode == STATE_RUNNING)
                setState(STATE_PAUSE);
        }
    }

    public void setState(int mode) {
        synchronized (mySurfaceHolder1) {
            mMode = mode;
        }
    }

    @Override
    public void run() {
        Canvas c;
        while (myRun1) {
            c = null;
            try {
                c = mySurfaceHolder1.lockCanvas(null);
                synchronized (mySurfaceHolder1) {
                    if (c != null)
                        if (mMode == STATE_RUNNING) {
                            myPanel1.draw(c);
                        }
                        else {
                            myPanel1.drawColor(c);
                        }
                }
            } finally {
                // do this in a finally so that if an exception
                // is thrown during the above we don't leave the surface in
                // an inconsistent state
                if (c != null) {
                    mySurfaceHolder1.unlockCanvasAndPost(c);
                }
            }
        }
    }
}