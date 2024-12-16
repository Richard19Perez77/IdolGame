package sprite.pixel.canvas;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.Random;

import sprite.pixel.canvas.movables.Boom;
import sprite.pixel.canvas.movables.LargeItem;
import sprite.pixel.canvas.movables.Bullet;
import sprite.pixel.canvas.movables.Item;
import sprite.pixel.canvas.movables.Effect;
import sprite.pixel.canvas.movables.Light;

@SuppressLint("ViewConstructor")
public class DrawPanel extends SurfaceView implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener {

    public final String path = "android.resource://sprite.pixel.canvas/";

    private final boolean isLogging = false;
    private final String TAG = "tagLog";

    public final int BOSS_SCORE = 100000;
    public final int WIN_SCORE = 90000;
    public final int SPEED_MARKER = 30000;

    public final int SLOW = 800;
    public final int XFAST = 3200;

    public final int PIECES = 49;
    public final int ROW = 7;

    public final int MAX_BOOMS = 60;

    public int startSpeedOffset;
    public int currSpeedOffset;
    public int incSpeed;
    public int speedOffset;

    public static final int STATE_RUNNING = 4;
    static final int HIGH_SCORE = 1;

    // panel variables
    public boolean rapidAvailable = true, finalScoreSet, crit, showChainText, introScreenPlaying, gamePlaying, ready, gameTouchReady, bonusPlaying, bonusInitialized, gameOver, mp2GameplayPlaying, mp3overplaying, mp4WinPlaying, threadAlive, planet1Phase, planet2Phase, planet3Phase, overheat, retry, showFinalScore, playerMapPressed, soundOn = true, allVarsLoaded, timer3Started, loadingPrepared, showHighScoreScreen, loadingScreen = true, incScore = true, incChain = true, createSmallItems = true, justFired;

    public int shieldReserve = 10;
    public int chainBonus;
    public int randPlanetType;
    public int tempScore;
    public int px1;
    public int px2;
    public int py1;
    public int py2;
    public int itemX1;
    public int itemY1;
    public int temp1;
    public int temp2;
    public int fireX1;
    public int fireX2;
    public int fireY1;
    public int fireY2;
    public int wallX;
    public int wallY;
    public int wallBX;
    public int wallBY;
    public int myX;
    public int myY;
    public int newX;
    public int newY;
    public int starTrail;
    public int acc;
    public int playerW;
    public int playerH;
    public int i;
    public int sCurr;
    public int sMax;
    public int aCurr;
    public int aMax;
    public int jx1;
    public int jx2;
    public int jy1;
    public int jy2;
    public int rapidH;
    public int noteHeight;
    public int noteWidth;
    public int itemX2;
    public int itemY2;
    public int fireW;
    public int fireH;
    public int[] starsX;
    public int[] starsY;
    public int speed = 1;
    public int critBonus;
    public int rapidCount = 200;
    public int bossNumber;
    public int roundChain;
    public int currChain;
    public int maxChain;
    public int timer;
    public int itemCount;
    public int maxItems;
    public int maxFire;
    public int itemH;
    public int itemW;
    public int screenH;
    public int fireSpeed;
    public int screenW;
    public int textSize1;
    public int textSize2;
    public int textSize3;
    public int displayBonusTextTimer;
    public int largeW;
    public int largeH;
    public int numStars;
    public int score;
    public int finalScore;

    public long chainTextTimer, tempTimer2, tempTimer3, displayScoreTimer;

    public AudioManager mgr;

    // replace boom map
    public Bitmap b, planetA, planetB, itemSkin, fireSkin;
    public Bitmap[] booms;
    public Boom[] allBooms2;
    public Bitmap rapidW, playerMap, loadingIntro, warningIntro, noteW, noteWoff;

    // large item variables
    public float measure, fSpeed, streamVolumeCurrent, streamVolumeMax, volume, prevVolume = 1, currVolume = 1;

    public String finalScoreText, chainText, textString1, textString2, textString3, textString4, textString5, textString6, scoreString, fireString, shieldString, chainString, rapidS, text, incomingLargeItemText;

    public final int AMMO = 1;
    public final int EXPLODE = 2;
    public final int EFFECT = 3;

    public Intent intent;

    public LargeItem largeItem;

    public MediaPlayer mp1;

    public Bullet[] bulletArray;

    public Paint textColor = new Paint();
    public Paint backGround = new Paint();
    public Paint loadingBG = new Paint();
    public Paint scoreText = new Paint();
    public Paint fireText = new Paint();
    public Paint loadingText = new Paint();
    public Paint shieldText = new Paint();
    public Paint firePaint = new Paint();
    public Paint shieldPaint = new Paint();
    public Paint randPaint = new Paint();
    public Paint[] starsC;
    public Paint timerPaint = new Paint();

    public DrawThread myThread1;

    public Item[] itemArray;

    public SoundPool soundPool;

    public SparseIntArray soundsMap;

    public Thread loadingThread;

    public Light[] lightArray;
    public Light newLight;

    public Effect[] effects = new Effect[shieldReserve];

    public Context context;

    public Random rand = new Random();

    public Activity activity;

    private final String PREFS_NAME = "myPreferences";

    //variables used for shared preferences
    public int numberOfItemsInItemArray;
    private int bonusSpeed;
    private int numberOfStars;
    private int numberOfStarsX;
    private int numberOfStarsY;
    private int numberOfFire;
    private int largeItemPieces;
    private int numberOfAllBooms;
    private int numberOfShields;
    private boolean sharedPreferencesValid;
    private boolean continueThread;
    private boolean isItemArrayLargeItem;
    private boolean isWindowReady = false;

    public DrawPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        getHolder().addCallback(this);
        myThread1 = new DrawThread(getHolder(), this);
        setFocusable(true);
        this.activity = (Activity) context;
    }

    public void onCreate() {
        if (isLogging) Log.d(TAG, "onCreate Panel");

        if (isLogging) Log.d(TAG, "getting Shared Prefs");

        SharedPreferences sharedpreferences;
        sharedpreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sharedPreferencesValid = false;

        getBooleanSharedPreferences(sharedpreferences);

        if (isLogging) Log.d(TAG, "getBooleanSharedPreferences valid " + sharedPreferencesValid);

        //get all int shared preferences
        getIntSharedPreferences(sharedpreferences);

        if (isLogging) Log.d(TAG, "getIntSharedPreferences valid " + sharedPreferencesValid);

        //get all float shared prefs
        getFloatSharedPreferences(sharedpreferences);

        if (isLogging) Log.d(TAG, "getFloatSharedPreferences valid " + sharedPreferencesValid);

        //get all long shared prefs
        getLongSharedPreferences(sharedpreferences);

        if (isLogging) Log.d(TAG, "getLongSharedPreferences valid " + sharedPreferencesValid);

        //get all string shared prefs
        getStringSharedPreferences(sharedpreferences);

        if (isLogging) Log.d(TAG, "getStringSharedPreferences valid " + sharedPreferencesValid);

        //get item array
        getItemArraySharedPreferences(sharedpreferences);

        if (isLogging) Log.d(TAG, "getIdolArraySharedPreferences valid " + sharedPreferencesValid);

        //get starArray
        getStarArray(sharedpreferences);

        if (isLogging) Log.d(TAG, "getStarArray valid " + sharedPreferencesValid);

        //get starX int array
        getStarXIntArraySharedPreferences(sharedpreferences);

        if (isLogging)
            Log.d(TAG, "getStarXIntArraySharedPreferences valid " + sharedPreferencesValid);

        //get starY int array
        getStarYIntArraySharedPreferences(sharedpreferences);

        if (isLogging)
            Log.d(TAG, "getStarYIntArraySharedPreferences valid " + sharedPreferencesValid);

        //new fire array from shared prefs
        getFireArraySharedPreferences(sharedpreferences);

        if (isLogging) Log.d(TAG, "getFireArraySharedPreferences valid " + sharedPreferencesValid);

        //getLargeIdolArray from shared prefs
        getLargeItemSharedPreferences(sharedpreferences);

        if (isLogging) Log.d(TAG, "getLargeIdolSharedPreferences valid " + sharedPreferencesValid);

        //get boom array from shared preferences
        getBoomsSharedPreferences(sharedpreferences);

        if (isLogging) Log.d(TAG, "getBoomsSharedPreferences valid " + sharedPreferencesValid);

        //get shields from shared preferences
        getShieldsSharedPreferences(sharedpreferences);

        if (isLogging) Log.d(TAG, "getShieldsSharedPreferences valid " + sharedPreferencesValid);

        if (!sharedPreferencesValid) {
            resetGameVars();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (sharedPreferencesValid) {
            sharedPreferencesValid = false;
            createNonSavedVars();
        } else if (loadingScreen) {
            loadingScreen(canvas);
        } else if (introScreenPlaying) {
            introScreen(canvas);
        } else if (gamePlaying) {
            gamePlaying(canvas);
        } else if (bonusPlaying) {
            bonusPlaying(canvas);
        } else if (showFinalScore) {
            showFinalScore(canvas);
        } else if (gameOver) {
            gameOver(canvas);
        } else if (showHighScoreScreen) {
            highScoreScreen();
        } else {
            resetGameVars();
            canvas.drawColor(Color.RED);
        }
    }

    public void loadingScreen(Canvas canvas) {
        if (!loadingPrepared) initLoading();

        acc++;

        canvas.drawBitmap(warningIntro, 1, 1, null);

        threadAlive = loadingThread.isAlive();

        if (acc > 200 && allVarsLoaded && !threadAlive) {
            loadingScreen = false;
            introScreenPlaying = true;
            acc = 0;
        }
    }

    public void initResume() {
        screenH = getHeight();
        screenW = getWidth();
        setScreenVarsSizes();

        starsC = new Paint[numStars];
        for (int i = 0; i < numStars; i++) {
            starsC[i] = newColor();
        }

        if (screenH > 1000 && screenW > 600) {
            loadingIntro = BitmapFactory.decodeResource(getResources(), R.drawable.loadingintrolarge);
        } else {
            loadingIntro = BitmapFactory.decodeResource(getResources(), R.drawable.loadingintro);
        }
        loadingIntro = getResizedBitmap(loadingIntro, screenH, screenW);

        warningIntro = BitmapFactory.decodeResource(getResources(), R.drawable.warningintro);
        warningIntro = getResizedBitmap(warningIntro, screenH, screenW);

        textString3 = String.format(getResources().getString(R.string.light));

        mpInit(getContext());
        initSoundPool();

        planetA = BitmapFactory.decodeResource(getResources(), R.drawable.planet1);
        planetB = BitmapFactory.decodeResource(getResources(), R.drawable.planet2b);
        planet1Phase = true;

        for (Light light : lightArray) {
            randPlanetType = rand.nextInt(5);
            getPlanetTypeForStar(light, randPlanetType);
        }

        timerPaint.setStrokeWidth(1);
        timerPaint.setColor(getResources().getColor(R.color.SlateBlue, null));

        timerPaint.setTextSize(textSize2);

        itemSkin = BitmapFactory.decodeResource(getResources(), R.drawable.item1);

        fireSkin = BitmapFactory.decodeResource(getResources(), R.drawable.bullet);

        rapidW = BitmapFactory.decodeResource(getResources(), R.drawable.rapidw);

        noteW = BitmapFactory.decodeResource(getResources(), R.drawable.notew);
        noteWoff = BitmapFactory.decodeResource(getResources(), R.drawable.notewoff);

        booms = new Bitmap[5];

        b = BitmapFactory.decodeResource(getResources(), R.drawable.boom0);
        booms[0] = b;
        b = BitmapFactory.decodeResource(getResources(), R.drawable.boom1);
        booms[1] = b;
        b = BitmapFactory.decodeResource(getResources(), R.drawable.boom2);
        booms[2] = b;
        b = BitmapFactory.decodeResource(getResources(), R.drawable.boom3);
        booms[3] = b;
        b = BitmapFactory.decodeResource(getResources(), R.drawable.boom4);
        booms[4] = b;

        allBooms2 = new Boom[MAX_BOOMS];
        for (int i = 0; i < MAX_BOOMS; i++) {
            allBooms2[i] = new Boom(booms);
        }

        playerMap = BitmapFactory.decodeResource(getResources(), R.drawable.player);

        scoreText.setStrokeWidth(1);
        scoreText.setColor(getResources().getColor(R.color.MediumVioletRed, null));
        scoreText.setTextSize(textSize2);

        textColor.setStrokeWidth(1);
        textColor.setColor(getResources().getColor(R.color.MediumVioletRed, null));
        textColor.setTextSize(textSize1);

        randPaint.setStrokeWidth(1);
        randPaint.setColor(getResources().getColor(R.color.Red, null));
        randPaint.setTextSize(textSize2);

        backGround.setStyle(Paint.Style.FILL);
        backGround.setColor(Color.BLACK);

        loadingBG.setStyle(Paint.Style.FILL);
        loadingBG.setColor(getResources().getColor(R.color.SlateBlue, null));

        firePaint.setColor(getResources().getColor(R.color.Red, null));
        firePaint.setStyle(Paint.Style.FILL);

        shieldPaint.setColor(getResources().getColor(R.color.SlateBlue, null));
        shieldPaint.setStyle(Paint.Style.FILL);

        fireText.setStrokeWidth(1);
        fireText.setColor(getResources().getColor(R.color.Red, null));
        fireText.setTextSize(textSize2);

        loadingText.setStrokeWidth(1);
        loadingText.setColor(Color.RED);
        loadingText.setTextSize(textSize2);
        loadingText.setAntiAlias(true);
        loadingText.setFilterBitmap(true);
        loadingText.setDither(true);
        loadingText.setTextAlign(Align.CENTER);

        shieldText.setStrokeWidth(1);
        shieldText.setColor(getResources().getColor(R.color.SlateBlue, null));
        shieldText.setTextSize(textSize2);

        allVarsLoaded = true;
    }

    public void initLoading() {
        screenH = getHeight();
        screenW = getWidth();
        setScreenVarsSizes();

        if (screenH > 1000 && screenW > 600) {
            loadingIntro = BitmapFactory.decodeResource(getResources(), R.drawable.loadingintrolarge);
        } else {
            loadingIntro = BitmapFactory.decodeResource(getResources(), R.drawable.loadingintro);
        }
        loadingIntro = getResizedBitmap(loadingIntro, screenH, screenW);

        warningIntro = BitmapFactory.decodeResource(getResources(), R.drawable.warningintro);
        warningIntro = getResizedBitmap(warningIntro, screenH, screenW);

        textString3 = String.format(getResources().getString(R.string.light));

        loadingThread = new Thread(() -> {
            mpInit(getContext());
            initSoundPool();

            initGameVars();
            allVarsLoaded = true;

        });
        loadingThread.start();
        loadingPrepared = true;
    }

    public void introScreen(Canvas canvas) {
        acc++;
        if (acc > 50) gameTouchReady = true;

        initStars();
        canvas.drawPaint(backGround);
        drawStars(canvas);
        printIntro(canvas);
    }

    private void printIntro(Canvas canvas) {

        textString3 = "Welcome to";
        textColor.setTextSize(textSize1);
        textColor.setColor(getResources().getColor(R.color.MediumVioletRed, null));
        measure = textColor.measureText(textString3);
        canvas.drawText(textString3, (screenW - measure) / 2, (float) screenH / 5, textColor);

        textString4 = "Idol Blaster";
        randPaint.setTextSize(textSize1 * 2);
        setRandomTextColor(randPaint);
        measure = randPaint.measureText(textString4);
        canvas.drawText(textString4, (screenW - measure) / 2, (float) screenH / 3, randPaint);

        textString1 = "Cosplay Artworks";
        textColor.setTextSize(textSize3);
        textColor.setColor(getResources().getColor(R.color.orange, null));
        measure = textColor.measureText(textString1);
        canvas.drawText(textString1, (screenW - measure) / 2, (float) screenH / 2, textColor);

        textString2 = "Caticornplay";
        measure = textColor.measureText(textString2);
        canvas.drawText(textString2, (screenW - measure) / 2, (float) screenH / 2 + (textColor.getTextSize() * 2), textColor);

        textString5 = "Developer: Rick Perez";
        textColor.setColor(getResources().getColor(R.color.SlateBlue, null));
        measure = textColor.measureText(textString5);
        canvas.drawText(textString5, (screenW - measure) / 2, screenH - ((float) screenH / 4), textColor);

        textString6 = "Music: Ted Gerstle";
        measure = textColor.measureText(textString6);
        canvas.drawText(textString6, (screenW - measure) / 2, screenH - ((float) screenH / 4) + (textColor.getTextSize() * 2), textColor);
    }

    public void gamePlaying(Canvas canvas) {
        // check for shields to be empty
        if (sCurr > 0) {
            // play non bonus game music
            if (!mp4WinPlaying && !mp2GameplayPlaying) startGamePlayMusic();

            if (incScore) {
                timer += 1;
                score += 10;
            }

            // create stars in background
            drawBackground(canvas);

            if (!ready) {
                // give intro greeting
                if (timer >= 300) {
                    ready = true;
                } else if (timer >= 200) {
                    text = "Go";
                } else if (timer >= 100) {
                    text = "Set";
                } else {
                    text = "Ready";
                    myY = screenH - screenH / 3;
                    myX = (screenW - playerW) / 2;
                }
                timer += 1;
                setRandomTextColor(textColor);
                textColor.setTextSize(textSize1);
                measure = textColor.measureText(text);
                canvas.drawText(text, (screenW - measure) / 2, (float) screenH / 2, textColor);
            }

            updateGame(canvas);

            if (checkForEndOfGame()) {
                createSmallItems = false;
                incScore = false;
                incChain = false;
                showFinalScore = true;
                aCurr = 0;
                destroyAllItems();
                displayScoreTimer = 1;
            }
        } else {
            endGamePlaying();
        }
    }

    public boolean checkForEndOfGame() {
        return sCurr <= 0 && incScore;
    }

    public void endGamePlaying() {
        gamePlaying = false;
        gameTouchReady = false;
    }

    public void initBonus() {
        bonusInitialized = true;
        maxItems = 1;
        itemArray = new Item[maxItems];
        isItemArrayLargeItem = false;
        itemCount = 0;
        // create one item at high speed
        bonusSpeed = setSpeed(XFAST);
        itemArray[0] = new Item(bonusSpeed);
        itemArray[0].x = rand.nextInt(screenW - itemW);
        itemH = itemSkin.getHeight();
        itemW = itemSkin.getWidth();
        createSmallItems = true;
        displayBonusTextTimer = 300;
        incScore = true;
        finalScoreSet = false;
    }

    public void bonusPlaying(Canvas c) {
        if (!bonusInitialized) {
            initBonus();
        }

        drawBackground(c);

        // if beginning of bonus stage print out bonus alert
        if (displayBonusTextTimer > 0) {
            displayBonusTextTimer--;
            setRandomTextColor(textColor);
            text = "!! Bonus Stage !!";
            measure = textColor.measureText(text);
            textColor.setTextSize(textSize1);
            c.drawText(text, (float) screenW / 2 - measure / 2, (float) screenH / 2, textColor);
        }

        if (incScore) {
            timer += 1;
            score += 10;
        }

        updateGame(c);
        checkScoreBonus();

        if (sCurr <= 0 && incScore) {
            initEndOfBonus();
        }

        setRandomTextColor(scoreText);
    }

    public void initEndOfBonus() {
        bonusPlaying = false;
        createSmallItems = false;
        incScore = false;
        incChain = false;
        showFinalScore = true;
        displayScoreTimer = 1;
        aCurr = 0;
        destroyAllItems();
        displayScoreTimer = 1;
    }

    public void updateGame(Canvas canvas) {
        // updates game vars and drawing
        getPlayerMapLocationThenDraw(canvas);
        reSetItems();
        // draw idol if any are created
        if (itemCount != 0) {
            drawItems(canvas);
        }
        moveFire();
        drawFire(canvas);
        checkForHits();
        updateBooms(canvas);
        showChainText(canvas);
        showRapidCount(canvas);
        showSoundNote(canvas);
        showShields(canvas, jx1, jx2, jy1, jy2);
        checkScore(canvas);
        printScoreTexts(canvas);
    }

    public void showFinalScore(Canvas canvas) {
        // delay before credits shows non-hidden bonus round final score
        if (displayScoreTimer >= 500) {
            gameTouchReady = true;
            gamePlaying = false;
            bonusPlaying = false;
            showFinalScore = false;
            gameOver = true;
        } else {
            startEndGameMusic();
            drawBackground(canvas);
            createStarField(canvas);
            showFinalScoreText(canvas);
            printScoreTexts(canvas);
            updateBooms(canvas);
            displayScoreTimer++;
        }
    }

    public void drawBackground(@NonNull Canvas c) {
        c.drawPaint(backGround);
        createStarField(c);
        createAsteroidField(c);
        createPlanets(c);
    }

    public void gameOver(@NonNull Canvas canvas) {
        canvas.drawPaint(backGround);
        initStars();
        drawStars(canvas);
        drawPlanets(canvas);
        drawThankYou(canvas);
    }

    private void drawThankYou(@NonNull Canvas canvas) {

        textString3 = "Thanks for Playing";
        textColor.setTextSize(textSize1);
        textColor.setColor(getResources().getColor(R.color.MediumVioletRed, null));
        measure = textColor.measureText(textString3);
        canvas.drawText(textString3, (screenW - measure) / 2, (float) screenH / 5, textColor);

        textString4 = "Idol Blaster";
        randPaint.setTextSize(textSize1 * 2);
        setRandomTextColor(randPaint);
        measure = randPaint.measureText(textString4);
        canvas.drawText(textString4, (screenW - measure) / 2, (float) screenH / 3, randPaint);

        textString1 = "Cosplay Artworks";
        textColor.setTextSize(textSize3);
        textColor.setColor(getResources().getColor(R.color.orange, null));
        measure = textColor.measureText(textString1);
        canvas.drawText(textString1, (screenW - measure) / 2, (float) screenH / 2, textColor);

        textString2 = "Caticornplay";
        measure = textColor.measureText(textString2);
        canvas.drawText(textString2, (screenW - measure) / 2, (float) screenH / 2 + (textColor.getTextSize() * 2), textColor);

        textString5 = "Developer: Rick Perez";
        textColor.setColor(getResources().getColor(R.color.SlateBlue, null));
        measure = textColor.measureText(textString5);
        canvas.drawText(textString5, (screenW - measure) / 2, screenH - ((float) screenH / 4), textColor);

        textString6 = "Music: Ted Gerstle";
        measure = textColor.measureText(textString6);
        canvas.drawText(textString6, (screenW - measure) / 2, screenH - ((float) screenH / 4) + (textColor.getTextSize() * 2), textColor);
    }

    public void highScoreScreen() {
        // method that starts the new activity score screen
        showHighScoreScreen = false;
        Bundle b = new Bundle();
        intent = new Intent(context, Database4Activity.class);
        textString1 = String.valueOf(finalScore);
        textString5 = String.valueOf(roundChain);
        b.putString("score", textString1);
        b.putString("chain", textString5);
        intent.putExtras(b);
        //context.startActivity(intent);
        activity.startActivityForResult(intent, HIGH_SCORE);
    }

    public void mpInit(Context context) {
        try {
            Uri uri = Uri.parse(path + R.raw.intro);
            mp1.setDataSource(context, uri);
            mp1.setLooping(true);
            mp1.setVolume(currVolume, currVolume);
            mp1.setOnPreparedListener(this);
            mp1.prepareAsync();
        } catch (IllegalStateException | IllegalArgumentException | IOException ignored) {
            Log.d(TAG, "mpInit");
        }
    }

    @SuppressWarnings("deprecation")
    public void initSoundPool() {
        fSpeed = 1.0f;
        soundPool = new SoundPool(40, AudioManager.STREAM_MUSIC, 100);
        soundsMap = new SparseIntArray();
        soundsMap.put(AMMO, soundPool.load(getContext(), R.raw.mozzarella, 1));
        soundsMap.put(EXPLODE, soundPool.load(getContext(), R.raw.explosion, 1));
        soundsMap.put(EFFECT, soundPool.load(getContext(), R.raw.shield, 1));

        mgr = (AudioManager) this.getContext().getSystemService(Context.AUDIO_SERVICE);
        streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
        streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    public void releaseSoundPool() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }

    /**
     * When starting a new game from scratch we need all values and bitmaps
     */
    public void initGameVars() {
        starTrail = 0;

        lightArray = new Light[numStars];
        for (int i = 0; i < numStars; i++) {
            randPlanetType = rand.nextInt(5);
            lightArray[i] = createNewStar(randPlanetType);
            lightArray[i].starX = screenW;
            lightArray[i].starY = rand.nextInt(screenH);

        }

        planetA = BitmapFactory.decodeResource(getResources(), R.drawable.planet1);
        planetB = BitmapFactory.decodeResource(getResources(), R.drawable.planet2b);
        planet1Phase = true;

        wallBX = screenW;
        wallBY = 50;
        wallX = screenW;
        wallY = 0;

        gameTouchReady = false;
        bonusPlaying = false;
        bonusInitialized = false;

        starsX = new int[numStars];
        starsY = new int[numStars];
        starsC = new Paint[numStars];
        itemArray = new Item[maxItems];
        bulletArray = new Bullet[maxFire];

        itemCount = 0;
        timer = 0;

        timerPaint.setStrokeWidth(1);
        timerPaint.setColor(getResources().getColor(R.color.SlateBlue, null));

        timerPaint.setTextSize(textSize2);

        itemSkin = BitmapFactory.decodeResource(getResources(), R.drawable.item1);
        itemH = itemSkin.getHeight();
        itemW = itemSkin.getWidth();

        fireSkin = BitmapFactory.decodeResource(getResources(), R.drawable.bullet);
        fireW = fireSkin.getWidth();
        fireH = fireSkin.getHeight();

        rapidW = BitmapFactory.decodeResource(getResources(), R.drawable.rapidw);
        rapidH = rapidW.getHeight();

        noteW = BitmapFactory.decodeResource(getResources(), R.drawable.notew);
        noteWoff = BitmapFactory.decodeResource(getResources(), R.drawable.notewoff);
        noteHeight = noteW.getHeight();
        noteWidth = noteW.getWidth();

        // create large pizza as largeIdol array
        largeItem = new LargeItem(incSpeed, PIECES);
        divideItemIntoLargeItems();

        incomingLargeItemText = "!! Final Idol's Incoming !!";

        for (int i = 0; i < itemArray.length; i++) {
            itemArray[i] = new Item(rand.nextInt(screenW - itemW), -itemH);
        }

        for (int i = 0; i < maxFire; i++) {
            bulletArray[i] = new Bullet();
            bulletArray[i].fireSpeed = fireSpeed;
        }

        initStars();

        currChain = 0;
        roundChain = 0;
        gameOver = false;

        booms = new Bitmap[5];

        b = BitmapFactory.decodeResource(getResources(), R.drawable.boom0);
        booms[0] = b;
        b = BitmapFactory.decodeResource(getResources(), R.drawable.boom1);
        booms[1] = b;
        b = BitmapFactory.decodeResource(getResources(), R.drawable.boom2);
        booms[2] = b;
        b = BitmapFactory.decodeResource(getResources(), R.drawable.boom3);
        booms[3] = b;
        b = BitmapFactory.decodeResource(getResources(), R.drawable.boom4);
        booms[4] = b;

        allBooms2 = new Boom[MAX_BOOMS];
        for (int i = 0; i < MAX_BOOMS; i++) {
            allBooms2[i] = new Boom(booms);
        }

        playerMap = BitmapFactory.decodeResource(getResources(), R.drawable.player);
        playerW = playerMap.getWidth();
        playerH = playerMap.getHeight();

        sMax = 36;
        sCurr = sMax;
        aMax = 36;
        aCurr = aMax;

        scoreText.setStrokeWidth(1);
        scoreText.setColor(getResources().getColor(R.color.MediumVioletRed, null));
        scoreText.setTextSize(textSize2);

        textColor.setStrokeWidth(1);
        textColor.setColor(getResources().getColor(R.color.MediumVioletRed, null));
        textColor.setTextSize(textSize1);

        randPaint.setStrokeWidth(1);
        randPaint.setColor(getResources().getColor(R.color.Red, null));
        randPaint.setTextSize(textSize2);

        backGround.setStyle(Paint.Style.FILL);
        backGround.setColor(Color.BLACK);

        loadingBG.setStyle(Paint.Style.FILL);
        loadingBG.setColor(getResources().getColor(R.color.SlateBlue, null));

        firePaint.setColor(getResources().getColor(R.color.Red, null));
        firePaint.setStyle(Paint.Style.FILL);

        shieldPaint.setColor(getResources().getColor(R.color.SlateBlue, null));
        shieldPaint.setStyle(Paint.Style.FILL);

        fireString = "Ammo";
        fireText.setStrokeWidth(1);
        fireText.setColor(getResources().getColor(R.color.Red, null));
        fireText.setTextSize(textSize2);

        loadingText.setStrokeWidth(1);
        loadingText.setColor(Color.RED);
        loadingText.setTextSize(textSize2);
        loadingText.setAntiAlias(true);
        loadingText.setFilterBitmap(true);
        loadingText.setDither(true);
        loadingText.setTextAlign(Align.CENTER);

        shieldString = "PlayerShields";
        shieldText.setStrokeWidth(1);
        shieldText.setColor(getResources().getColor(R.color.SlateBlue, null));
        shieldText.setTextSize(textSize2);

        speed = 1;
        score = 0;

        for (int i = 0; i < effects.length; i++) {
            effects[i] = new Effect();
        }
    }

    private void divideItemIntoLargeItems() {
        largeW = screenW - screenW / 10;

        Bitmap lItem = BitmapFactory.decodeResource(getResources(), R.drawable.largeskin);
        Bitmap largeSkin = Bitmap.createScaledBitmap(lItem, largeW, largeW, false);

        // re do if the image didn't split correctly
        boolean imageSplit = false;

        while (!imageSplit) {

            int w = largeSkin.getWidth();
            int h = largeSkin.getHeight();
            int bitmapWd3 = w / ROW;
            int bitmapHd3 = h / ROW;

            largeW = bitmapWd3;
            largeH = bitmapHd3;

            int xOffset = (screenW - w);
            xOffset = xOffset / 2;

            int x, y;

            for (int i = 0; i < PIECES; i++) {

                if (i < ROW) {
                    y = 0;
                } else if (i < ROW * 2) {
                    y = bitmapHd3;
                } else if (i < ROW * 3) {
                    y = bitmapHd3 * 2;
                } else if (i < ROW * 4) {
                    y = bitmapHd3 * 3;
                } else if (i < ROW * 5) {
                    y = bitmapHd3 * 4;
                } else if (i < ROW * 6) {
                    y = bitmapHd3 * 5;
                } else {
                    y = bitmapHd3 * 6;
                }

                x = ((i % ROW) * bitmapWd3);

                largeItem.pieces[i].piece = Bitmap.createBitmap(largeSkin, x, y, bitmapWd3, bitmapHd3);

                largeItem.pieces[i].largex = largeItem.pieces[i].x = x + xOffset;

                largeItem.pieces[i].largey = largeItem.pieces[i].y = y - (bitmapHd3 * ROW);

            }
            imageSplit = true;
        }
        largeSkin.recycle();

        if (isItemArrayLargeItem) {
            itemArray = largeItem.pieces;
        }
    }

    public Light createNewStar(int planetType) {
        newLight = new Light();
        newLight.speed = rand.nextInt(6);
        getPlanetTypeForStar(newLight, planetType);
        return newLight;
    }

    private void getPlanetTypeForStar(Light newLight, int planetType) {
        switch (planetType) {
            case 0:
                newLight.star = BitmapFactory.decodeResource(getResources(), R.drawable.smplanet1);
                newLight.starW = newLight.star.getWidth();
                newLight.starH = newLight.star.getHeight();
                break;
            case 1:
                newLight.star = BitmapFactory.decodeResource(getResources(), R.drawable.smplanet2);
                newLight.starW = newLight.star.getWidth();
                newLight.starH = newLight.star.getHeight();
                break;
            case 2:
                newLight.star = BitmapFactory.decodeResource(getResources(), R.drawable.smplanet3);
                newLight.starW = newLight.star.getWidth();
                newLight.starH = newLight.star.getHeight();
                break;
            case 3:
                newLight.star = BitmapFactory.decodeResource(getResources(), R.drawable.smplanet4);
                newLight.starW = newLight.star.getWidth();
                newLight.starH = newLight.star.getHeight();
                break;
            case 4:
                newLight.star = BitmapFactory.decodeResource(getResources(), R.drawable.smplanet5);
                newLight.starW = newLight.star.getWidth();
                newLight.starH = newLight.star.getHeight();
                break;
        }
    }

    public void getPlayerMapLocationThenDraw(@NonNull Canvas canvas) {
        // get player map location and draw him
        jx1 = myX - playerW / 2;
        jx2 = myX + (playerW / 2);
        jy1 = myY - (playerH / 2) - 20;
        jy2 = myY + (playerH / 2) - 20;

        canvas.drawBitmap(playerMap, jx1, jy1, null);
    }

    public void printScoreTexts(@NonNull Canvas canvas) {
        // print score and chain
        scoreText.setTextSize(textSize2);
        chainString = "Chain/Max " + currChain + "/" + maxChain;
        canvas.drawText(chainString, 0, scoreText.getTextSize(), scoreText);

        scoreString = "Score " + score;
        measure = scoreText.measureText(scoreString);
        canvas.drawText(scoreString, screenW - measure, scoreText.getTextSize(), scoreText);

        // draw the ammo and shields count over player map, so its drawn last
        canvas.drawText(fireString, 10, screenH - fireText.getTextSize() * 2, fireText);

        canvas.drawText(shieldString, 10, screenH - shieldText.getTextSize(), shieldText);

        float sPercent = (float) sCurr / (float) sMax;
        float aPercent = (float) aCurr / (float) aMax;
        float shields = ((float) screenW / 2) * sPercent;
        float ammo = ((float) screenW / 2) * aPercent;
        int sh = (int) ((float) screenW / 2 + shields);
        int am = (int) ((float) screenW / 2 + ammo);

        if (sCurr >= 0) {
            canvas.drawRect((float) screenW / 2, screenH - shieldText.getTextSize() * 1, sh, screenH - shieldText.getTextSize() * 2, shieldPaint);
        }

        if (aCurr >= 0) {
            if (!overheat)
                canvas.drawRect((float) screenW / 2, screenH - fireText.getTextSize() * 2, am, screenH - fireText.getTextSize() * 3, firePaint);
            else {
                setRandomTextColor(randPaint);
                canvas.drawRect((float) screenW / 2, screenH - fireText.getTextSize() * 2, am, screenH - fireText.getTextSize() * 3, randPaint);
                if (aCurr > aMax / 2) overheat = false;

            }
        }
    }

    public void startEndGameMusic() {
        if (!mp3overplaying) {
            if (mp2GameplayPlaying) {
                mp1.stop();
                mp2GameplayPlaying = false;
            }
            if (mp4WinPlaying) {
                mp1.stop();
                mp4WinPlaying = false;
            }
            try {
                mp1.reset();
                Uri uri = Uri.parse(path + R.raw.replay);
                mp1.setDataSource(getContext(), uri);
                mp1.setLooping(true);
                mp1.setVolume(currVolume, currVolume);
                mp1.setOnPreparedListener(this);
                mp1.prepareAsync();
            } catch (IllegalStateException | IllegalArgumentException | IOException e) {
                Log.d(TAG, "startEndGameMusic");
            }

            mp3overplaying = true;
        }
    }

    public Paint newColor() {
        Paint p = new Paint();
        i = rand.nextInt(3);
        switch (i) {
            case 0:
                p.setARGB(255, 0, 0, 255);
                break;
            case 1:
                p.setARGB(255, 255, 255, 255);
                break;
            case 2:
                p.setARGB(255, 255, 0, 0);
        }
        return p;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        synchronized (myThread1.getSurfaceHolder()) {
            if (isWindowReady) {
                if (continueThread) {
                    continueThread = false;
                    myThread1.setState(STATE_RUNNING);
                    myThread1.setRunning(true);
                } else if (gameTouchReady) {
                    if (introScreenPlaying) {
                        gamePlaying = true;
                        introScreenPlaying = false;
                    } else if (gameOver) {
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            showHighScoreScreen = true;
                            gameOver = false;
                        }
                    } else if ((gamePlaying || bonusPlaying || showFinalScore) && ready) {
                        // game is playing start actions
                        newX = (int) event.getX();
                        newY = (int) event.getY();

                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                //check if touch down on player map
                                if (newX >= myX - playerW * 2 && newX <= myX + playerW * 2 && newY >= myY - playerW * 2 && newY <= myY + playerW * 2) {
                                    playerMapPressed = true;
                                    myX = newX;
                                    myY = newY;

                                    if (aCurr >= 1) {
                                        createFire(newX, newY - 50);
                                    }
                                }

                                // if tapped on sound icon change on/off
                                if (newX >= (screenW - noteWidth) && newX <= screenW && newY >= noteHeight && newY <= noteHeight * 2) {
                                    toggleSound();
                                }
                                break;
                            case MotionEvent.ACTION_MOVE:
                                // deactivate for rapid fire in bonus stage
                                if (playerMapPressed) {
                                    myX = newX;
                                    myY = newY;

                                    if (rapidAvailable && timer % 2 == 0) {
                                        if (aCurr >= 1) {
                                            createFire(newX, newY - 50);
                                            if (!overheat) rapidCount--;
                                        }
                                        if (rapidCount <= 0) {
                                            rapidAvailable = false;
                                        }
                                    }
                                }
                                break;
                            case MotionEvent.ACTION_UP:
                                if (playerMapPressed) {
                                    myX = newX;
                                    myY = newY;
                                }
                                playerMapPressed = false;
                                break;
                        }
                    }
                }
            }
            return true;
        }
    }

    private void toggleSound() {
        if (soundOn) {
            soundOn = false;
            prevVolume = currVolume;
            currVolume = 0;
            mp1.setVolume(currVolume, currVolume);
        } else {
            soundOn = true;
            currVolume = prevVolume;
            mp1.setVolume(currVolume, currVolume);
        }
    }

    public Bitmap getResizedBitmap(@NonNull Bitmap bm, int newHeight, int newWidth) {
        int w = bm.getWidth();
        int h = bm.getHeight();
        float sW = ((float) newWidth) / w;
        float sH = ((float) newHeight) / h;

        Matrix matrix = new Matrix();
        matrix.postScale(sW, sH);

        return Bitmap.createBitmap(bm, 0, 0, w, h, matrix, false);
    }

    public void checkScoreBonus() {

        if (score % 10000 == 0) {
            itemArray[0].itemSpeed += incSpeed;
        }

        if (score % 7000 == 0) {
            for (Item p : itemArray) {
                p.movementType = 0;
            }
        }

        if (score % 5000 == 0) {
            for (Item p : itemArray) {
                if (p.x > itemW && p.x < screenW - itemW) p.movementType = 1;
            }
            speed++;
        }

        if (score % 500 == 0) {
            createNewItem();
        }

        if (score % 30 == 0 && aCurr < aMax) {
            aCurr += 1;
        }

        if (score % 100 == 0) {
            wallY++;
        }

        if (score % 50 == 0) {
            wallBY++;
        }

        if (score % 40 == 0) {
            wallX--;
        }
        if (score % 20 == 0) {
            wallBX--;
        }
    }

    public void destroyAllItems() {
        for (Item item : itemArray) {
            if (item.exists) {
                itemX1 = item.x;
                itemY1 = item.y;
                // replace with boom map animation
                temp1 = itemX1 - 30;
                temp2 = itemY1 - 30;
                playSound(EXPLODE, fSpeed);
                boomMap(temp1, temp2, crit);
                destroyItems(item);
            }
        }
    }

    public void showShields(Canvas canvas, int jx1, int jx2, int jy1, int jy2) {
        for (Effect effect : effects) {
            if (effect.exists) {
                effect.update(canvas, jx1, jx2, jy1, jy2);
            }
        }
    }

    public void showFinalScoreText(Canvas canvas) {
        if (displayScoreTimer > 0) {

            setRandomTextColor(textColor);

            textColor.setTextSize(textSize1);

            tempScore = score;
            finalScoreText = "GameScore";
            float measure2 = textColor.measureText(finalScoreText);
            canvas.drawText(finalScoreText, ((float) screenW / 2) - measure2, ((float) screenH / 4) + textColor.getTextSize() * 1, textColor);
            finalScoreText = Integer.toString(tempScore);
            measure = textColor.measureText(finalScoreText);
            canvas.drawText(finalScoreText, (screenW - (((float) screenW / 2) - measure2)) - measure, ((float) screenH / 4) + textColor.getTextSize() * 1, textColor);

            tempScore = (maxChain * 24);
            finalScoreText = "ChainBonus";
            canvas.drawText(finalScoreText, ((float) screenW / 2) - measure2, ((float) screenH / 4) + textColor.getTextSize() * 2, textColor);
            finalScoreText = Integer.toString(tempScore);
            measure = textColor.measureText(finalScoreText);
            canvas.drawText(finalScoreText, (screenW - (((float) screenW / 2) - measure2)) - measure, ((float) screenH / 4) + textColor.getTextSize() * 2, textColor);

            tempScore = critBonus;
            finalScoreText = "CritBonus";
            canvas.drawText(finalScoreText, ((float) screenW / 2) - measure2, ((float) screenH / 4) + textColor.getTextSize() * 3, textColor);
            finalScoreText = Integer.toString(tempScore);
            measure = textColor.measureText(finalScoreText);
            canvas.drawText(finalScoreText, (screenW - (((float) screenW / 2) - measure2)) - measure, ((float) screenH / 4) + textColor.getTextSize() * 3, textColor);

            tempScore = score + (maxChain * 24) + critBonus;
            finalScoreText = "Total";
            canvas.drawText(finalScoreText, ((float) screenW / 2) - measure2, ((float) screenH / 4) + textColor.getTextSize() * 5, textColor);
            finalScoreText = Integer.toString(tempScore);
            measure = textColor.measureText(finalScoreText);
            canvas.drawText(finalScoreText, (screenW - (((float) screenW / 2) - measure2)) - measure, ((float) screenH / 4) + textColor.getTextSize() * 5, textColor);

            if (!finalScoreSet) {
                finalScore = score + (maxChain * 24) + critBonus;
                finalScoreSet = true;
            }
            displayScoreTimer++;
        }
    }

    public void startGamePlayMusic() {
        mp1.stop();
        try {
            mp1.reset();
            Uri uri = Uri.parse(path + R.raw.gameplay);
            mp1.setDataSource(getContext(), uri);
            mp1.setLooping(true);
            mp1.setVolume(currVolume, currVolume);
            mp1.setOnPreparedListener(this);
            mp1.prepareAsync();
        } catch (IllegalStateException | IllegalArgumentException | IOException ignored) {
            Log.d(TAG, "startGamePlayMusic");
        }

        mp2GameplayPlaying = true;
    }

    public void showSoundNote(Canvas canvas) {
        if (soundOn) {
            canvas.drawBitmap(noteW, (screenW - noteWidth), rapidH, null);
        } else {
            canvas.drawBitmap(noteWoff, (screenW - noteWidth), rapidH, null);
        }
    }

    public void showRapidCount(Canvas canvas) {
        // deactivate for not rapid graphics on bonus stage
        if (rapidAvailable) {
            canvas.drawBitmap(rapidW, 0, rapidH, null);
            rapidS = " " + rapidCount;
            scoreText.setTextSize(textSize2);
            canvas.drawText(rapidS, 5, rapidH + rapidH + scoreText.getTextSize(), scoreText);
        }
    }

    public void showChainText(Canvas canvas) {
        if (showChainText && chainTextTimer < 100) {
            if (chainTextTimer % 3 == 0) {
                textColor.setColor(getResources().getColor(R.color.Red, null));
            } else if (chainTextTimer % 3 == 1) {
                textColor.setColor(getResources().getColor(R.color.MediumVioletRed, null));
            } else {
                textColor.setColor(getResources().getColor(R.color.SlateBlue, null));
            }
            textColor.setTextSize(textSize1);
            measure = textColor.measureText(chainText);
            canvas.drawText(chainText, (float) screenW / 2 - measure / 2, ((float) screenH / 3), textColor);
            chainTextTimer++;
        }
    }

    public void comboCheck() {
        if (currChain > 0 && currChain % 100 == 0) {
            chainBonus += 50;
            rapidAvailable = true;
            rapidCount += 200 + chainBonus;
            setRandomTextColor(scoreText);
            if (currChain >= maxChain) {
                maxChain = currChain;
            }
            if (currChain >= roundChain) {
                roundChain = currChain;
            }
            showChainText = true;
            chainTextTimer = 0;
            chainText = "!! " + currChain + " HIT CHAIN !!";
        }
    }

    public void setRandomTextColor(@NonNull Paint scoreText2) {
        if (scoreText2.getColor() == getResources().getColor(R.color.MediumVioletRed, null)) {
            scoreText2.setColor(getResources().getColor(R.color.Red, null));
        } else if (scoreText2.getColor() == (getResources().getColor(R.color.Red, null))) {
            scoreText2.setColor(getResources().getColor(R.color.SlateBlue, null));
        } else {
            scoreText2.setColor(getResources().getColor(R.color.MediumVioletRed, null));
        }
    }

    public void updateBooms(Canvas canvas) {
        for (int i = 0; i < MAX_BOOMS; i++) {
            if (allBooms2[i].isBooming()) {
                allBooms2[i].update(canvas);
            }
        }
    }

    public void drawItems(Canvas canvas) {
        if (createSmallItems) {
            for (Item item : itemArray) {
                if (item.exists) {
                    item.moveItem(incSpeed);
                    canvas.drawBitmap(itemSkin, item.x, item.y, null);
                }
            }
        } else {
            for (int i = 0; i < PIECES; i++) {
                if (largeItem.pieces[i].exists) {
                    largeItem.pieces[i].moveItem(incSpeed);
                    canvas.drawBitmap(largeItem.pieces[i].piece, largeItem.pieces[i].x, largeItem.pieces[i].y, null);
                }
            }
        }
    }

    public void createNewItem() {
        if (createSmallItems) {
            if (itemCount <= itemArray.length) {
                for (Item item : itemArray) {
                    if (!item.exists) {
                        item.exists = true;
                        itemCount += 1;
                        break;
                    }
                }
            }
        }
    }

    public void checkForHits() {
        // first check for fire hitting item
        for (int i = 0; i < maxFire; i++) {
            if (bulletArray[i].exists) {
                fireX1 = bulletArray[i].x;
                fireX2 = fireX1 + fireW;
                fireY1 = bulletArray[i].y;
                fireY2 = bulletArray[i].y + fireH;

                // check for fire hitting small items
                // and small items hitting player
                for (Item item : itemArray) {
                    if (item.exists) {
                        itemX1 = item.x;
                        itemX2 = itemX1 + itemW;
                        itemY1 = item.y;
                        itemY2 = itemY1 + itemH;

                        if ((fireX1 >= itemX1 && fireX1 <= itemX2) && (fireY2 >= itemY1 && fireY2 <= itemY2) || (fireX2 >= itemX1 && fireX2 <= itemX2) && (fireY2 >= itemY1 && fireY2 <= itemY2) || (fireX1 >= itemX1 && fireX1 <= itemX2) && (fireY1 >= itemY1 && fireY1 <= itemY2) || (fireX2 >= itemX1 && fireX2 <= itemX2) && (fireY1 >= itemY1 && fireY1 <= itemY2)) {
                            if (incChain) currChain++;
                            comboCheck();
                            if (currChain >= maxChain) {
                                maxChain = currChain;
                            }
                            if (currChain >= roundChain) {
                                roundChain = currChain;
                            }

                            // check for critical hit
                            if ((item.center <= bulletArray[i].center + 2 && item.center >= bulletArray[i].center - 2) || bonusPlaying) {
                                crit = true;
                                critBonus += 500;
                            }

                            // replace graphic with boom map animation
                            temp1 = itemX1 - 30;
                            temp2 = itemY1 - 30;
                            boomMap(temp1, temp2, crit);
                            crit = false;
                            playSound(EXPLODE, fSpeed);
                            destroyFire(bulletArray[i]);
                            destroyItems(item);
                        }
                    }
                }
            }
        }

        // next check for item hitting player
        for (Item item : itemArray) {
            if (item.exists) {
                px1 = item.x;
                px2 = item.x + itemW;
                py1 = item.y;
                py2 = item.y + itemH;

                if ((px1 >= jx1 && px1 <= jx2) && (py2 >= jy1 && py2 <= jy2) || (px2 >= jx1 && px2 <= jx2) && (py2 >= jy1 && py2 <= jy2) || (px1 >= jx1 && px1 <= jx2) && (py1 >= jy1 && py1 <= jy2) || (px2 >= jx1 && px2 <= jx2) && (py1 >= jy1 && py1 <= jy2)) {

                    for (Effect effect : effects) {
                        if (!effect.exists) {
                            effect.start();
                            break;
                        }
                    }

                    temp1 = px1 - 30;
                    temp2 = py1 - 30;
                    boomMap(temp1, temp2, crit);
                    playSound(EXPLODE, fSpeed);
                    destroyItems(item);
                    sCurr -= 3;
                    playSound(EFFECT, fSpeed);
                }
            }
        }
    }

    public void boomMap(int temp1, int temp2, boolean crit) {
        for (int i = 0; i < MAX_BOOMS; i++) {
            if (!allBooms2[i].isBooming()) {
                allBooms2[i].setX(temp1 + 5);
                allBooms2[i].setY(temp2 + 5);
                allBooms2[i].setCrit(crit);
                allBooms2[i].resetCounter();
                allBooms2[i].resetFrames();
                allBooms2[i].setToBooming();
                break;
            }
        }
    }

    public void destroyFire(@NonNull Bullet bullet) {
        bullet.exists = false;
    }

    public void playSound(int sound, float fSpeed) {
        // plays the sounds effect called
        if (soundOn) volume = streamVolumeCurrent / streamVolumeMax;
        else volume = 0;
        soundPool.play(soundsMap.get(sound), volume, volume, 1, 0, fSpeed);
    }

    public void destroyItems(@NonNull Item item) {
        itemCount--;
        item.exists = false;
        if (createSmallItems) {
            item.x = rand.nextInt(screenW - itemW);
            item.y = -itemH;
        }
    }

    public void drawFire(Canvas canvas) {
        for (int i = 0; i < maxFire; i++) {
            if (bulletArray[i].exists) {
                canvas.drawBitmap(fireSkin, bulletArray[i].x, bulletArray[i].y, null);
            }
        }
    }

    public void moveFire() {
        for (int i = 0; i < maxFire; i++) {
            if (bulletArray[i].exists) {
                bulletArray[i].moveFire();
            }
            if (bulletArray[i].y < -50) {
                destroyFire(bulletArray[i]);
            }
        }
    }

    public void createFire(int x, int y) {
        if (!overheat) {
            playSound(AMMO, fSpeed);
            for (int i = 0; i < maxFire; i++) {
                if (!bulletArray[i].exists) {
                    bulletArray[i].exists = true;
                    bulletArray[i].x = x;
                    bulletArray[i].y = y;
                    if (!rapidAvailable) {
                        aCurr -= 1;
                    } else {
                        aCurr--;
                    }
                    if (aCurr == 0) overheat = true;
                    break;
                }
            }
        } else {
            if (aCurr > aMax / 2) {
                overheat = false;
            }
        }

    }

    public void reSetItems() {
        if (createSmallItems) {
            for (Item item : itemArray) {
                if (item.exists) if (item.y > (screenH)) {
                    //items off bottom of screen
                    destroyItems(item);
                    currChain = 0;
                    if (bonusPlaying) {
                        sCurr--;
                        for (Effect effect : effects) {
                            if (!effect.exists) {
                                effect.start();
                                break;
                            }
                        }
                    }
                }
                if (item.x < 0 || item.x + itemW > screenW) {
                    item.changeDirection();
                }
            }
        } else {
            for (Item item : itemArray) {
                if (item.exists && item.y > (screenH + itemH)) {
                    item.exists = false;
                    if (incChain) currChain = 0;
                }
            }
        }
    }

    public void checkScore(Canvas c) {
        // create large item
        if (score == BOSS_SCORE) {

            destroyAllItems();

            for (int i = 0; i < PIECES; i++) {
                largeItem.pieces[i].exists = true;
            }

            itemH = largeH;
            itemW = largeW;

            itemArray = largeItem.pieces;
            isItemArrayLargeItem = true;
            itemCount = PIECES;

            maxItems = largeItem.pieces.length;
            createSmallItems = false;
            tempTimer2 = 1;
            bossNumber++;

        }

        if (score > SPEED_MARKER * 2) {
            if (score % 5000 == 0) {
                speed -= 1;
                starTrail -= 3;
            }
            if (score % 500 == 0) {
                if (numStars > 25) numStars--;

                for (Light light1 : lightArray) {
                    if (!light1.exists) {
                        light1.exists = true;
                        break;
                    }
                }
            }
        } else {
            if (score % 3000 == 0) {
                speed += 1;
                starTrail += 3;
            }
        }

        if (score < BOSS_SCORE) {
            if (score % 8000 == 0) {
                for (Item p : itemArray) {
                    if (p.x > itemW && p.x < screenW - itemW) p.movementType = 1;
                }
            } else if (score % 4000 == 0) {
                for (Item p : itemArray) {
                    p.movementType = 0;
                }
            }
        }

        if (score % 50 == 0) {
            wallBY++;
        }
        if (score % 20 == 0) {
            wallBX--;
        }

        if (score % 100 == 0) {
            wallY++;
        }
        if (score % 40 == 0) {
            wallX--;
        }

        // create second and third large item
        if (score == BOSS_SCORE + 6000 || score == BOSS_SCORE + 11000) {
            tempTimer2 = 1;
            if (bossNumber < 2) bossNumber++;
        }

        // show incoming large item alert
        if (tempTimer2 > 0 && tempTimer2 < 200) {

            if (tempTimer2 % 3 == 0) {
                textColor.setColor(getResources().getColor(R.color.Red, null));
            } else if (tempTimer2 % 3 == 1) {
                textColor.setColor(getResources().getColor(R.color.MediumVioletRed, null));
            } else {
                textColor.setColor(getResources().getColor(R.color.SlateBlue, null));
            }

            textColor.setTextSize(textSize1);
            measure = textColor.measureText(incomingLargeItemText);
            c.drawText(incomingLargeItemText, (float) screenW / 2 - measure / 2, ((float) screenH / 2), textColor);
            tempTimer2++;
        }

        // end of game playing and start of bonus
        if (score == BOSS_SCORE + 16000 && !timer3Started) {
            tempTimer3 = 1;
            timer3Started = true;
        }

        // show scores before bonus round starts
        if (tempTimer3 > 0 && tempTimer3 < 300) {
            falseEndingHighScore(c);
        }

        if (tempTimer3 == 300) {
            gamePlaying = false;
            bonusPlaying = true;
        }

        if (score == BOSS_SCORE + 6500 || score == BOSS_SCORE + 12500) {
            // reset large item to start

            for (int i = 0; i < PIECES; i++) {
                largeItem.pieces[i].y = largeItem.pieces[i].largey;
                largeItem.pieces[i].exists = true;
                largeItem.pieces[i].itemSpeed += (bossNumber + incSpeed);
            }

            itemCount = PIECES;

        }

        if (score > BOSS_SCORE + 500 && !bonusPlaying) {
            if (score % 50 == 0) if (aCurr < aMax) aCurr += 1;
        }

        if (score > WIN_SCORE) {
            // after win theme kicks off barrage of items!
            if (score % 100 == 0) {
                createNewItem();
            }
        }

        if (score % WIN_SCORE == 0 && score != 0 && !mp4WinPlaying) {
            mp1.stop();
            mp2GameplayPlaying = false;
            try {
                mp1.reset();
                Uri uri = Uri.parse(path + R.raw.win);
                mp1.setDataSource(getContext(), uri);
                mp1.setLooping(true);
                if (currVolume != 0) {
                    currVolume = 0.5f;
                }
                mp1.setVolume(currVolume, currVolume);
            } catch (IllegalStateException | IllegalArgumentException | IOException ignored) {
                Log.d(TAG, "checkScore");
            }

            mp1.setOnPreparedListener(this);
            mp1.prepareAsync();
            mp4WinPlaying = true;
        }

        if (score % SPEED_MARKER == 0) {
            for (Item item : itemArray) {
                item.itemSpeed += incSpeed;
            }
        }

        if (score % 7000 == 0 && sCurr < sMax) {
            sCurr += 1;
        }

        if (score % 300 == 0 && score < BOSS_SCORE) {
            createNewItem();
        }

        if (score % 200 == 0 && score < BOSS_SCORE) {
            createNewItem();
        }

        if (score % 30 == 0 && aCurr < aMax) {
            aCurr += 1;
        }

        // create new items at intervals
        if (timer % 50 == 0 && itemCount < maxItems && score < BOSS_SCORE && createSmallItems) {
            createNewItem();
        }

    }

    public void falseEndingHighScore(Canvas c) {
        if (textColor.getColor() == getResources().getColor(R.color.MediumVioletRed, null)) {
            textColor.setColor(getResources().getColor(R.color.Red, null));
        } else if (textColor.getColor() == (getResources().getColor(R.color.Red, null))) {
            textColor.setColor(getResources().getColor(R.color.SlateBlue, null));
        } else {
            textColor.setColor(getResources().getColor(R.color.MediumVioletRed, null));
        }

        setRandomTextColor(textColor);
        textColor.setTextSize(textSize1);

        tempScore = score;
        finalScoreText = "GameScore";
        float measure2 = textColor.measureText(finalScoreText);
        c.drawText(finalScoreText, ((float) screenW / 2) - measure2, ((float) screenH / 4) + textColor.getTextSize() * 1, textColor);
        finalScoreText = Integer.toString(tempScore);
        measure = textColor.measureText(finalScoreText);
        c.drawText(finalScoreText, (screenW - (((float) screenW / 2) - measure2)) - measure, ((float) screenH / 4) + textColor.getTextSize() * 1, textColor);

        tempScore = (maxChain * 24);
        finalScoreText = "ChainBonus";
        c.drawText(finalScoreText, ((float) screenW / 2) - measure2, ((float) screenH / 4) + textColor.getTextSize() * 2, textColor);
        finalScoreText = Integer.toString(tempScore);
        measure = textColor.measureText(finalScoreText);
        c.drawText(finalScoreText, (screenW - (((float) screenW / 2) - measure2)) - measure, ((float) screenH / 4) + textColor.getTextSize() * 2, textColor);

        tempScore = critBonus;
        finalScoreText = "CritBonus";
        c.drawText(finalScoreText, ((float) screenW / 2) - measure2, ((float) screenH / 4) + textColor.getTextSize() * 3, textColor);
        finalScoreText = Integer.toString(tempScore);
        measure = textColor.measureText(finalScoreText);
        c.drawText(finalScoreText, (screenW - (((float) screenW / 2) - measure2)) - measure, ((float) screenH / 4) + textColor.getTextSize() * 3, textColor);

        tempScore = score + (maxChain * 24) + critBonus;
        finalScoreText = "Total";
        c.drawText(finalScoreText, ((float) screenW / 2) - measure2, ((float) screenH / 4) + textColor.getTextSize() * 5, textColor);
        finalScoreText = Integer.toString(tempScore);
        measure = textColor.measureText(finalScoreText);
        c.drawText(finalScoreText, (screenW - (((float) screenW / 2) - measure2)) - measure, ((float) screenH / 4) + textColor.getTextSize() * 5, textColor);

        if (!finalScoreSet) {
            finalScore = score + (maxChain * 24) + critBonus;
            finalScoreSet = true;
        }
        tempTimer3++;

    }

    public void createStarField(Canvas canvas) {
        drawStars(canvas);
        moveStars();
    }

    public void createAsteroidField(Canvas canvas) {
        drawAsteroids(canvas);
        moveAsteroids();
    }

    public void drawAsteroids(Canvas canvas) {
        for (int i = 0; i < numStars; i++) {
            if (lightArray[i].exists) {
                canvas.drawBitmap(lightArray[i].star, lightArray[i].starX, lightArray[i].starY, null);
            } else {
                break;
            }
        }
    }

    public void moveAsteroids() {
        for (Light light : lightArray) {
            if (light.exists) {
                if (light.starX < -light.starW) {
                    light.starX = screenW;
                    light.starY = screenH - rand.nextInt(screenH);
                } else {
                    // star.starY -= star.speed + 1;
                    light.starX -= light.speed + 1;
                }
            } else {
                break;
            }
        }
    }

    public void createPlanets(Canvas canvas) {
        drawPlanets(canvas);
        movePlanets();
    }

    public void drawPlanets(@NonNull Canvas c) {
        c.drawBitmap(planetB, wallBX, wallBY, null);
        c.drawBitmap(planetA, wallX, wallY, null);
    }

    public void movePlanets() {
        if (wallBX < -planetB.getWidth() - 75) {
            wallBX = screenW;
            wallBY = 50;
        }

        if (wallX < -planetA.getWidth() - 50) {
            if (planet1Phase) {
                planetA = BitmapFactory.decodeResource(getResources(), R.drawable.planet2);
                wallX = screenW;
                wallY = 0;
                planet1Phase = false;
                planet2Phase = true;
            } else if (planet2Phase) {
                planetA = BitmapFactory.decodeResource(getResources(), R.drawable.planet3);
                wallX = screenW;
                wallY = 0;
                planet2Phase = false;
                planet3Phase = true;
            } else if (planet3Phase) {
                planetA = BitmapFactory.decodeResource(getResources(), R.drawable.planet1);
                wallX = screenW;
                wallY = 0;
                planet3Phase = false;
                planet1Phase = true;
            }
        }
    }

    public void initStars() {
        // randomly place start on the screen
        starTrail = 0;
        for (int i = 0; i < numStars; i++) {
            starsX[i] = rand.nextInt(screenW);
            starsY[i] = rand.nextInt(screenH);
            starsC[i] = newColor();
        }
    }

    public void drawStars(Canvas canvas) {
        if (numStars > 0) {
            for (int i = 0; i < numStars; i++) {
                canvas.drawRect(starsX[i], starsY[i], starsX[i] + 3, starsY[i] + 3 + starTrail, starsC[i]);
            }
        }
    }

    public void moveStars() {
        for (int i = 0; i < numStars; i++) {
            if (starsY[i] > screenH) {
                starsY[i] = 0;
                starsX[i] = rand.nextInt(screenW);
                starsC[i] = newColor();
            } else {
                starsY[i] += speed;
            }
        }
    }

    public int setSpeed(int rate) {
        if (rate < screenH) {
            int divisor = (screenH - rate);
            startSpeedOffset = currSpeedOffset = rate / divisor;
            incSpeed = 1;
            speedOffset = 1;
            return incSpeed;
        } else if (rate > screenH) {
            incSpeed = rate / screenH;
            int divisor = screenH / incSpeed - rate;
            startSpeedOffset = currSpeedOffset = rate / divisor;
            speedOffset = 1;
        } else {
            incSpeed = 1;
            speedOffset = 1;
        }
        return incSpeed;
    }

    public void setScreenVarsSizes() {
        setSpeed(SLOW);

        if (screenH >= 1000 && screenW >= 600) {
            fireSpeed = 33;
            maxItems = 50;
            maxFire = 75;
            textSize1 = 55;
            textSize2 = 40;
            textSize3 = 37;
            numStars = 50;
        } else if (screenH >= 700 && screenW >= 400) {
            fireSpeed = 28;
            maxItems = 40;
            maxFire = 50;
            textSize1 = 35;
            textSize2 = 25;
            textSize3 = 22;
            numStars = 40;
        } else if (screenH >= 400 && screenW >= 300) {
            fireSpeed = 23;
            maxItems = 20;
            maxFire = 30;
            textSize1 = 25;
            textSize2 = 20;
            textSize3 = 18;
            numStars = 30;
        } else if (screenH >= 200 && screenW >= 200) {
            fireSpeed = 18;
            maxItems = 15;
            maxFire = 25;
            textSize1 = 25;
            textSize2 = 15;
            textSize3 = 13;
            numStars = 20;
        } else {
            fireSpeed = 16;
            maxItems = 10;
            maxFire = 10;
            textSize1 = 20;
            textSize2 = 10;
            textSize3 = 9;
            numStars = 15;
        }
    }

    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        retry = true;
        myThread1.setRunning(false);
        while (retry) {
            try {
                myThread1.join();
                retry = false;
            } catch (InterruptedException e) {
                Log.d(TAG, "surfaceDestroyed");
            }
        }
    }

    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
    }

    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        if (isLogging) {
            Log.d(TAG, "surface Created Panel");
        }
        Thread.State state = myThread1.getState();
        if (isLogging) {
            Log.d(TAG, "thread state " + state);
        }
        if (myThread1.getState() == Thread.State.TERMINATED) {
            myThread1 = new DrawThread(getHolder(), this);
            myThread1.setState(STATE_RUNNING);
            myThread1.setRunning(true);
            myThread1.start();
        } else if (myThread1.getState() == Thread.State.NEW) {
            myThread1.setState(STATE_RUNNING);
            myThread1.setRunning(true);
            myThread1.start();
        }
    }

    public void onPrepared(@NonNull MediaPlayer mp) {
        mp.start();
    }

    public DrawThread getThread() {
        return myThread1;
    }

    public void drawColor(@NonNull Canvas canvas) {
        // game not playing, pausing process
        canvas.drawBitmap(loadingIntro, 1, 1, null);
    }

    private void createNonSavedVars() {
        initResume();
        divideItemIntoLargeItems();
    }

    /**
     * Reset the variables to the initialization state
     */
    private void resetGameVars() {
        acc = 0;
        startSpeedOffset = 0;
        currSpeedOffset = 0;
        incSpeed = 0;
        speedOffset = 0;

        rapidAvailable = true;
        finalScoreSet = false;
        crit = false;
        showChainText = false;
        introScreenPlaying = false;
        gamePlaying = false;
        ready = false;
        gameTouchReady = false;
        bonusPlaying = false;
        bonusInitialized = false;
        gameOver = false;
        mp2GameplayPlaying = false;
        mp3overplaying = false;
        mp4WinPlaying = false;
        threadAlive = false;
        planet1Phase = false;
        planet2Phase = false;
        planet3Phase = false;
        overheat = false;
        retry = false;
        showFinalScore = false;
        playerMapPressed = false;
        soundOn = true;
        allVarsLoaded = false;
        timer3Started = false;
        loadingPrepared = false;
        showHighScoreScreen = false;
        loadingScreen = true;
        incScore = true;
        incChain = true;
        createSmallItems = true;
        justFired = false;
        isItemArrayLargeItem = false;

        shieldReserve = 10;
        chainBonus = 15;
        randPlanetType = 0;
        tempScore = 0;
        px1 = 0;
        px2 = 0;
        py1 = 0;
        py2 = 0;
        itemX1 = 0;
        itemY1 = 0;
        temp1 = 0;
        temp2 = 0;
        fireX1 = 0;
        fireX2 = 0;
        fireY1 = 0;
        fireY2 = 0;
        wallX = 0;
        wallY = 0;
        wallBX = 0;
        wallBY = 0;
        myX = 0;
        myY = 0;
        newX = 0;
        newY = 0;
        starTrail = 0;
        playerW = 0;
        playerH = 0;
        i = 0;
        sCurr = 0;
        sMax = 0;
        aCurr = 0;
        aMax = 0;
        jx1 = 0;
        jx2 = 0;
        jy1 = 0;
        jy2 = 0;
        rapidH = 0;
        noteHeight = 0;
        noteWidth = 0;
        itemX2 = 0;
        itemY2 = 0;
        fireW = 0;
        speed = 1;
        critBonus = 0;
        rapidCount = 200;
        bossNumber = 0;
        roundChain = 0;
        currChain = 0;
        maxChain = 0;
        timer = 0;
        itemCount = 0;
        maxItems = 0;
        maxFire = 0;
        itemH = 0;
        itemW = 0;
        screenH = 0;
        fireSpeed = 0;
        screenW = 0;
        textSize1 = 0;
        textSize2 = 0;
        textSize3 = 0;
        displayBonusTextTimer = 0;
        largeW = 0;
        largeH = 0;
        numStars = 0;

        chainTextTimer = 0;
        tempTimer2 = 0;
        tempTimer3 = 0;
        displayScoreTimer = 0;
        finalScore = 0;
        score = 0;

        measure = 0;
        fSpeed = 0;
        streamVolumeCurrent = 0;
        streamVolumeMax = 0;
        volume = 0;
        prevVolume = 1;
        currVolume = 1;

        finalScoreText = chainText = textString1 = textString2 = textString3 = textString4 = textString5 = textString6 = scoreString = fireString = shieldString = chainString = rapidS = text = incomingLargeItemText = "";
    }

    private void getShieldsSharedPreferences(@NonNull SharedPreferences sharedpreferences) {
        if (sharedpreferences.contains("numberOfShields")) {
            numberOfShields = sharedpreferences.getInt("numberOfShields", -1);

            effects = new Effect[numberOfShields];
            for (int i = 0; i < numberOfShields; i++) {

                effects[i] = new Effect();
                if (sharedpreferences.contains("shieldsjx1" + i)) {
                    effects[i].jx1 = sharedpreferences.getInt("shieldsjx1" + i, -1);
                    if (effects[i].jx1 != -1) sharedPreferencesValid = true;
                } else {
                    sharedPreferencesValid = false;
                }

                if (sharedpreferences.contains("shieldsjy1" + i)) {
                    effects[i].jy1 = sharedpreferences.getInt("shieldsjy1" + i, -1);
                    if (effects[i].jy1 != -1) sharedPreferencesValid = true;
                } else {
                    sharedPreferencesValid = false;
                }

                if (sharedpreferences.contains("shieldsjx2" + i)) {
                    effects[i].jx2 = sharedpreferences.getInt("shieldsjx2" + i, -1);
                    if (effects[i].jx2 != -1) sharedPreferencesValid = true;
                } else {
                    sharedPreferencesValid = false;
                }

                if (sharedpreferences.contains("shieldsjy2" + i)) {
                    effects[i].jy2 = sharedpreferences.getInt("shieldsjy2" + i, -1);
                    if (effects[i].jy2 != -1) sharedPreferencesValid = true;
                } else {
                    sharedPreferencesValid = false;
                }

                if (sharedpreferences.contains("shieldsshieldCount" + i)) {
                    effects[i].shieldCount = sharedpreferences.getInt("shieldsshieldCount" + i, -1);
                    if (effects[i].shieldCount != -1) sharedPreferencesValid = true;
                } else {
                    sharedPreferencesValid = false;
                }

                if (sharedpreferences.contains("shieldsexists" + i)) {
                    effects[i].exists = sharedpreferences.getBoolean("shieldsexists" + i, false);
                    sharedPreferencesValid = true;
                } else {
                    sharedPreferencesValid = false;
                }
            }
        }
    }

    private void getBoomsSharedPreferences(@NonNull SharedPreferences sharedpreferences) {
        if (sharedpreferences.contains("numberOfAllBooms")) {
            numberOfAllBooms = sharedpreferences.getInt("numberOfAllBooms", -1);

            allBooms2 = new Boom[numberOfAllBooms];
            for (int i = 0; i < numberOfAllBooms; i++) {

                allBooms2[i] = new Boom();
                if (sharedpreferences.contains("allBooms2x" + i)) {
                    allBooms2[i].x = sharedpreferences.getInt("allBooms2x" + i, -1);
                    if (allBooms2[i].x != -1) sharedPreferencesValid = true;
                } else {
                    sharedPreferencesValid = false;
                }

                if (sharedpreferences.contains("allBooms2y" + i)) {
                    allBooms2[i].y = sharedpreferences.getInt("allBooms2y" + i, -1);
                    if (allBooms2[i].y != -1) sharedPreferencesValid = true;
                } else {
                    sharedPreferencesValid = false;
                }

                if (sharedpreferences.contains("allBooms2frames" + i)) {
                    allBooms2[i].frames = sharedpreferences.getInt("allBooms2frames" + i, -1);
                    if (allBooms2[i].frames != -1) sharedPreferencesValid = true;
                } else {
                    sharedPreferencesValid = false;
                }

                if (sharedpreferences.contains("allBooms2currentFrame" + i)) {
                    allBooms2[i].currentFrame = sharedpreferences.getInt("allBooms2currentFrame" + i, -1);
                    if (allBooms2[i].currentFrame != -1) sharedPreferencesValid = true;
                } else {
                    sharedPreferencesValid = false;
                }

                if (sharedpreferences.contains("allBooms2currentfps" + i)) {
                    allBooms2[i].fps = sharedpreferences.getInt("allBooms2currentfps" + i, -1);
                    if (allBooms2[i].fps != -1) sharedPreferencesValid = true;
                } else {
                    sharedPreferencesValid = false;
                }

                if (sharedpreferences.contains("allBooms2currentcounter" + i)) {
                    allBooms2[i].counter = sharedpreferences.getInt("allBooms2currentcounter" + i, -1);
                    if (allBooms2[i].counter != -1) sharedPreferencesValid = true;
                } else {
                    sharedPreferencesValid = false;
                }

                if (sharedpreferences.contains("allBooms2critx" + i)) {
                    allBooms2[i].critx = sharedpreferences.getInt("allBooms2critx" + i, -1);
                    if (allBooms2[i].critx != -1) sharedPreferencesValid = true;
                } else {
                    sharedPreferencesValid = false;
                }

                if (sharedpreferences.contains("allBooms2crity" + i)) {
                    allBooms2[i].crity = sharedpreferences.getInt("allBooms2crity" + i, -1);
                    if (allBooms2[i].crity != -1) sharedPreferencesValid = true;
                } else {
                    sharedPreferencesValid = false;
                }

                if (sharedpreferences.contains("allBooms2booming" + i)) {
                    allBooms2[i].booming = sharedpreferences.getBoolean("allBooms2booming" + i, false);
                    sharedPreferencesValid = true;
                } else {
                    sharedPreferencesValid = false;
                }

                if (sharedpreferences.contains("allBooms2crit" + i)) {
                    allBooms2[i].crit = sharedpreferences.getBoolean("allBooms2crit" + i, false);
                    sharedPreferencesValid = true;
                } else {
                    sharedPreferencesValid = false;
                }

                if (sharedpreferences.contains("allBooms2c" + i)) {
                    allBooms2[i].c = sharedpreferences.getString("allBooms2c" + i, "");
                    sharedPreferencesValid = !allBooms2[i].c.isEmpty();
                } else {
                    sharedPreferencesValid = false;
                }
            }
        }
    }

    private void getLargeItemSharedPreferences(@NonNull SharedPreferences sharedpreferences) {
        if (sharedpreferences.contains("largeItemPieces")) {
            largeItemPieces = sharedpreferences.getInt("largeItemPieces", -1);
            largeItem = new LargeItem(incSpeed, largeItemPieces);
            for (int i = 0; i < largeItemPieces; i++) {

                if (sharedpreferences.contains("largeItempieceslargex" + i)) {
                    largeItem.pieces[i].largex = sharedpreferences.getInt("largeItempieceslargex" + i, -1);
                    if (largeItem.pieces[i].largex != -1) sharedPreferencesValid = true;
                } else {
                    sharedPreferencesValid = false;
                }

                if (sharedpreferences.contains("largeItempieceslargey" + i)) {
                    largeItem.pieces[i].largey = sharedpreferences.getInt("largeItempieceslargey" + i, -1);
                    if (largeItem.pieces[i].largey != -1) sharedPreferencesValid = true;
                } else {
                    sharedPreferencesValid = false;
                }

                if (sharedpreferences.contains("largeItempiecesArrayx" + i)) {
                    largeItem.pieces[i].x = sharedpreferences.getInt("largeItempiecesArrayx" + i, -1);
                    if (largeItem.pieces[i].x != -1) sharedPreferencesValid = true;
                } else {
                    sharedPreferencesValid = false;
                }

                if (sharedpreferences.contains("largeItempiecesArrayy" + i)) {
                    largeItem.pieces[i].y = sharedpreferences.getInt("largeItempiecesArrayy" + i, -1);
                    if (largeItem.pieces[i].y != -1) sharedPreferencesValid = true;
                } else {
                    sharedPreferencesValid = false;
                }

                if (sharedpreferences.contains("largeItempiecesArrayItemSpeed" + i)) {
                    largeItem.pieces[i].itemSpeed = sharedpreferences.getInt("largeItempiecesArrayItemSpeed" + i, -1);
                    if (largeItem.pieces[i].itemSpeed != -1) sharedPreferencesValid = true;
                } else {
                    sharedPreferencesValid = false;
                }

                if (sharedpreferences.contains("largeItempiecesArraycenter" + i)) {
                    largeItem.pieces[i].center = sharedpreferences.getInt("largeItempiecesArraycenter" + i, -1);
                    if (largeItem.pieces[i].center != -1) sharedPreferencesValid = true;
                } else {
                    sharedPreferencesValid = false;
                }

                if (sharedpreferences.contains("largeItempiecesArrayexists" + i)) {
                    largeItem.pieces[i].exists = sharedpreferences.getBoolean("largeItempiecesArrayexists" + i, false);
                    sharedPreferencesValid = true;
                } else {
                    sharedPreferencesValid = false;
                }

                if (sharedpreferences.contains("largeItempiecesArraymoveRight" + i)) {
                    largeItem.pieces[i].moveRight = sharedpreferences.getBoolean("largeItempiecesArraymoveRight" + i, false);
                    sharedPreferencesValid = true;
                } else {
                    sharedPreferencesValid = false;
                }
            }
        }
    }

    private void getFireArraySharedPreferences(@NonNull SharedPreferences sharedpreferences) {
        if (sharedpreferences.contains("numberOfFire")) {
            numberOfFire = sharedpreferences.getInt("numberOfFire", -1);

            if (numberOfFire != -1) {
                //create new star array and populate it
                bulletArray = new Bullet[numberOfFire];
                for (int i = 0; i < numberOfFire; i++) {
                    bulletArray[i] = new Bullet();
                    if (sharedpreferences.contains("FireArrayx" + i)) {
                        bulletArray[i].x = sharedpreferences.getInt("FireArrayx" + i, -1);
                        if (bulletArray[i].x != -1) sharedPreferencesValid = true;
                    } else {
                        sharedPreferencesValid = false;
                    }

                    if (sharedpreferences.contains("FireArrayy" + i)) {
                        bulletArray[i].y = sharedpreferences.getInt("FireArrayy" + i, -1);
                        if (bulletArray[i].y != -1) sharedPreferencesValid = true;
                    } else {
                        sharedPreferencesValid = false;
                    }

                    if (sharedpreferences.contains("FireArrayFireSpeed" + i)) {
                        bulletArray[i].fireSpeed = sharedpreferences.getInt("FireArrayFireSpeed" + i, -1);
                        if (bulletArray[i].fireSpeed != -1) sharedPreferencesValid = true;
                    } else {
                        sharedPreferencesValid = false;
                    }

                    if (sharedpreferences.contains("FireArraycenter" + i)) {
                        bulletArray[i].center = sharedpreferences.getInt("FireArraycenter" + i, -1);
                        if (bulletArray[i].center != -1) sharedPreferencesValid = true;
                    } else {
                        sharedPreferencesValid = false;
                    }

                    if (sharedpreferences.contains("FireArrayexists" + i)) {
                        bulletArray[i].exists = sharedpreferences.getBoolean("FireArrayexists", false);
                        sharedPreferencesValid = true;
                    } else {
                        sharedPreferencesValid = false;
                    }
                }
            }
        }
    }

    private void getStarYIntArraySharedPreferences(@NonNull SharedPreferences sharedpreferences) {
        if (sharedpreferences.contains("numberOfStarsY")) {
            numberOfStarsY = sharedpreferences.getInt("numberOfStarsY", -1);

            if (numberOfStarsY != -1) {
                //create new star array and populate it
                starsY = new int[numberOfStarsY];
                for (int i = 0; i < numberOfStarsY; i++) {
                    if (sharedpreferences.contains("starsY" + i)) {
                        starsY[i] = sharedpreferences.getInt("starsY" + i, -1);
                        if (starsY[i] != -1) sharedPreferencesValid = true;
                    } else {
                        sharedPreferencesValid = false;
                    }
                }
            }
        }
    }

    private void getStarXIntArraySharedPreferences(@NonNull SharedPreferences sharedpreferences) {
        if (sharedpreferences.contains("numberOfStarsX")) {
            numberOfStarsX = sharedpreferences.getInt("numberOfStarsX", -1);

            if (numberOfStarsX != -1) {
                //create new star array and populate it
                starsX = new int[numberOfStarsX];
                for (int i = 0; i < numberOfStarsX; i++) {
                    if (sharedpreferences.contains("starsX" + i)) {
                        starsX[i] = sharedpreferences.getInt("starsX" + i, -1);
                        if (starsX[i] != -1) sharedPreferencesValid = true;
                    } else {
                        sharedPreferencesValid = false;
                    }
                }
            }
        }
    }

    private void getStarArray(@NonNull SharedPreferences sharedpreferences) {
        if (sharedpreferences.contains("numberOfStars")) {
            numberOfStars = sharedpreferences.getInt("numberOfStars", -1);

            if (numberOfStars != -1) {
                //create new star array and populate it
                lightArray = new Light[numberOfStars];

                for (int i = 0; i < numberOfStars; i++) {
                    lightArray[i] = new Light();
                    if (sharedpreferences.contains("starArraystarW" + i)) {
                        lightArray[i].starW = sharedpreferences.getInt("starArraystarW" + i, -1);
                        if (lightArray[i].starW != -1) sharedPreferencesValid = true;
                    } else {
                        sharedPreferencesValid = false;
                    }

                    if (sharedpreferences.contains("starArraystarH" + i)) {
                        lightArray[i].starH = sharedpreferences.getInt("starArraystarH" + i, -1);
                        if (lightArray[i].starH != -1) sharedPreferencesValid = true;
                    } else {
                        sharedPreferencesValid = false;
                    }

                    if (sharedpreferences.contains("starArraystarX" + i)) {
                        lightArray[i].starX = sharedpreferences.getInt("starArraystarX" + i, -1);
                        if (lightArray[i].starX != -1) sharedPreferencesValid = true;
                    } else {
                        sharedPreferencesValid = false;
                    }

                    if (sharedpreferences.contains("starArraystarY" + i)) {
                        lightArray[i].starY = sharedpreferences.getInt("starArraystarY" + i, -1);
                        if (lightArray[i].starY != -1) sharedPreferencesValid = true;
                    } else {
                        sharedPreferencesValid = false;
                    }

                    if (sharedpreferences.contains("starArrayspeed" + i)) {
                        lightArray[i].speed = sharedpreferences.getInt("starArrayspeed" + i, -1);
                        if (lightArray[i].speed != -1) sharedPreferencesValid = true;
                    } else {
                        sharedPreferencesValid = false;
                    }

                    if (sharedpreferences.contains("starArrayexists" + i)) {
                        lightArray[i].exists = sharedpreferences.getBoolean("starArrayexists" + i, false);
                        sharedPreferencesValid = true;
                    } else {
                        sharedPreferencesValid = false;
                    }
                }
            }
        }
    }

    private void getItemArraySharedPreferences(@NonNull SharedPreferences sharedpreferences) {
        if (sharedpreferences.contains("numberOfItemsInItemArray")) {
            numberOfItemsInItemArray = sharedpreferences.getInt("numberOfItemsInItemArray", -1);

            if (numberOfItemsInItemArray != -1) {
                //create new array of Items and populate it
                itemArray = new Item[numberOfItemsInItemArray];
                for (int i = 0; i < numberOfItemsInItemArray; i++) {
                    itemArray[i] = new Item();
                    if (sharedpreferences.contains("ItemArraylargex" + i)) {
                        itemArray[i].largex = sharedpreferences.getInt("ItemArraylargex" + i, -1);
                        if (itemArray[i].largex != -1) sharedPreferencesValid = true;
                    } else {
                        sharedPreferencesValid = false;
                    }

                    if (sharedpreferences.contains("ItemArraylargey" + i)) {
                        itemArray[i].largey = sharedpreferences.getInt("ItemArraylargey" + i, -1);
                        if (itemArray[i].largey != -1) sharedPreferencesValid = true;
                    } else {
                        sharedPreferencesValid = false;
                    }

                    if (sharedpreferences.contains("ItemArrayx" + i)) {
                        itemArray[i].x = sharedpreferences.getInt("ItemArrayx" + i, -1);
                        if (itemArray[i].x != -1) sharedPreferencesValid = true;
                    } else {
                        sharedPreferencesValid = false;
                    }

                    if (sharedpreferences.contains("ItemArrayy" + i)) {
                        itemArray[i].y = sharedpreferences.getInt("ItemArrayy" + i, -1);
                        if (itemArray[i].y != -1) sharedPreferencesValid = true;
                    } else {
                        sharedPreferencesValid = false;
                    }

                    if (sharedpreferences.contains("ItemArrayItemSpeed" + i)) {
                        itemArray[i].itemSpeed = sharedpreferences.getInt("ItemArrayItemSpeed" + i, -1);
                        if (itemArray[i].itemSpeed != -1) sharedPreferencesValid = true;
                    } else {
                        sharedPreferencesValid = false;
                    }

                    if (sharedpreferences.contains("ItemArraymovementType" + i)) {
                        itemArray[i].movementType = sharedpreferences.getInt("ItemArraymovementType" + i, -1);
                        if (itemArray[i].movementType != -1) sharedPreferencesValid = true;
                    } else {
                        sharedPreferencesValid = false;
                    }

                    if (sharedpreferences.contains("ItemArraycenter" + i)) {
                        itemArray[i].center = sharedpreferences.getInt("ItemArraycenter" + i, -1);
                        if (itemArray[i].center != -1) sharedPreferencesValid = true;
                    } else {
                        sharedPreferencesValid = false;
                    }

                    if (sharedpreferences.contains("ItemArraymoveRight" + i)) {
                        itemArray[i].moveRight = sharedpreferences.getBoolean("ItemArraymoveRight" + i, false);
                        sharedPreferencesValid = true;
                    } else {
                        sharedPreferencesValid = false;
                    }

                    if (sharedpreferences.contains("ItemArrayexists" + i)) {
                        itemArray[i].exists = sharedpreferences.getBoolean("ItemArrayexists" + i, false);
                        sharedPreferencesValid = true;
                    } else {
                        sharedPreferencesValid = false;
                    }
                }
            }
        } else {
            sharedPreferencesValid = false;
        }
    }

    private void getStringSharedPreferences(@NonNull SharedPreferences sharedpreferences) {
        if (sharedpreferences.contains("text")) {
            text = sharedpreferences.getString("text", "");
            if (text.isEmpty()) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("chainText")) {
            chainText = sharedpreferences.getString("chainText", "");
            if (chainText.isEmpty()) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("rapidS")) {
            rapidS = sharedpreferences.getString("rapidS", "");
            if (rapidS.isEmpty()) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("FireString")) {
            fireString = sharedpreferences.getString("FireString", "");
            if (fireString.isEmpty()) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("textString1")) {
            textString1 = sharedpreferences.getString("textString1", "");
            if (textString1.isEmpty()) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("textString2")) {
            textString2 = sharedpreferences.getString("textString2", "");
            if (textString2.isEmpty()) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("textString3")) {
            textString3 = sharedpreferences.getString("textString3", "");
            if (textString3.isEmpty()) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("textString4")) {
            textString4 = sharedpreferences.getString("textString4", "");
            if (textString4.isEmpty()) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("textString5")) {
            textString5 = sharedpreferences.getString("textString5", "");
            if (textString5.isEmpty()) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("textString6")) {
            textString6 = sharedpreferences.getString("textString6", "");
            if (textString6.isEmpty()) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("incomingLargeItemText")) {
            incomingLargeItemText = sharedpreferences.getString("incomingLargeItemText", "");
            if (incomingLargeItemText.isEmpty()) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("doughString")) {
            shieldString = sharedpreferences.getString("doughString", "");
            if (shieldString.isEmpty()) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("chainString")) {
            chainString = sharedpreferences.getString("chainString", "");
            if (chainString.isEmpty()) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("finalScoreText")) {
            finalScoreText = sharedpreferences.getString("finalScoreText", "");
            if (finalScoreText.isEmpty()) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }
    }

    private void getLongSharedPreferences(@NonNull SharedPreferences sharedpreferences) {
        if (sharedpreferences.contains("score")) {
            score = sharedpreferences.getInt("score", -1);
            if (score != -1L) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("tempTimer2")) {
            tempTimer2 = sharedpreferences.getLong("tempTimer2", -1L);
            if (tempTimer2 != -1L) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("tempTimer3")) {
            tempTimer3 = sharedpreferences.getLong("tempTimer3", -1L);
            if (tempTimer3 != -1L) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("finalScore")) {
            finalScore = sharedpreferences.getInt("finalScore", -1);
            if (finalScore != -1L) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("displayScoreTimer")) {
            displayScoreTimer = sharedpreferences.getLong("displayScoreTimer", -1L);
            if (displayScoreTimer != -1L) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("chainTextTimer")) {
            chainTextTimer = sharedpreferences.getLong("chainTextTimer", -1L);
            if (chainTextTimer != -1L) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }
    }

    private void getFloatSharedPreferences(@NonNull SharedPreferences sharedpreferences) {
        if (sharedpreferences.contains("volume")) {
            volume = sharedpreferences.getFloat("volume", -1f);
            if (volume != -1f) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("prevVolume")) {
            prevVolume = sharedpreferences.getFloat("prevVolume", -1f);
            if (prevVolume != -1f) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("currVolume")) {
            currVolume = sharedpreferences.getFloat("currVolume", -1f);
            if (currVolume != -1f) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }
    }

    private void getIntSharedPreferences(@NonNull SharedPreferences sharedpreferences) {
        if (sharedpreferences.contains("acc")) {
            acc = sharedpreferences.getInt("acc", -1);
            if (acc != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("largeW")) {
            largeW = sharedpreferences.getInt("largeW", -1);
            if (largeW != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("largeH")) {
            largeH = sharedpreferences.getInt("largeH", -1);
            if (largeH != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("aCurr")) {
            aCurr = sharedpreferences.getInt("aCurr", -1);
            if (aCurr != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("sCurr")) {
            sCurr = sharedpreferences.getInt("sCurr", -1);
            if (sCurr != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("timer")) {
            timer = sharedpreferences.getInt("timer", -1);
            if (timer != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("myX")) {
            myX = sharedpreferences.getInt("myX", -1);
            if (myX != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("myY")) {
            myY = sharedpreferences.getInt("myY", -1);
            if (myY != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("maxItems")) {
            maxItems = sharedpreferences.getInt("maxItems", -1);
            if (maxItems != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("ItemCount")) {
            itemCount = sharedpreferences.getInt("ItemCount", -1);
            if (itemCount != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("bonusSpeed")) {
            bonusSpeed = sharedpreferences.getInt("bonusSpeed", -1);
            if (bonusSpeed != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("ItemH")) {
            itemH = sharedpreferences.getInt("ItemH", -1);
            if (itemH != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("ItemW")) {
            itemW = sharedpreferences.getInt("ItemW", -1);
            if (itemW != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("displayBonusTextTimer")) {
            displayBonusTextTimer = sharedpreferences.getInt("displayBonusTextTimer", -1);
            if (displayBonusTextTimer != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("textSize1")) {
            textSize1 = sharedpreferences.getInt("textSize1", -1);
            if (textSize1 != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("jx1")) {
            jx1 = sharedpreferences.getInt("jx1", -1);
            if (jx1 != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("jx2")) {
            jx2 = sharedpreferences.getInt("jx2", -1);
            if (jx2 != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("jy1")) {
            jy1 = sharedpreferences.getInt("jy1", -1);
            if (jy1 != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("jy2")) {
            jy2 = sharedpreferences.getInt("jy2", -1);
            if (jy2 != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("roundChain")) {
            roundChain = sharedpreferences.getInt("roundChain", -1);
            if (roundChain != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("starTrail")) {
            starTrail = sharedpreferences.getInt("starTrail", -1);
            if (starTrail != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("wallbX")) {
            wallBX = sharedpreferences.getInt("wallbX", -1);
            if (wallBX != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("wallbY")) {
            wallBY = sharedpreferences.getInt("wallbY", -1);
            if (wallBY != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("wallX")) {
            wallX = sharedpreferences.getInt("wallX", -1);
            if (wallX != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("wallY")) {
            wallY = sharedpreferences.getInt("wallY", -1);
            if (wallY != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("FireW")) {
            fireW = sharedpreferences.getInt("FireW", -1);
            if (fireW != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("FireH")) {
            fireH = sharedpreferences.getInt("FireH", -1);
            if (fireH != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("rapidH")) {
            rapidH = sharedpreferences.getInt("rapidH", -1);
            if (rapidH != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("noteHeight")) {
            noteHeight = sharedpreferences.getInt("noteHeight", -1);
            if (noteHeight != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("noteWidth")) {
            noteWidth = sharedpreferences.getInt("noteWidth", -1);
            if (noteWidth != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("incSpeed")) {
            incSpeed = sharedpreferences.getInt("incSpeed", -1);
            if (incSpeed != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("maxFires")) {
            maxFire = sharedpreferences.getInt("maxFires", -1);
            if (maxFire != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("currChain")) {
            currChain = sharedpreferences.getInt("currChain", -1);
            if (currChain != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("playerMapW")) {
            playerW = sharedpreferences.getInt("playerMapW", -1);
            if (playerW != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("playerMapH")) {
            playerH = sharedpreferences.getInt("playerMapH", -1);
            if (playerH != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("sMax")) {
            sMax = sharedpreferences.getInt("sMax", -1);
            if (sMax != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("aMax")) {
            aMax = sharedpreferences.getInt("aMax", -1);
            if (aMax != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("speed")) {
            speed = sharedpreferences.getInt("speed", -1);
            if (speed != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("i")) {
            i = sharedpreferences.getInt("i", -1);
            if (i != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("newX")) {
            newX = sharedpreferences.getInt("newX", -1);
            if (newX != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("newY")) {
            newY = sharedpreferences.getInt("newY", -1);
            if (newY != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("rapidCount")) {
            rapidCount = sharedpreferences.getInt("rapidCount", -1);
            if (rapidCount != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("Itemx1")) {
            itemX1 = sharedpreferences.getInt("Itemx1", -1);
            if (itemX1 != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("Itemx2")) {
            itemX2 = sharedpreferences.getInt("Itemx2", -1);
            if (itemX2 != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("Itemy1")) {
            itemY1 = sharedpreferences.getInt("Itemy1", -1);
            if (itemY1 != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("Itemy2")) {
            itemY2 = sharedpreferences.getInt("Itemy2", -1);
            if (itemY2 != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("temp1")) {
            temp1 = sharedpreferences.getInt("temp1", -1);
            if (temp1 != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("temp2")) {
            temp2 = sharedpreferences.getInt("temp2", -1);
            if (temp2 != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("tempscore")) {
            tempScore = sharedpreferences.getInt("tempscore", -1);
            if (tempScore != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("maxChain")) {
            maxChain = sharedpreferences.getInt("maxChain", -1);
            if (maxChain != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("critBonus")) {
            critBonus = sharedpreferences.getInt("critBonus", -1);
            if (critBonus != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("chainBonus")) {
            chainBonus = sharedpreferences.getInt("chainBonus", -1);
            if (chainBonus != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("Firex1")) {
            fireX1 = sharedpreferences.getInt("Firex1", -1);
            if (fireX1 != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("Firex2")) {
            fireX2 = sharedpreferences.getInt("Firex2", -1);
            if (fireX2 != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("ItemW")) {
            itemW = sharedpreferences.getInt("ItemW", -1);
            if (itemW != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("ItemH")) {
            itemH = sharedpreferences.getInt("ItemH", -1);
            if (itemH != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("px1")) {
            px1 = sharedpreferences.getInt("px1", -1);
            if (px1 != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("px2")) {
            px2 = sharedpreferences.getInt("px2", -1);
            if (px2 != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("py1")) {
            py1 = sharedpreferences.getInt("py1", -1);
            if (py1 != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("py2")) {
            py2 = sharedpreferences.getInt("py2", -1);
            if (py2 != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("bossNumber")) {
            bossNumber = sharedpreferences.getInt("bossNumber", -1);
            if (bossNumber != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("numStars")) {
            numStars = sharedpreferences.getInt("numStars", -1);
            if (numStars != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("startSpeedOffset")) {
            startSpeedOffset = sharedpreferences.getInt("startSpeedOffset", -1);
            if (startSpeedOffset != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("speedOffset")) {
            speedOffset = sharedpreferences.getInt("speedOffset", -1);
            if (speedOffset != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("currSpeedOffset")) {
            currSpeedOffset = sharedpreferences.getInt("currSpeedOffset", -1);
            if (currSpeedOffset != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("FireSpeed")) {
            fireSpeed = sharedpreferences.getInt("FireSpeed", -1);
            if (fireSpeed != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("textSize1")) {
            textSize1 = sharedpreferences.getInt("textSize1", -1);
            if (textSize1 != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("textSize2")) {
            textSize2 = sharedpreferences.getInt("textSize2", -1);
            if (textSize2 != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("textSize3")) {
            textSize3 = sharedpreferences.getInt("textSize3", -1);
            if (textSize3 != -1) sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

    }

    private void getBooleanSharedPreferences(@NonNull SharedPreferences sharedpreferences) {
        if (sharedpreferences.contains("loadingScreen")) {
            loadingScreen = sharedpreferences.getBoolean("loadingScreen", false);
            sharedPreferencesValid = !loadingScreen;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("introScreenPlaying")) {
            introScreenPlaying = sharedpreferences.getBoolean("introScreenPlaying", false);
            sharedPreferencesValid = !introScreenPlaying;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("showChainText")) {
            showChainText = sharedpreferences.getBoolean("showChainText", false);
            sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }


        if (sharedpreferences.contains("gamePlaying")) {
            gamePlaying = sharedpreferences.getBoolean("gamePlaying", false);
            sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("bonusPlaying")) {
            bonusPlaying = sharedpreferences.getBoolean("bonusPlaying", false);
            sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("showFinalScore")) {
            showFinalScore = sharedpreferences.getBoolean("showFinalScore", false);
            sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("gameOver")) {
            gameOver = sharedpreferences.getBoolean("gameOver", false);
            sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("showHighScoreScreen")) {
            showHighScoreScreen = sharedpreferences.getBoolean("showHighScoreScreen", false);
            sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("allVarsLoaded")) {
            allVarsLoaded = sharedpreferences.getBoolean("allVarsLoaded", false);
            sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("gameTouchReady")) {
            gameTouchReady = sharedpreferences.getBoolean("gameTouchReady", false);
            sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("mp4winplaying")) {
            mp4WinPlaying = sharedpreferences.getBoolean("mp4winplaying", false);
            sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("mp2gameplayplaying")) {
            mp2GameplayPlaying = sharedpreferences.getBoolean("mp2gameplayplaying", false);
            sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("mp3overplaying")) {
            mp3overplaying = sharedpreferences.getBoolean("mp3overplaying", false);
            sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("incScore")) {
            incScore = sharedpreferences.getBoolean("incScore", false);
            sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("ready")) {
            ready = sharedpreferences.getBoolean("ready", false);
            sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("createSmallItems")) {
            createSmallItems = sharedpreferences.getBoolean("createSmallItems", false);
            sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("incChain")) {
            incChain = sharedpreferences.getBoolean("incChain", false);
            sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("bonusInitialized")) {
            bonusInitialized = sharedpreferences.getBoolean("bonusInitialized", false);
            sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("finalScoreSet")) {
            finalScoreSet = sharedpreferences.getBoolean("finalScoreSet", false);
            sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("planet1Phase")) {
            planet1Phase = sharedpreferences.getBoolean("planet1Phase", false);
            sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("planet2Phase")) {
            planet2Phase = sharedpreferences.getBoolean("planet2Phase", false);
            sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("planet3Phase")) {
            planet3Phase = sharedpreferences.getBoolean("planet3Phase", false);
            sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("overheat")) {
            overheat = sharedpreferences.getBoolean("overheat", false);
            sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("playerMapPressed")) {
            playerMapPressed = sharedpreferences.getBoolean("playerMapPressed", false);
            sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("rapidAvailable")) {
            rapidAvailable = sharedpreferences.getBoolean("rapidAvailable", false);
            sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("justFired")) {
            justFired = sharedpreferences.getBoolean("justFired", false);
            sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("soundOn")) {
            soundOn = sharedpreferences.getBoolean("soundOn", false);
            sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("crit")) {
            crit = sharedpreferences.getBoolean("crit", false);
            sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("timer3Started")) {
            timer3Started = sharedpreferences.getBoolean("timer3Started", false);
            sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("isItemArrayLargeItem")) {
            isItemArrayLargeItem = sharedpreferences.getBoolean("isItemArrayLargeItem", false);
            sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }
    }

    public void onResume() {
        if (soundPool == null) initSoundPool();
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        isWindowReady = hasWindowFocus;
    }

    public void onRestart() {
        Thread.State threadState = myThread1.getState();
        Log.d(TAG, "thread state " + threadState);
        if (threadState == Thread.State.RUNNABLE) {
            continueThread = true;
        }
    }

    public void onPause() {
        getThread().pause();
    }

    public void onStop() {
        if (isLogging) Log.d(TAG, "storing Shared Pres");

        SharedPreferences sharedpreferences;
        sharedpreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.clear();

        editor.putBoolean("showHighScoreScreen", showHighScoreScreen);
        editor.putBoolean("loadingScreen", loadingScreen);
        editor.putBoolean("showChainText", showChainText);
        editor.putBoolean("introScreenPlaying", introScreenPlaying);
        editor.putBoolean("gamePlaying", gamePlaying);
        editor.putBoolean("bonusPlaying", bonusPlaying);
        editor.putBoolean("showFinalScore", showFinalScore);
        editor.putBoolean("gameOver", gameOver);
        editor.putBoolean("showHighScoreScreen", showHighScoreScreen);
        editor.putBoolean("allVarsLoaded", allVarsLoaded);
        editor.putBoolean("gameTouchReady", gameTouchReady);
        editor.putBoolean("mp4winplaying", mp4WinPlaying);
        editor.putBoolean("mp2gameplayplaying", mp2GameplayPlaying);
        editor.putBoolean("mp3overplaying", mp3overplaying);
        editor.putBoolean("ready", ready);
        editor.putBoolean("createSmallItems", createSmallItems);
        editor.putBoolean("incChain", incChain);
        editor.putBoolean("showFinalScore", showFinalScore);
        editor.putBoolean("incScore", incScore);
        editor.putBoolean("bonusInitialized", bonusInitialized);
        editor.putBoolean("finalScoreSet", finalScoreSet);
        editor.putBoolean("planet1Phase", planet1Phase);
        editor.putBoolean("planet2Phase", planet2Phase);
        editor.putBoolean("planet3Phase", planet3Phase);
        editor.putBoolean("overheat", overheat);
        editor.putBoolean("playerMapPressed", playerMapPressed);
        editor.putBoolean("rapidAvailable", rapidAvailable);
        editor.putBoolean("justFired", justFired);
        editor.putBoolean("soundOn", soundOn);
        editor.putBoolean("crit", crit);
        editor.putBoolean("timer3Started", timer3Started);
        editor.putBoolean("isItemArrayLargeItem", isItemArrayLargeItem);

        editor.putInt("acc", acc);
        editor.putInt("largeW", largeW);
        editor.putInt("largeH", largeH);
        editor.putInt("aCurr", aCurr);
        editor.putInt("sCurr", sCurr);
        editor.putInt("timer", timer);
        editor.putInt("myX", myX);
        editor.putInt("myY", myY);
        editor.putInt("maxItems", maxItems);
        editor.putInt("ItemCount", itemCount);
        editor.putInt("bonusSpeed", bonusSpeed);
        editor.putInt("ItemH", itemH);
        editor.putInt("ItemW", itemW);
        editor.putInt("displayBonusTextTimer", displayBonusTextTimer);
        editor.putInt("textSize1", textSize1);
        editor.putInt("jx1", jx1);
        editor.putInt("jx2", jx2);
        editor.putInt("jy1", jy1);
        editor.putInt("jy2", jy2);
        editor.putInt("roundChain", roundChain);
        editor.putInt("starTrail", starTrail);
        editor.putInt("wallbX", wallBX);
        editor.putInt("wallbY", wallBY);
        editor.putInt("wallX", wallX);
        editor.putInt("wallY", wallY);
        editor.putInt("FireW", fireW);
        editor.putInt("rapidH", rapidH);
        editor.putInt("noteHeight", noteHeight);
        editor.putInt("noteWidth", noteWidth);
        editor.putInt("incSpeed", incSpeed);
        editor.putInt("maxFires", maxFire);
        editor.putInt("currChain", currChain);
        editor.putInt("playerMapW", playerW);
        editor.putInt("playerMapH", playerH);
        editor.putInt("sMax", sMax);
        editor.putInt("aMax", aMax);
        editor.putInt("speed", speed);
        editor.putInt("i", i);
        editor.putInt("newX", newX);
        editor.putInt("newY", newY);
        editor.putInt("rapidCount", rapidCount);
        editor.putInt("Itemx1", itemX1);
        editor.putInt("Itemx2", itemX2);
        editor.putInt("Itemy1", itemY1);
        editor.putInt("Itemy2", itemY2);
        editor.putInt("temp1", temp1);
        editor.putInt("temp2", temp2);
        editor.putInt("tempscore", tempScore);
        editor.putInt("maxChain", maxChain);
        editor.putInt("critBonus", critBonus);
        editor.putInt("chainBonus", chainBonus);
        editor.putInt("Firex1", fireX1);
        editor.putInt("Firex2", fireX2);
        editor.putInt("FireW", fireW);
        editor.putInt("ItemW", itemW);
        editor.putInt("ItemH", itemH);
        editor.putInt("px1", px1);
        editor.putInt("px2", px2);
        editor.putInt("py1", py1);
        editor.putInt("py2", py2);
        editor.putInt("bossNumber", bossNumber);
        editor.putInt("numStars", numStars);
        editor.putInt("startSpeedOffset", startSpeedOffset);
        editor.putInt("speedOffset", speedOffset);
        editor.putInt("currSpeedOffset", currSpeedOffset);
        editor.putInt("FireSpeed", fireSpeed);
        editor.putInt("textSize1", textSize1);
        editor.putInt("textSize2", textSize2);
        editor.putInt("textSize3", textSize3);
        editor.putInt("score", score);
        editor.putInt("finalScore", finalScore);

        editor.putFloat("volume", volume);
        editor.putFloat("prevVolume", prevVolume);
        editor.putFloat("currVolume", currVolume);

        editor.putLong("tempTimer2", tempTimer2);
        editor.putLong("tempTimer3", tempTimer3);
        editor.putLong("displayScoreTimer", displayScoreTimer);
        editor.putLong("chainTextTimer", chainTextTimer);

        editor.putString("text", text);
        editor.putString("chainText", chainText);
        editor.putString("rapidS", rapidS);
        editor.putString("FireString", fireString);
        editor.putString("textString1", textString1);
        editor.putString("textString2", textString2);
        editor.putString("textString3", textString3);
        editor.putString("textString4", textString4);
        editor.putString("textString5", textString5);
        editor.putString("textString6", textString6);
        editor.putString("incomingLargeItemText", incomingLargeItemText);
        editor.putString("doughString", shieldString);
        editor.putString("chainString", chainString);
        editor.putString("finalScoreText", finalScoreText);

        numberOfItemsInItemArray = itemArray.length;
        editor.putInt("numberOfItemsInItemArray", numberOfItemsInItemArray);
        for (int i = 0; i < numberOfItemsInItemArray; i++) {
            editor.putInt("ItemArraylargex" + i, itemArray[i].largex);
            editor.putInt("ItemArraylargey" + i, itemArray[i].largey);
            editor.putInt("ItemArrayx" + i, itemArray[i].x);
            editor.putInt("ItemArrayy" + i, itemArray[i].y);
            editor.putInt("ItemArrayItemSpeed" + i, itemArray[i].itemSpeed);
            editor.putBoolean("ItemArrayexists" + i, itemArray[i].exists);
            editor.putInt("ItemArraymovementType" + i, itemArray[i].movementType);
            editor.putInt("ItemArraycenter" + i, itemArray[i].center);
            editor.putBoolean("ItemArraymoveRight" + i, itemArray[i].moveRight);
        }

        numberOfStars = lightArray.length;
        editor.putInt("numberOfStars", numberOfStars);
        for (int i = 0; i < numberOfStars; i++) {
            editor.putInt("starArraystarW" + i, lightArray[i].starW);
            editor.putInt("starArraystarH" + i, lightArray[i].starH);
            editor.putInt("starArraystarX" + i, lightArray[i].starX);
            editor.putInt("starArraystarY" + i, lightArray[i].starY);
            editor.putInt("starArraystarY" + i, lightArray[i].starY);
            editor.putInt("starArrayspeed" + i, lightArray[i].speed);
            editor.putBoolean("starArrayexists" + i, lightArray[i].exists);
        }

        //init startX int array
        numberOfStarsX = starsX.length;
        editor.putInt("numberOfStarsX", numberOfStarsX);
        for (int i = 0; i < numberOfStarsX; i++) {
            editor.putInt("starsX" + i, starsX[i]);
        }

        //init startY int array
        numberOfStarsY = starsY.length;
        editor.putInt("numberOfStarsY", numberOfStarsY);
        for (int i = 0; i < numberOfStarsY; i++) {
            editor.putInt("starsY" + i, starsY[i]);
        }

        //new Fire array is also complex
        numberOfFire = bulletArray.length;
        editor.putInt("numberOfFire", numberOfFire);
        for (int i = 0; i < numberOfFire; i++) {
            editor.putInt("FireArrayx" + i, bulletArray[i].x);
            editor.putInt("FireArrayy" + i, bulletArray[i].y);
            editor.putInt("FireArrayFireSpeed" + i, bulletArray[i].fireSpeed);
            editor.putInt("FireArraycenter" + i, bulletArray[i].center);
            editor.putBoolean("FireArrayexists" + i, bulletArray[i].exists);
        }

        largeItemPieces = largeItem.pieces.length;
        editor.putInt("largeItemPieces", largeItemPieces);
        for (int i = 0; i < largeItemPieces; i++) {
            editor.putInt("largeItempieceslargex" + i, largeItem.pieces[i].largex);
            editor.putInt("largeItempieceslargey" + i, largeItem.pieces[i].largey);
            editor.putInt("largeItempiecesArrayx" + i, largeItem.pieces[i].x);
            editor.putInt("largeItempiecesArrayy" + i, largeItem.pieces[i].y);
            editor.putInt("largeItempiecesArrayItemSpeed" + i, largeItem.pieces[i].itemSpeed);
            editor.putBoolean("largeItempiecesArrayexists" + i, largeItem.pieces[i].exists);
            editor.putInt("largeItempiecesArraymovementType" + i, largeItem.pieces[i].movementType);
            editor.putInt("largeItempiecesArraycenter" + i, largeItem.pieces[i].center);
            editor.putBoolean("largeItempiecesArraymoveRight" + i, largeItem.pieces[i].moveRight);
        }

        numberOfAllBooms = allBooms2.length;
        editor.putInt("numberOfAllBooms", numberOfAllBooms);
        for (int i = 0; i < numberOfAllBooms; i++) {
            editor.putInt("allBooms2x" + i, allBooms2[i].x);
            editor.putInt("allBooms2y" + i, allBooms2[i].y);
            editor.putInt("allBooms2frames" + i, allBooms2[i].frames);
            editor.putInt("allBooms2currentFrame" + i, allBooms2[i].currentFrame);
            editor.putInt("allBooms2currentfps" + i, allBooms2[i].fps);
            editor.putInt("allBooms2currentcounter" + i, allBooms2[i].counter);
            editor.putBoolean("allBooms2booming" + i, allBooms2[i].booming);
            editor.putBoolean("allBooms2crit" + i, allBooms2[i].crit);
            editor.putInt("allBooms2critx" + i, allBooms2[i].critx);
            editor.putInt("allBooms2crity" + i, allBooms2[i].crity);
            editor.putString("allBooms2c" + i, allBooms2[i].c);
        }

        //shields are complex
        numberOfShields = effects.length;
        editor.putInt("numberOfShields", numberOfShields);
        for (int i = 0; i < numberOfShields; i++) {
            editor.putInt("shieldsjx1" + i, effects[i].jx1);
            editor.putInt("shieldsjy1" + i, effects[i].jy1);
            editor.putInt("shieldsjx2" + i, effects[i].jx2);
            editor.putInt("shieldsjy2" + i, effects[i].jy2);
            editor.putInt("shieldsshieldCount" + i, effects[i].shieldCount);
            editor.putBoolean("shieldsexists" + i, effects[i].exists);
        }

        // Commit the edits!
        editor.apply();
    }

    public void onDestroy() {
        if (soundPool != null) releaseSoundPool();
    }
}