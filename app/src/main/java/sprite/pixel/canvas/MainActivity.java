package sprite.pixel.canvas;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private final boolean isLogging = false;
    private final String TAG = "tagLog";

    public MediaPlayer mp = new MediaPlayer();
    public DrawPanel drawPanel;
    public DrawThread drawThread;
    public int soundStopped;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isLogging)
            Log.d(TAG, "onCreate Main Activity");
        //getSupportActionBar().hide();
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_main);
        drawPanel = findViewById(R.id.drawPanel);
        drawPanel.mp1 = mp;
        drawThread = drawPanel.getThread();
        drawPanel.onCreate();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isLogging)
            Log.d(TAG, "onStart Main Activity");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isLogging)
            Log.d(TAG, "onResume Main Activity");
        // called after the application is ready to start
        if (soundStopped > 0) {
            mp.seekTo(soundStopped);
            mp.start();
        }
        drawPanel.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (isLogging)
            Log.d(TAG, "onActivityResult Main Activity");
        if (isLogging)
            Log.d(TAG, "call idolPanel.onRestart()");
        drawPanel.onRestart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isLogging)
            Log.d(TAG, "onPause Main Activity");

        drawPanel.onPause();
        drawPanel.getThread().pause();

        if (mp != null) {
            if (mp.isPlaying()) {
                mp.pause();
                soundStopped = mp.getCurrentPosition();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isLogging)
            Log.d(TAG, "onStop Main Activity");

        drawPanel.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (isLogging)
            Log.d(TAG, "onRestart Main Activity");
        drawPanel.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isLogging)
            Log.d(TAG, "onDestroy Main Activity");
        if (mp != null) {
            if (mp.isPlaying())
                mp.stop();
            mp.release();
            mp = null;
        }

        drawPanel.onDestroy();
    }
}
