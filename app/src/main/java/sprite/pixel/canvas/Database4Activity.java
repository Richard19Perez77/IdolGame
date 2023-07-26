package sprite.pixel.canvas;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.TransitionDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;

import java.util.Date;

public class Database4Activity extends Activity {

    private MediaPlayer mp;

    // Constants
    public static final String DATABASE_NAME = "highscores.db";
    public static final String HIGH_SCORE_TABLE = "highscore";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_SCORE = "SCORE";
    public static final String COLUMN_NAME = "NAME";
    public static final String COLUMN_CHAIN = "CHAIN";

    private int score;
    private String name;
    private String chain;
    private boolean highScoreSaved = false;

    private SQLiteDatabase scoreDB;

    EditText editTextName;
    WebView webview;
    Button saveHighScore;
    Button playAgain;

    TransitionDrawable trans1, trans2;

    private String PREFS_NAME = "myPreferences";

    public boolean isLogging = false;
    String TAG = "idolLog";

    Context context;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isLogging)
            Log.d(TAG, "onCreate DB Activity");

        setContentView(R.layout.scores);

        context = this;

        mp = MediaPlayer.create(getBaseContext(), R.raw.replay);
        mp.setLooping(true);
        mp.start();

        Bundle extras = getIntent().getExtras();
        String scoreStr = extras != null ? extras.getString("score") : "0";

        try {
            score = Integer.parseInt(scoreStr);
        } catch (NumberFormatException ee) {
            score = 0;
        }

        chain = extras != null ? extras.getString("chain") : "0";

        editTextName = (EditText) findViewById(R.id.editText1);

        webview = (WebView) findViewById(R.id.webView1);
        webview.setBackgroundColor(Color.parseColor("#000000"));

        saveHighScore = (Button) findViewById(R.id.saveScore);
        saveHighScore.setBackgroundResource(R.drawable.revtransitiontopbuttons);
        trans1 = (TransitionDrawable) saveHighScore.getBackground();
        trans1.startTransition(500);
        saveHighScore.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                if (!highScoreSaved) {
                    trans2.reverseTransition(500);
                    trans1.reverseTransition(500);

                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    //Find the currently focused view, so we can grab the correct window token from it.
                    View view = getCurrentFocus();
                    //If no view currently has focus, create a new one, just so we can grab a window token from it
                    if (view == null) {
                        view = new View(context);
                    }
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                    saveHighScore();
                    printRecords();

                    // Retrieve the new list of scores
                    Cursor c = scoreDB.query(HIGH_SCORE_TABLE, new String[]{
                                    COLUMN_NAME, COLUMN_SCORE, COLUMN_CHAIN}, null,
                            null, null, null, COLUMN_SCORE);

                    StringBuilder builder = new StringBuilder();
                    builder.append("<html><body>");
                    builder.append("<h1><FONT COLOR=FF0000><center>High Scores</center></FONT></h1>");
                    builder.append("<table width='100%25'>");
                    builder.append("<tr><td><h2>");
                    builder.append("<FONT COLOR=FF0000>Name </FONT>");
                    builder.append("</h2></td><td><h2>");
                    builder.append("<FONT COLOR=FF0000>Chain </FONT>");
                    builder.append("</h2></td><td><h2>");
                    builder.append("<FONT COLOR=FF0000>Score </FONT>");
                    builder.append("</h2></td></tr>");
                    c.moveToLast();

                    for (int i = c.getCount() - 1; i >= 0; i--) {
                        int count = c.getCount();
                        // Get the data
                        builder.append("<tr><td><h3><FONT COLOR=FFFFFF> ").append(count - i).append(" ");
                        builder.append(c.getString(0));
                        builder.append("</FONT></h3></td><td><h3><FONT COLOR=FFFFFF>");
                        builder.append(c.getString(2));
                        builder.append("</FONT></h3></td><td><h3><FONT COLOR=FFFFFF>");
                        builder.append(c.getString(1));
                        builder.append("</FONT></h3></td></tr>");

                        // Move the cursor
                        c.moveToPrevious();
                    }

                    builder.append("</table></body></html>");
                    webview.loadData(builder.toString(), "text/html", "UTF-8");
                    // Close the cursor
                    c.close();
                    highScoreSaved = true;
                }
            }
        });

        playAgain = (Button) findViewById(R.id.playAgain);
        playAgain.setBackgroundResource(R.drawable.transitiontopbuttons);
        trans2 = (TransitionDrawable) playAgain.getBackground();
        trans2.startTransition(500);

        playAgain.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (highScoreSaved) {
                    trans2.startTransition(500);
                    finish();
                }
            }
        });
    }

    protected void printRecords() {
        // Retrieve the new list of scores
        Cursor c = scoreDB.query(HIGH_SCORE_TABLE, new String[]{COLUMN_NAME,
                        COLUMN_SCORE, COLUMN_CHAIN}, null, null, null, null,
                COLUMN_SCORE);

        StringBuilder builder = new StringBuilder();
        builder.append("<html><body>");
        builder.append("<h1><FONT COLOR=FF0000><center>High Scores</center></FONT></h1>");
        builder.append("<table width='100%25'>");
        builder.append("<tr><td><h2>");
        builder.append("<FONT COLOR=FF0000>Name </FONT>");
        builder.append("</h2></td><td><h2>");
        builder.append("<FONT COLOR=FF0000>Chain </FONT>");
        builder.append("</h2></td><td><h2>");
        builder.append("<FONT COLOR=FF0000>Score </FONT>");
        builder.append("</h2></td></tr>");
        c.moveToLast();

        for (int i = c.getCount() - 1; i >= 0; i--) {
            int count = c.getCount();
            // Get the data
            builder.append("<tr><td><h3><FONT COLOR=FFFFFF> ").append(count - i).append(" ");
            builder.append(c.getString(0));
            builder.append("</FONT></h3></td><td><h3><FONT COLOR=FFFFFF>");
            builder.append(c.getString(2));
            builder.append("</FONT></h3></td><td><h3><FONT COLOR=FFFFFF>");
            builder.append(c.getString(1));
            builder.append("</FONT></h3></td></tr>");

            // Move the cursor
            c.moveToPrevious();
        }

        builder.append("</table></body></html>");
        webview.loadData(builder.toString(), "text/html", "UTF-8");
        // Close the cursor
        c.close();
    }

    private void saveHighScore() {
        name = editTextName.getText().toString();
        if (name.equals("")) {
            long currentToppingTime = new Date().getTime()  % 4;
            int toppingName = (int) currentToppingTime;
            switch (toppingName) {
                case 1:
                    name = "Cosplay";
                    break;
                case 2:
                    name = "Caticorn";
                case 3:
                    name = "Cosplay";
                    break;
                default:
                    name = "Caticorn";
            }
        }

        // Add the values
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_SCORE, score);
        values.put(COLUMN_CHAIN, chain);
        scoreDB.insert(HIGH_SCORE_TABLE, null, values);
        highScoreSaved = true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isLogging)
            Log.d(TAG, "onStart DB Activity");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isLogging)
            Log.d(TAG, "onResume DB Activity");

        scoreDB = openOrCreateDatabase(DATABASE_NAME,
                Context.MODE_PRIVATE, null);
//        scoreDB = openOrCreateDatabase(DATABASE_NAME,
//                SQLiteDatabase.CREATE_IF_NECESSARY, null);
        scoreDB.execSQL("CREATE TABLE IF NOT EXISTS " + HIGH_SCORE_TABLE + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY, " + COLUMN_NAME
                + " VARCHAR, " + COLUMN_SCORE + " INT, " + COLUMN_CHAIN
                + " VARCHAR)");

        printRecords();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isLogging)
            Log.d(TAG, "onPause DB Activity");

        if (mp != null) {
            if (mp.isPlaying())
                mp.stop();
            mp.release();
            mp = null;
        }

        if (scoreDB.isOpen()) {
            if (!highScoreSaved) {
                // First get the values from the EditText
                saveHighScore();
            }
            scoreDB.close();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isLogging)
            Log.d(TAG, "onStop DB Activity");

        if (mp != null) {
            if (mp.isPlaying())
                mp.stop();
            mp.release();
            mp = null;
        }

        if (scoreDB.isOpen()) {
            scoreDB.close();
        }

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (isLogging)
            Log.d(TAG, "onRestart DB Activity");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isLogging)
            Log.d(TAG, "onDestroy DB Activity");

        if (mp != null) {
            if (mp.isPlaying())
                mp.stop();
            mp.release();
            mp = null;
        }

        if (scoreDB.isOpen()) {
            scoreDB.close();
        }
    }
}