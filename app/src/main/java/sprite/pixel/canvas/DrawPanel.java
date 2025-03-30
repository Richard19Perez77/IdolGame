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
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.Random;

import sprite.pixel.canvas.movables.Boom;
import sprite.pixel.canvas.movables.LargeItem;
import sprite.pixel.canvas.movables.Bullet;
import sprite.pixel.canvas.movables.Item;
import sprite.pixel.canvas.movables.Effect;
import sprite.pixel.canvas.movables.Light;


@SuppressLint("ViewConstructor")
public class DrawPanel extends SurfaceView implements SurfaceHolder.Callback,
        MediaPlayer.OnPreparedListener {

    private boolean isLogging = false;
    private final String TAG = "tagLog";

    public final int BOSS_SCORE = 100000;
    public final int WIN_SCORE = 90000;
    public final int SPEED_MARKER = 30000;

    public final int SLOW = 800;
    public final int XFAST = 3200;

    public final int PIECES = 49;
    public final int ROW = 7;

    public final int MAX_BOOMS = 60;

    public final String path = "android.resource://sprite.pixel.canvas/";

    public int startSpeedOffset;
    public int currSpeedOffset;
    public int incSpeed;
    public int speedOffset;

    public static final int STATE_RUNNING = 4;
    static final int HIGH_SCORE = 1;

    // panel variables
    public boolean rapidAvailable = true, finalScoreSet, crit,
            showChainText, introScreenPlaying, gamePlaying,
            ready, gameTouchReady, bonusPlaying, bonusInitialized, gameOver,
            mp2gameplayplaying, mp3overplaying, mp4winplaying, threadAlive,
            planet1Phase, planet2Phase, planet3Phase, overheat, retry,
            showFinalScore, playerMapPressed, soundOn = true, allVarsLoaded,
            timer3Started, loadingPrepared, showHighScoreScreen,
            loadingScreen = true, incScore = true, incChain = true,
            createSmallItems = true, justFired;

    public int shieldReserve = 10, chainBonus, randPlanetType, tempscore, px1, px2, py1,
            py2, itemx1, itemy1, temp1, temp2, firex1, firex2, firey1, firey2, wallX,
            wallY, wallbX, wallbY, myX, myY, newX, newY, starTrail, acc, playerW,
            playerH, i, sCurr, sMax, aCurr, aMax, jx1, jx2, jy1, jy2, rapidH,
            noteHeight, noteWidth, itemx2, itemy2, fireW, fireH, starsX[], starsY[],
            speed = 1, critBonus, rapidCount = 200, bossNumber, roundChain,
            currChain, maxChain, timer, itemCount, maxItems, maxFire,
            itemH, itemW, screenH, fireSpeed, screenW, textSize1, textSize2,
            textSize3, displayBonusTextTimer, largeW, largeH, numStars, score, finalScore;

    public long chainTextTimer, tempTimer2, tempTimer3, displayScoreTimer;

    public AudioManager mgr;

    // replace boom map
    public Bitmap b, planeta, planetb, itemSkin, fireSkin;
    public Bitmap[] booms;
    public Boom[] allBooms2;
    public Bitmap rapidG, rapidR, rapidW, playerMap, loadingIntro, warningIntro,
            noteR, noteG, noteW, noteRoff, noteWoff, noteGoff;

    // large item variables
    public float measure, fSpeed, streamVolumeCurrent,
            streamVolumeMax, volume, prevVolume = 1, currVolume = 1;

    public String finalScoreText, chainText, textString1, textString2,
            textString3, textString4, textString5, textString6, scoreString,
            fireString, shieldString, chainString, rapidS, text,
            incomingLargeItemText;

    public final int AMMO = 1;
    public final int EXPLODE = 2;
    public final int EFFECT = 3;

    public Intent intent;

    public LargeItem largeItem;

    public MediaPlayer mp1;

    public Bullet[] bulletArray;

    public Paint textColor = new Paint(), backGround = new Paint(),
            loadingBG = new Paint(), scoreText = new Paint(),
            fireText = new Paint(), loadingText = new Paint(),
            shieldText = new Paint(), firePaint = new Paint(),
            shieldPaint = new Paint(), randPaint = new Paint(), starsC[],
            timerPaint = new Paint();

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

    private String PREFS_NAME = "myPreferences";

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

    private HandlerThread soundThread;
    private Handler soundHandler;

    public DrawPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        getHolder().addCallback(this);
        myThread1 = new DrawThread(getHolder(), this);
        setFocusable(true);
        this.activity = (Activity) context;

        soundThread = new HandlerThread("SoundThread");
        soundThread.start();
        soundHandler = new Handler(soundThread.getLooper());
    }

    public void onCreate() {
        if (isLogging)
            Log.d(TAG, "onCreate Panel");

        if (isLogging)
            Log.d(TAG, "getting Shared Prefs");

        SharedPreferences sharedpreferences;
        sharedpreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sharedPreferencesValid = false;

        //get all boolean shared preferences
        getBooleanSharedPreferences(sharedpreferences);

        if (isLogging)
            Log.d(TAG, "getBooleanSharedPreferences valid " + sharedPreferencesValid);

        //get all int shared preferences
        getIntSharedPreferences(sharedpreferences);

        if (isLogging)
            Log.d(TAG, "getIntSharedPreferences valid " + sharedPreferencesValid);

        //get all float shared prefs
        getFloatSharedPreferences(sharedpreferences);

        if (isLogging)
            Log.d(TAG, "getFloatSharedPreferences valid " + sharedPreferencesValid);

        //get all long shared prefs
        getLongSharedPreferences(sharedpreferences);

        if (isLogging)
            Log.d(TAG, "getLongSharedPreferences valid " + sharedPreferencesValid);

        //get all string shared prefs
        getStringSharedPreferences(sharedpreferences);

        if (isLogging)
            Log.d(TAG, "getStringSharedPreferences valid " + sharedPreferencesValid);

        //get item array
        getItemArraySharedPreferences(sharedpreferences);

        if (isLogging)
            Log.d(TAG, "getIdolArraySharedPreferences valid " + sharedPreferencesValid);

        //get starArray
        getStarArray(sharedpreferences);

        if (isLogging)
            Log.d(TAG, "getStarArray valid " + sharedPreferencesValid);

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

        if (isLogging)
            Log.d(TAG, "getFireArraySharedPreferences valid " + sharedPreferencesValid);

        //getLargeIdolArray from shared prefs
        getLargeItemSharedPreferences(sharedpreferences);

        if (isLogging)
            Log.d(TAG, "getLargeIdolSharedPreferences valid " + sharedPreferencesValid);

        //get boom array from shared preferences
        getBoomsSharedPreferences(sharedpreferences);

        if (isLogging)
            Log.d(TAG, "getBoomsSharedPreferences valid " + sharedPreferencesValid);

        //get shields from shared preferences
        getShieldsSharedPreferences(sharedpreferences);

        if (isLogging)
            Log.d(TAG, "getShieldsSharedPreferences valid " + sharedPreferencesValid);

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
            //draw nothing? how about red? Its settled!
            resetGameVars();
            canvas.drawColor(Color.RED);
        }
    }

    public void loadingScreen(Canvas canvas) {
        if (!loadingPrepared)
            initLoading();

        acc++;

        textString1 = "Loading... " + (acc * 100) / 450 + "%";

        if (acc < 300)
            canvas.drawBitmap(warningIntro, 1, 1, null);
        else {
            canvas.drawBitmap(loadingIntro, 1, 1, null);
            canvas.drawText(textString1, (getWidth()) / 2, screenH - screenH
                    / 5, loadingText);
            canvas.drawText(textString3, (getWidth()) / 2, screenH / 10,
                    loadingText);
        }

        threadAlive = loadingThread.isAlive();

        if (acc >= 450 && allVarsLoaded && !threadAlive) {
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
            loadingIntro = BitmapFactory.decodeResource(getResources(),
                    R.drawable.loadingintrolarge);
            loadingIntro = getResizedBitmap(loadingIntro, screenH, screenW);
        } else {
            loadingIntro = BitmapFactory.decodeResource(getResources(),
                    R.drawable.loadingintro);
            loadingIntro = getResizedBitmap(loadingIntro, screenH, screenW);
        }

        loadingIntro = BitmapFactory.decodeResource(getResources(),
                R.drawable.loadingintro);
        loadingIntro = getResizedBitmap(loadingIntro, screenH, screenW);

        warningIntro = BitmapFactory.decodeResource(getResources(),
                R.drawable.warningintro);
        warningIntro = getResizedBitmap(warningIntro, screenH, screenW);

        textString3 = String.format(getResources().getString(R.string.light));

        mpInit(getContext());
        initSoundPool();

        planeta = BitmapFactory.decodeResource(getResources(),
                R.drawable.planet1);
        planetb = BitmapFactory.decodeResource(getResources(),
                R.drawable.planet2b);
        planet1Phase = true;

        for (int i = 0; i < lightArray.length; i++) {
            randPlanetType = rand.nextInt(5);
            getPlanetTypeForStar(lightArray[i], randPlanetType);
        }

        timerPaint.setStrokeWidth(1);
        timerPaint.setColor(getResources().getColor(R.color.SlateBlue));

        timerPaint.setTextSize(textSize2);

        itemSkin = BitmapFactory.decodeResource(getResources(),
                R.drawable.item1);

        fireSkin = BitmapFactory
                .decodeResource(getResources(), R.drawable.bullet);

        rapidG = BitmapFactory
                .decodeResource(getResources(), R.drawable.rapidg);
        rapidR = BitmapFactory
                .decodeResource(getResources(), R.drawable.rapidr);
        rapidW = BitmapFactory
                .decodeResource(getResources(), R.drawable.rapidw);

        noteR = BitmapFactory.decodeResource(getResources(), R.drawable.noter);
        noteG = BitmapFactory.decodeResource(getResources(), R.drawable.noteg);
        noteW = BitmapFactory.decodeResource(getResources(), R.drawable.notew);
        noteRoff = BitmapFactory.decodeResource(getResources(),
                R.drawable.noteroff);
        noteGoff = BitmapFactory.decodeResource(getResources(),
                R.drawable.notegoff);
        noteWoff = BitmapFactory.decodeResource(getResources(),
                R.drawable.notewoff);

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
        scoreText.setColor(getResources().getColor(R.color.MediumVioletRed));
        scoreText.setTextSize(textSize2);

        textColor.setStrokeWidth(1);
        textColor.setColor(getResources().getColor(R.color.MediumVioletRed));
        textColor.setTextSize(textSize1);

        randPaint.setStrokeWidth(1);
        randPaint.setColor(getResources().getColor(R.color.Red));
        randPaint.setTextSize(textSize2);

        backGround.setStyle(Paint.Style.FILL);
        backGround.setColor(Color.BLACK);

        loadingBG.setStyle(Paint.Style.FILL);
        loadingBG.setColor(getResources().getColor(R.color.SlateBlue));

        firePaint.setColor(getResources().getColor(R.color.Red));
        firePaint.setStyle(Paint.Style.FILL);

        shieldPaint.setColor(getResources().getColor(R.color.SlateBlue));
        shieldPaint.setStyle(Paint.Style.FILL);

        fireText.setStrokeWidth(1);
        fireText.setColor(getResources().getColor(R.color.Red));
        fireText.setTextSize(textSize2);

        loadingText.setStrokeWidth(1);
        loadingText.setColor(Color.RED);
        loadingText.setTextSize(textSize2);
        loadingText.setAntiAlias(true);
        loadingText.setFilterBitmap(true);
        loadingText.setDither(true);
        loadingText.setTextAlign(Align.CENTER);

        shieldText.setStrokeWidth(1);
        shieldText.setColor(getResources().getColor(R.color.SlateBlue));
        shieldText.setTextSize(textSize2);

        allVarsLoaded = true;
    }

    public void initLoading() {
        screenH = getHeight();
        screenW = getWidth();
        setScreenVarsSizes();

        if (screenH > 1000 && screenW > 600) {
            loadingIntro = BitmapFactory.decodeResource(getResources(),
                    R.drawable.loadingintrolarge);
            loadingIntro = getResizedBitmap(loadingIntro, screenH, screenW);
        } else {
            loadingIntro = BitmapFactory.decodeResource(getResources(),
                    R.drawable.loadingintro);
            loadingIntro = getResizedBitmap(loadingIntro, screenH, screenW);
        }

        loadingIntro = BitmapFactory.decodeResource(getResources(),
                R.drawable.loadingintro);
        loadingIntro = getResizedBitmap(loadingIntro, screenH, screenW);

        warningIntro = BitmapFactory.decodeResource(getResources(),
                R.drawable.warningintro);
        warningIntro = getResizedBitmap(warningIntro, screenH, screenW);

        textString3 = String.format(getResources().getString(R.string.light));

        loadingThread = new Thread() {
            @Override
            public void run() {
                mpInit(getContext());
                initSoundPool();

                initGameVars();
                allVarsLoaded = true;

            }
        };
        loadingThread.start();
        loadingPrepared = true;
    }

    public void introScreen(Canvas canvas) {
        acc++;
        if (acc > 50)
            gameTouchReady = true;

        initStars();
        canvas.drawPaint(backGround);
        drawStars(canvas);
        printIntro(canvas);
    }

    private void printIntro(Canvas canvas) {
        textString3 = "Title Text 1";

        textString4 = "Title Text 2";
        textString1 = "Title Text 3";

        textString5 = "Title Text 4";
        textString6 = "Title Text 5";

        textColor.setTextSize(textSize1);
        textColor.setColor(getResources().getColor(R.color.DarkBlue));
        measure = textColor.measureText(textString3);
        canvas.drawText(textString3, (screenW - measure) / 2, screenH / 5,
                textColor);

        randPaint.setTextSize(textSize1 * 2);
        setRandomTextColor(randPaint);
        measure = randPaint.measureText(textString4);
        canvas.drawText(textString4, (screenW - measure) / 2, screenH / 2
                - randPaint.getTextSize(), randPaint);

        randPaint.setTextSize(textSize1 * 2);
        setRandomTextColor(randPaint);
        measure = randPaint.measureText(textString1);
        canvas.drawText(textString1, (screenW - measure) / 2, screenH / 2,
                randPaint);

        textColor.setTextSize(textSize3);
        textColor.setColor(getResources().getColor(R.color.MistyRose));
        measure = textColor.measureText(textString5);
        canvas.drawText(textString5, (screenW - measure) / 2, screenH
                - (screenH / 3), textColor);
        measure = textColor.measureText(textString6);
        canvas.drawText(textString6, (screenW - measure) / 2, screenH
                - (screenH / 4), textColor);
        canvas.drawBitmap(playerMap, (screenW - playerW) / 2, screenH - playerH, null);
    }

    public void gamePlaying(Canvas canvas) {
        // check for shields to be empty
        if (sCurr > 0) {
            // play non bonus game music
            if (!mp4winplaying && !mp2gameplayplaying)
                startGamePlayMusic();

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
                    text = "Game Text";
                } else if (timer >= 100) {
                    text = "Game Text";
                } else {
                    text = "Game Text";
                    myY = screenH - playerH;
                    myX = (screenW - playerW) / 2;
                }
                timer += 1;
                setRandomTextColor(textColor);
                textColor.setTextSize(textSize1);
                measure = textColor.measureText(text);
                canvas.drawText(text, (screenW - measure) / 2, screenH / 2,
                        textColor);
            }

            updateGame(canvas);

            if (checkForEndOfGame()) {
                createSmallItems = false;
                incScore = false;
                incChain = false;
                showFinalScore = true;
                aCurr = 0;
                destroyAllItems(canvas);
                displayScoreTimer = 1;
            }
        } else {
            endGamePlaying();
        }
    }

    public boolean checkForEndOfGame() {
        if (sCurr <= 0 && incScore)
            return true;
        else
            return false;
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
            c.drawText(text, screenW / 2 - measure / 2, screenH / 2, textColor);
        }

        if (incScore) {
            timer += 1;
            score += 10;
        }

        updateGame(c);
        checkScoreBonus(c);

        if (sCurr <= 0 && incScore) {
            initEndOfBonus(c);
        }

        setRandomTextColor(scoreText);
    }

    public void initEndOfBonus(Canvas c) {
        bonusPlaying = false;
        createSmallItems = false;
        incScore = false;
        incChain = false;
        showFinalScore = true;
        displayScoreTimer = 1;
        aCurr = 0;
        destroyAllItems(c);
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
        checkForHits(canvas);
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
            createStarfield(canvas);
            showFinalScoreText(canvas);
            printScoreTexts(canvas);
            updateBooms(canvas);
            displayScoreTimer++;
        }
    }

    public void drawBackground(Canvas c) {
        c.drawPaint(backGround);
        createStarfield(c);
        createAsteroidfield(c);
        createPlanets(c);
    }

    public void gameOver(Canvas canvas) {
        canvas.drawPaint(backGround);
        initStars();
        drawStars(canvas);
        drawPlanets(canvas);
        drawThankYou(canvas);
    }

    private void drawThankYou(Canvas canvas) {
        textString3 = "Title Text 1";
        textString4 = "Title Text 2";

        textString1 = "Title Text 3";
        textString2 = "Title Text 4";

        textString5 = "Title Text 5";
        textString6 = "Title Text 6";

        textColor.setTextSize(textSize1);
        textColor.setColor(getResources().getColor(R.color.MediumVioletRed));
        measure = textColor.measureText(textString3);
        canvas.drawText(textString3, (screenW - measure) / 2, screenH / 5,
                textColor);

        randPaint.setTextSize(textSize2);
        setRandomTextColor(randPaint);
        measure = randPaint.measureText(textString4);
        canvas.drawText(textString4, (screenW - measure) / 2, screenH / 3,
                randPaint);

        textColor.setTextSize(textSize3);
        textColor.setColor(getResources().getColor(R.color.orange));
        measure = textColor.measureText(textString1);
        canvas.drawText(textString1, (screenW - measure) / 2, screenH / 2,
                textColor);
        measure = textColor.measureText(textString2);
        canvas.drawText(textString2, (screenW - measure) / 2, screenH / 2
                + (textColor.getTextSize() * 2), textColor);

        textColor.setTextSize(textSize3);
        textColor.setColor(getResources().getColor(R.color.SlateBlue));
        measure = textColor.measureText(textString5);
        canvas.drawText(textString5, (screenW - measure) / 2, screenH
                - (screenH / 3), textColor);
        measure = textColor.measureText(textString6);
        canvas.drawText(textString6, (screenW - measure) / 2, screenH
                - (screenH / 4), textColor);

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
        } catch (IllegalStateException | IllegalArgumentException | IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    public void initSoundPool() {
        fSpeed = 1.0f;
        soundPool = new SoundPool(40, AudioManager.STREAM_MUSIC, 100);
        soundsMap = new SparseIntArray();
        soundsMap.put(AMMO,
                soundPool.load(getContext(), R.raw.mozzarella, 1));
        soundsMap
                .put(EXPLODE, soundPool.load(getContext(), R.raw.explosion, 1));
        soundsMap.put(EFFECT, soundPool.load(getContext(), R.raw.shield, 1));

        mgr = (AudioManager) this.getContext().getSystemService(
                Context.AUDIO_SERVICE);
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
            // add canvas var's for x and y
            lightArray[i].starX = screenW;
            lightArray[i].starY = rand.nextInt(screenH);

        }

        planeta = BitmapFactory.decodeResource(getResources(),
                R.drawable.planet1);
        planetb = BitmapFactory.decodeResource(getResources(),
                R.drawable.planet2b);
        planet1Phase = true;

        wallbX = screenW;
        wallbY = 50;
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
        timerPaint.setColor(getResources().getColor(R.color.SlateBlue));

        timerPaint.setTextSize(textSize2);

        itemSkin = BitmapFactory.decodeResource(getResources(),
                R.drawable.item1);
        itemH = itemSkin.getHeight();
        itemW = itemSkin.getWidth();

        fireSkin = BitmapFactory
                .decodeResource(getResources(), R.drawable.bullet);
        fireW = fireSkin.getWidth();
        fireH = fireSkin.getHeight();

        rapidG = BitmapFactory
                .decodeResource(getResources(), R.drawable.rapidg);
        rapidR = BitmapFactory
                .decodeResource(getResources(), R.drawable.rapidr);
        rapidW = BitmapFactory
                .decodeResource(getResources(), R.drawable.rapidw);
        rapidH = rapidG.getHeight();

        noteR = BitmapFactory.decodeResource(getResources(), R.drawable.noter);
        noteG = BitmapFactory.decodeResource(getResources(), R.drawable.noteg);
        noteW = BitmapFactory.decodeResource(getResources(), R.drawable.notew);
        noteRoff = BitmapFactory.decodeResource(getResources(),
                R.drawable.noteroff);
        noteGoff = BitmapFactory.decodeResource(getResources(),
                R.drawable.notegoff);
        noteWoff = BitmapFactory.decodeResource(getResources(),
                R.drawable.notewoff);
        noteHeight = noteR.getHeight();
        noteWidth = noteR.getWidth();

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
        scoreText.setColor(getResources().getColor(R.color.MediumVioletRed));
        scoreText.setTextSize(textSize2);

        textColor.setStrokeWidth(1);
        textColor.setColor(getResources().getColor(R.color.MediumVioletRed));
        textColor.setTextSize(textSize1);

        randPaint.setStrokeWidth(1);
        randPaint.setColor(getResources().getColor(R.color.Red));
        randPaint.setTextSize(textSize2);

        backGround.setStyle(Paint.Style.FILL);
        backGround.setColor(Color.BLACK);

        loadingBG.setStyle(Paint.Style.FILL);
        loadingBG.setColor(getResources().getColor(R.color.SlateBlue));

        firePaint.setColor(getResources().getColor(R.color.Red));
        firePaint.setStyle(Paint.Style.FILL);

        shieldPaint.setColor(getResources().getColor(R.color.SlateBlue));
        shieldPaint.setStyle(Paint.Style.FILL);

        fireString = "Ammo";
        fireText.setStrokeWidth(1);
        fireText.setColor(getResources().getColor(R.color.Red));
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
        shieldText.setColor(getResources().getColor(R.color.SlateBlue));
        shieldText.setTextSize(textSize2);

        speed = 1;
        score = 0;

        for (int i = 0; i < effects.length; i++) {
            effects[i] = new Effect();
        }
    }

    private boolean divideItemIntoLargeItems() {
        largeW = screenW - screenW / 10;

        Bitmap lItem = BitmapFactory.decodeResource(getResources(),
                R.drawable.largeskin);
        Bitmap largeSkin = Bitmap.createScaledBitmap(lItem, largeW, largeW,
                false);

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

                largeItem.pieces[i].piece = Bitmap.createBitmap(largeSkin, x,
                        y, bitmapWd3, bitmapHd3);

                if (largeItem.pieces[i].piece == null) {
                    return false;
                }

                largeItem.pieces[i].largex = largeItem.pieces[i].x = x
                        + xOffset;

                largeItem.pieces[i].largey = largeItem.pieces[i].y = y
                        - (bitmapHd3 * ROW);

            }
            imageSplit = true;
        }
        largeSkin.recycle();

        if (isItemArrayLargeItem) {
            itemArray = largeItem.pieces;
        }

        return true;
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
                newLight.star = BitmapFactory.decodeResource(getResources(),
                        R.drawable.smplanet1);
                newLight.starW = newLight.star.getWidth();
                newLight.starH = newLight.star.getHeight();
                break;
            case 1:
                newLight.star = BitmapFactory.decodeResource(getResources(),
                        R.drawable.smplanet2);
                newLight.starW = newLight.star.getWidth();
                newLight.starH = newLight.star.getHeight();
                break;
            case 2:
                newLight.star = BitmapFactory.decodeResource(getResources(),
                        R.drawable.smplanet3);
                newLight.starW = newLight.star.getWidth();
                newLight.starH = newLight.star.getHeight();
                break;
            case 3:
                newLight.star = BitmapFactory.decodeResource(getResources(),
                        R.drawable.smplanet4);
                newLight.starW = newLight.star.getWidth();
                newLight.starH = newLight.star.getHeight();
                break;
            case 4:
                newLight.star = BitmapFactory.decodeResource(getResources(),
                        R.drawable.smplanet5);
                newLight.starW = newLight.star.getWidth();
                newLight.starH = newLight.star.getHeight();
                break;
        }
    }

    public void getPlayerMapLocationThenDraw(Canvas canvas) {
        // get player map location and draw him
        jx1 = myX - playerW / 2;
        jx2 = myX + (playerW / 2);
        jy1 = myY - (playerH / 2) - 20;
        jy2 = myY + (playerH / 2) - 20;

        canvas.drawBitmap(playerMap, jx1, jy1, null);
    }

    public void printScoreTexts(Canvas canvas) {
        // print score and chain
        scoreText.setTextSize(textSize2);
        chainString = "Chain/Max " + currChain + "/" + maxChain;
        canvas.drawText(chainString, 0, scoreText.getTextSize(), scoreText);

        scoreString = "Score " + score;
        measure = scoreText.measureText(scoreString);
        canvas.drawText(scoreString, screenW - measure,
                scoreText.getTextSize(), scoreText);

        // draw the ammo and shields count over player map, so its drawn last
        canvas.drawText(fireString, 10, screenH - fireText.getTextSize() * 2,
                fireText);

        canvas.drawText(shieldString, 10, screenH - shieldText.getTextSize(),
                shieldText);

        float sPercent = (float) sCurr / (float) sMax;
        float aPercent = (float) aCurr / (float) aMax;
        float shields = (screenW / 2) * sPercent;
        float ammo = (screenW / 2) * aPercent;
        int sh = (int) (screenW / 2 + shields);
        int am = (int) (screenW / 2 + ammo);

        if (sCurr >= 0) {
            canvas.drawRect(screenW / 2, screenH - shieldText.getTextSize() * 1,
                    sh, screenH - shieldText.getTextSize() * 2, shieldPaint);
        }

        if (aCurr >= 0) {
            if (!overheat)
                canvas.drawRect(screenW / 2, screenH - fireText.getTextSize()
                                * 2, am, screenH - fireText.getTextSize() * 3,
                        firePaint);
            else {
                setRandomTextColor(randPaint);
                canvas.drawRect(screenW / 2, screenH - fireText.getTextSize()
                                * 2, am, screenH - fireText.getTextSize() * 3,
                        randPaint);
                if (aCurr > aMax / 2)
                    overheat = false;

            }
        }
    }

    public void startEndGameMusic() {
        if (!mp3overplaying) {
            if (mp2gameplayplaying) {
                mp1.stop();
                mp2gameplayplaying = false;
            }
            if (mp4winplaying) {
                mp1.stop();
                mp4winplaying = false;
            }
            try {
                mp1.reset();
                Uri uri = Uri.parse(path + R.raw.replay);
                mp1.setDataSource(getContext(), uri);
                mp1.setLooping(true);
                mp1.setVolume(currVolume, currVolume);
                mp1.setOnPreparedListener(this);
                mp1.prepareAsync();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mp3overplaying = true;
        }
    }

    public Paint newColor() {
        // randomly choose between red green and white
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
                    } else if ((gamePlaying || bonusPlaying || showFinalScore)
                            && ready) {
                        // game is playing start actions
                        newX = (int) event.getX();
                        newY = (int) event.getY();

                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                //check if touch down on player map
                                if (newX >= myX - playerW * 2 && newX <= myX + playerW * 2
                                        && newY >= myY - playerW * 2
                                        && newY <= myY + playerW * 2) {
                                    playerMapPressed = true;
                                    myX = newX;
                                    myY = newY;

                                    if (aCurr >= 1) {
                                        createFire(newX, newY - 50);
                                    }
                                }

                                // if tapped on sound icon change on/off
                                if (newX >= (screenW - noteWidth) && newX <= screenW
                                        && newY >= noteHeight && newY <= noteHeight * 2) {
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
                                            if (!overheat)
                                                rapidCount--;
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

    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        int w = bm.getWidth();
        int h = bm.getHeight();
        float sW = ((float) newWidth) / w;
        float sH = ((float) newHeight) / h;

        Matrix matrix = new Matrix();
        matrix.postScale(sW, sH);

        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, w, h, matrix,
                false);
        return resizedBitmap;
    }

    public void checkScoreBonus(Canvas canvas) {

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
                if (p.x > itemW && p.x < screenW - itemW)
                    p.movementType = 1;
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
            wallbY++;
        }

        if (score % 40 == 0) {
            wallX--;
        }
        if (score % 20 == 0) {
            wallbX--;
        }
    }

    public void destroyAllItems(Canvas c) {
        for (int i = 0; i < itemArray.length; i++) {
            if (itemArray[i].exists) {
                itemx1 = itemArray[i].x;
                itemy1 = itemArray[i].y;
                // replace with boom map animation
                temp1 = itemx1 - 30;
                temp2 = itemy1 - 30;
                playSound(EXPLODE, fSpeed);
                boomMap(temp1, temp2, crit);
                destroyItems(itemArray[i]);
            }
        }
    }

    public void showShields(Canvas canvas, int jx1, int jx2, int jy1, int jy2) {
        for (int i = 0; i < effects.length; i++) {
            if (effects[i].exists) {
                effects[i].update(canvas, jx1, jx2, jy1, jy2);
            }
        }
    }

    public void showFinalScoreText(Canvas canvas) {
        if (displayScoreTimer > 0) {

            setRandomTextColor(textColor);

            textColor.setTextSize(textSize1);

            tempscore = (int) score;
            finalScoreText = "GameScore";
            float measure2 = textColor.measureText(finalScoreText);
            canvas.drawText(finalScoreText, (screenW / 2) - measure2,
                    (screenH / 4) + textColor.getTextSize() * 1, textColor);
            finalScoreText = Integer.toString(tempscore);
            measure = textColor.measureText(finalScoreText);
            canvas.drawText(finalScoreText,
                    (screenW - ((screenW / 2) - measure2)) - measure,
                    (screenH / 4) + textColor.getTextSize() * 1, textColor);

            tempscore = (maxChain * 24);
            finalScoreText = "ChainBonus";
            canvas.drawText(finalScoreText, (screenW / 2) - measure2,
                    (screenH / 4) + textColor.getTextSize() * 2, textColor);
            finalScoreText = Integer.toString(tempscore);
            measure = textColor.measureText(finalScoreText);
            canvas.drawText(finalScoreText,
                    (screenW - ((screenW / 2) - measure2)) - measure,
                    (screenH / 4) + textColor.getTextSize() * 2, textColor);

            tempscore = critBonus;
            finalScoreText = "CritBonus";
            canvas.drawText(finalScoreText, (screenW / 2) - measure2,
                    (screenH / 4) + textColor.getTextSize() * 3, textColor);
            finalScoreText = Integer.toString(tempscore);
            measure = textColor.measureText(finalScoreText);
            canvas.drawText(finalScoreText,
                    (screenW - ((screenW / 2) - measure2)) - measure,
                    (screenH / 4) + textColor.getTextSize() * 3, textColor);

            tempscore = (int) (score + (maxChain * 24) + critBonus);
            finalScoreText = "Total";
            canvas.drawText(finalScoreText, (screenW / 2) - measure2,
                    (screenH / 4) + textColor.getTextSize() * 5, textColor);
            finalScoreText = Integer.toString(tempscore);
            measure = textColor.measureText(finalScoreText);
            canvas.drawText(finalScoreText,
                    (screenW - ((screenW / 2) - measure2)) - measure,
                    (screenH / 4) + textColor.getTextSize() * 5, textColor);

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
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mp2gameplayplaying = true;
    }

    public void showSoundNote(Canvas canvas) {
        if (soundOn) {
            canvas.drawBitmap(noteW, (screenW - noteWidth), rapidH, null);
//            if (scoreText.getColor() == (getResources().getColor(R.color.Red))) {
//                canvas.drawBitmap(noteR, (screenW - noteWidth), rapidH, null);
//            } else if (scoreText.getColor() == getResources().getColor(
//                    R.color.MediumVioletRed)) {
//                canvas.drawBitmap(noteG, (screenW - noteWidth), rapidH, null);
//            } else {
//                canvas.drawBitmap(noteW, (screenW - noteWidth), rapidH, null);
//            }
        } else {
            canvas.drawBitmap(noteWoff, (screenW - noteWidth), rapidH, null);
//            if (scoreText.getColor() == (getResources().getColor(R.color.Red))) {
//                canvas.drawBitmap(noteRoff, (screenW - noteWidth), rapidH, null);
//            } else if (scoreText.getColor() == getResources().getColor(
//                    R.color.MediumVioletRed)) {
//                canvas.drawBitmap(noteGoff, (screenW - noteWidth), rapidH, null);
//            } else {
//                canvas.drawBitmap(noteWoff, (screenW - noteWidth), rapidH, null);
//            }
        }
    }

    public void showRapidCount(Canvas canvas) {
        // deactivate for not rapid graphics on bonus stage
        if (rapidAvailable) {
            // if (rapidAvailable && !bonusPlaying) {
//            if (scoreText.getColor() == (getResources().getColor(R.color.Red))) {
//                canvas.drawBitmap(rapidR, 0, rapidH, null);
//            } else if (scoreText.getColor() == getResources().getColor(
//                    R.color.MediumVioletRed)) {
//                canvas.drawBitmap(rapidG, 0, rapidH, null);
//            } else {
//                canvas.drawBitmap(rapidW, 0, rapidH, null);
//            }

            canvas.drawBitmap(rapidW, 0, rapidH, null);
            rapidS = " " + rapidCount;
            scoreText.setTextSize(textSize2);
            canvas.drawText(rapidS, 5,
                    rapidH + rapidH + scoreText.getTextSize(), scoreText);
        }
    }

    public void showChainText(Canvas canvas) {
        if (showChainText && chainTextTimer < 100) {
            if (chainTextTimer % 3 == 0) {
                textColor.setColor(getResources().getColor(R.color.Red));
            } else if (chainTextTimer % 3 == 1) {
                textColor.setColor(getResources()
                        .getColor(R.color.MediumVioletRed));
            } else {
                textColor.setColor(getResources().getColor(R.color.SlateBlue));
            }
            textColor.setTextSize(textSize1);
            measure = textColor.measureText(chainText);
            canvas.drawText(chainText, screenW / 2 - measure / 2,
                    (screenH / 3), textColor);
            chainTextTimer++;
        }
    }

    public void comboCheck(Canvas canvas) {
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

    public void setRandomTextColor(Paint scoreText2) {
        // on combo hit change text color
        if (scoreText2.getColor() == getResources().getColor(
                R.color.MediumVioletRed)) {
            scoreText2.setColor(getResources().getColor(R.color.Red));
        } else if (scoreText2.getColor() == (getResources()
                .getColor(R.color.Red))) {
            scoreText2.setColor(getResources().getColor(R.color.SlateBlue));
        } else {
            scoreText2.setColor(getResources().getColor(R.color.MediumVioletRed));
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
            for (int i = 0; i < itemArray.length; i++) {
                if (itemArray[i].exists) {
                    itemArray[i].moveItem(incSpeed);
                    canvas.drawBitmap(itemSkin, itemArray[i].x,
                            itemArray[i].y, null);
                }
            }
        } else {
            for (int i = 0; i < PIECES; i++) {
                if (largeItem.pieces[i].exists) {
                    largeItem.pieces[i].moveItem(incSpeed);
                    canvas.drawBitmap(largeItem.pieces[i].piece,
                            largeItem.pieces[i].x, largeItem.pieces[i].y,
                            null);
                }
            }
        }
    }

    public void createNewItem() {
        if (createSmallItems) {
            if (itemCount <= itemArray.length) {
                for (int i = 0; i < itemArray.length; i++) {
                    if (itemArray[i].exists == false) {
                        itemArray[i].exists = true;
                        itemCount += 1;
                        break;
                    }
                }
            }
        }
    }

    public void checkForHits(Canvas canvas) {
        // first check for fire hitting item
        for (int i = 0; i < maxFire; i++) {
            if (bulletArray[i].exists) {
                firex1 = bulletArray[i].x;
                firex2 = firex1 + fireW;
                firey1 = bulletArray[i].y;
                firey2 = bulletArray[i].y + fireH;

                // check for fire hitting small items
                // and small items hitting player
                for (int j = 0; j < itemArray.length; j++) {
                    if (itemArray[j].exists) {
                        itemx1 = itemArray[j].x;
                        itemx2 = itemx1 + itemW;
                        itemy1 = itemArray[j].y;
                        itemy2 = itemy1 + itemH;

                        if ((firex1 >= itemx1 && firex1 <= itemx2)
                                && (firey2 >= itemy1 && firey2 <= itemy2)
                                || (firex2 >= itemx1 && firex2 <= itemx2)
                                && (firey2 >= itemy1 && firey2 <= itemy2)
                                || (firex1 >= itemx1 && firex1 <= itemx2)
                                && (firey1 >= itemy1 && firey1 <= itemy2)
                                || (firex2 >= itemx1 && firex2 <= itemx2)
                                && (firey1 >= itemy1 && firey1 <= itemy2)) {
                            if (incChain)
                                currChain++;
                            comboCheck(canvas);
                            if (currChain >= maxChain) {
                                maxChain = currChain;
                            }
                            if (currChain >= roundChain) {
                                roundChain = currChain;
                            }

                            // check for critical hit
                            if ((itemArray[j].center <= bulletArray[i].center + 2 && itemArray[j].center >= bulletArray[i].center - 2)
                                    || bonusPlaying) {
                                crit = true;
                                critBonus += 500;
                            }

                            // replace graphic with boom map animation
                            temp1 = itemx1 - 30;
                            temp2 = itemy1 - 30;
                            boomMap(temp1, temp2, crit);
                            crit = false;
                            playSound(EXPLODE, fSpeed);
                            destroyFire(bulletArray[i]);
                            destroyItems(itemArray[j]);
                        }
                    }
                }
            }
        }

        // next check for item hitting player
        for (int i = 0; i < itemArray.length; i++) {
            if (itemArray[i].exists) {
                px1 = itemArray[i].x;
                px2 = itemArray[i].x + itemW;
                py1 = itemArray[i].y;
                py2 = itemArray[i].y + itemH;

                if ((px1 >= jx1 && px1 <= jx2) && (py2 >= jy1 && py2 <= jy2)
                        || (px2 >= jx1 && px2 <= jx2)
                        && (py2 >= jy1 && py2 <= jy2)
                        || (px1 >= jx1 && px1 <= jx2)
                        && (py1 >= jy1 && py1 <= jy2)
                        || (px2 >= jx1 && px2 <= jx2)
                        && (py1 >= jy1 && py1 <= jy2)) {

                    for (int j = 0; j < effects.length; j++) {
                        if (effects[j].exists == false) {
                            effects[j].start();
                            break;
                        }
                    }

                    temp1 = px1 - 30;
                    temp2 = py1 - 30;
                    boomMap(temp1, temp2, crit);
                    playSound(EXPLODE, fSpeed);
                    destroyItems(itemArray[i]);
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

    public void destroyFire(Bullet bullet) {
        bullet.exists = false;
    }

    public void playSound(int sound, float fSpeed) {
//        // plays the sounds effect called
//        if (soundOn)
//            volume = streamVolumeCurrent / streamVolumeMax;
//        else
//            volume = 0;

        if (soundHandler == null) return;

        soundHandler.post(new Runnable() {
            @Override
            public void run() {
                float volumeToPlay = soundOn ? (streamVolumeCurrent / streamVolumeMax) : 0;
                soundPool.play(soundsMap.get(sound), volumeToPlay, volumeToPlay, 1, 0, fSpeed);
            }
        });

        // soundPool.play(soundsMap.get(sound), volume, volume, 1, 0, fSpeed);
    }

    public void destroyItems(Item item) {
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
                canvas.drawBitmap(fireSkin, bulletArray[i].x, bulletArray[i].y,
                        null);
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
                        if (aCurr == 0)
                            overheat = true;
                    } else {
                        aCurr--;
                        if (aCurr == 0)
                            overheat = true;
                    }
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
            for (int i = 0; i < itemArray.length; i++) {
                if (itemArray[i].exists)
                    if (itemArray[i].y > (screenH)) {
                        //items off bottom of screen
                        destroyItems(itemArray[i]);
                        currChain = 0;
                        if (bonusPlaying) {
                            sCurr--;
                            for (int j = 0; j < effects.length; j++) {
                                if (effects[j].exists == false) {
                                    effects[j].start();
                                    break;
                                }
                            }
                        }
                    }
                if (itemArray[i].x < 0 || itemArray[i].x + itemW > screenW) {
                    itemArray[i].changeDirection();
                }
            }
        } else {
            for (int i = 0; i < itemArray.length; i++) {
                if (itemArray[i].exists
                        && itemArray[i].y > (screenH + itemH)) {
                    itemArray[i].exists = false;
                    if (incChain)
                        currChain = 0;
                }
            }
        }
    }

    public void checkScore(Canvas c) {
        // create large item
        if (score == BOSS_SCORE) {

            destroyAllItems(c);

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
                if (numStars > 25)
                    numStars--;

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
                    if (p.x > itemW && p.x < screenW - itemW)
                        p.movementType = 1;
                }
            } else if (score % 4000 == 0) {
                for (Item p : itemArray) {
                    p.movementType = 0;
                }
            }
        }

        if (score % 50 == 0) {
            wallbY++;
        }
        if (score % 20 == 0) {
            wallbX--;
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
            if (bossNumber < 2)
                bossNumber++;
        }

        // show incoming large item alert
        if (tempTimer2 > 0 && tempTimer2 < 200) {

            if (tempTimer2 % 3 == 0) {
                textColor.setColor(getResources().getColor(R.color.Red));
            } else if (tempTimer2 % 3 == 1) {
                textColor.setColor(getResources()
                        .getColor(R.color.MediumVioletRed));
            } else {
                textColor.setColor(getResources().getColor(R.color.SlateBlue));
            }

            textColor.setTextSize(textSize1);
            measure = textColor.measureText(incomingLargeItemText);
            c.drawText(incomingLargeItemText, screenW / 2 - measure / 2,
                    (screenH / 2), textColor);
            tempTimer2++;
        }

        // end of gameplaying and start of bonus
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
            if (score % 50 == 0)
                if (aCurr < aMax)
                    aCurr += 1;
        }

        if (score > WIN_SCORE) {
            // after win theme kicks off barrage of items!
            if (score % 100 == 0) {
                createNewItem();
            }
        }

        if (score % WIN_SCORE == 0 && score != 0 && !mp4winplaying) {
            mp1.stop();
            mp2gameplayplaying = false;
            try {
                mp1.reset();
                Uri uri = Uri.parse(path + R.raw.win);
                mp1.setDataSource(getContext(), uri);
                mp1.setLooping(true);
                if (currVolume != 0) {
                    currVolume = 0.5f;
                }
                mp1.setVolume(currVolume, currVolume);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mp1.setOnPreparedListener(this);
            mp1.prepareAsync();
            mp4winplaying = true;
        }

        if (score % SPEED_MARKER == 0) {
            for (int i = 0; i < itemArray.length; i++) {
                itemArray[i].itemSpeed += incSpeed;
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
        if (timer % 30 == 0 && itemCount < maxItems && score < BOSS_SCORE
                && createSmallItems) {
            createNewItem();
        }

    }

    public void falseEndingHighScore(Canvas c) {
        if (textColor.getColor() == getResources().getColor(
                R.color.MediumVioletRed)) {
            textColor.setColor(getResources().getColor(R.color.Red));
        } else if (textColor.getColor() == (getResources()
                .getColor(R.color.Red))) {
            textColor.setColor(getResources().getColor(R.color.SlateBlue));
        } else {
            textColor.setColor(getResources().getColor(R.color.MediumVioletRed));
        }

        setRandomTextColor(textColor);
        textColor.setTextSize(textSize1);

        tempscore = (int) score;
        finalScoreText = "GameScore";
        float measure2 = textColor.measureText(finalScoreText);
        c.drawText(finalScoreText, (screenW / 2) - measure2, (screenH / 4)
                + textColor.getTextSize() * 1, textColor);
        finalScoreText = Integer.toString(tempscore);
        measure = textColor.measureText(finalScoreText);
        c.drawText(finalScoreText, (screenW - ((screenW / 2) - measure2))
                        - measure, (screenH / 4) + textColor.getTextSize() * 1,
                textColor);

        tempscore = (maxChain * 24);
        finalScoreText = "ChainBonus";
        c.drawText(finalScoreText, (screenW / 2) - measure2, (screenH / 4)
                + textColor.getTextSize() * 2, textColor);
        finalScoreText = Integer.toString(tempscore);
        measure = textColor.measureText(finalScoreText);
        c.drawText(finalScoreText, (screenW - ((screenW / 2) - measure2))
                        - measure, (screenH / 4) + textColor.getTextSize() * 2,
                textColor);

        tempscore = critBonus;
        finalScoreText = "CritBonus";
        c.drawText(finalScoreText, (screenW / 2) - measure2, (screenH / 4)
                + textColor.getTextSize() * 3, textColor);
        finalScoreText = Integer.toString(tempscore);
        measure = textColor.measureText(finalScoreText);
        c.drawText(finalScoreText, (screenW - ((screenW / 2) - measure2))
                        - measure, (screenH / 4) + textColor.getTextSize() * 3,
                textColor);

        tempscore = (int) (score + (maxChain * 24) + critBonus);
        finalScoreText = "Total";
        c.drawText(finalScoreText, (screenW / 2) - measure2, (screenH / 4)
                + textColor.getTextSize() * 5, textColor);
        finalScoreText = Integer.toString(tempscore);
        measure = textColor.measureText(finalScoreText);
        c.drawText(finalScoreText, (screenW - ((screenW / 2) - measure2))
                        - measure, (screenH / 4) + textColor.getTextSize() * 5,
                textColor);

        if (!finalScoreSet) {
            finalScore = score + (maxChain * 24) + critBonus;
            finalScoreSet = true;
        }
        tempTimer3++;

    }

    public void createStarfield(Canvas canvas) {
        drawStars(canvas);
        moveStars();
    }

    public void createAsteroidfield(Canvas canvas) {
        drawAsteroids(canvas);
        moveAsteroids();
    }

    public void drawAsteroids(Canvas canvas) {
        for (int i = 0; i < numStars; i++) {
            if (lightArray[i].exists) {
                canvas.drawBitmap(lightArray[i].star, lightArray[i].starX,
                        lightArray[i].starY, null);
            } else {
                break;
            }
        }
    }

    public void moveAsteroids() {
        for (Light light : lightArray) {
            if (light.exists) {
                if (light.starX < 0 - light.starW) {
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

    public void drawPlanets(Canvas c) {
        c.drawBitmap(planetb, wallbX, wallbY, null);
        c.drawBitmap(planeta, wallX, wallY, null);
    }

    public void movePlanets() {
        if (wallbX < -planetb.getWidth() - 75) {
            wallbX = screenW;
            wallbY = 50;
        }

        if (wallX < -planeta.getWidth() - 50) {
            if (planet1Phase) {
                planeta = BitmapFactory.decodeResource(getResources(),
                        R.drawable.planet2);
                wallX = screenW;
                wallY = 0;
                planet1Phase = false;
                planet2Phase = true;
            } else if (planet2Phase) {
                planeta = BitmapFactory.decodeResource(getResources(),
                        R.drawable.planet3);
                wallX = screenW;
                wallY = 0;
                planet2Phase = false;
                planet3Phase = true;
            } else if (planet3Phase) {
                planeta = BitmapFactory.decodeResource(getResources(),
                        R.drawable.planet1);
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
                canvas.drawRect(starsX[i], starsY[i], starsX[i] + 3, starsY[i]
                        + 3 + starTrail, starsC[i]);
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
        } else if (rate == screenH) {
            incSpeed = 1;
            speedOffset = 1;
        }
        return incSpeed;
    }

    public void setScreenVarsSizes() {
        setSpeed(SLOW);

        if (screenH >= 1000 && screenW >= 600) {
            fireSpeed = 33;
            maxItems = 75;
            maxFire = 75;
            textSize1 = 55;
            textSize2 = 40;
            textSize3 = 37;
            numStars = 50;
        } else if (screenH >= 700 && screenW >= 400) {
            fireSpeed = 28;
            maxItems = 60;
            maxFire = 50;
            textSize1 = 35;
            textSize2 = 25;
            textSize3 = 22;
            numStars = 40;
        } else if (screenH >= 400 && screenW >= 300) {
            fireSpeed = 23;
            maxItems = 25;
            maxFire = 30;
            textSize1 = 25;
            textSize2 = 20;
            textSize3 = 18;
            numStars = 30;
        } else if (screenH >= 200 && screenW >= 200) {
            fireSpeed = 18;
            maxItems = 20;
            maxFire = 25;
            textSize1 = 25;
            textSize2 = 15;
            textSize3 = 13;
            numStars = 20;
        } else {
            fireSpeed = 16;
            maxItems = 15;
            maxFire = 10;
            textSize1 = 20;
            textSize2 = 10;
            textSize3 = 9;
            numStars = 15;
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        retry = true;
        myThread1.setRunning(false);
        while (retry) {
            try {
                myThread1.join();
                retry = false;
            } catch (InterruptedException e) {
                // we will try it again and again
            }
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }

    public void surfaceCreated(SurfaceHolder holder) {
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

    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    public DrawThread getThread() {
        return myThread1;
    }

    public void drawColor(Canvas canvas) {
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
        mp2gameplayplaying = false;
        mp3overplaying = false;
        mp4winplaying = false;
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
        tempscore = 0;
        px1 = 0;
        px2 = 0;
        py1 = 0;
        py2 = 0;
        itemx1 = 0;
        itemy1 = 0;
        temp1 = 0;
        temp2 = 0;
        firex1 = 0;
        firex2 = 0;
        firey1 = 0;
        firey2 = 0;
        wallX = 0;
        wallY = 0;
        wallbX = 0;
        wallbY = 0;
        myX = 0;
        myY = 0;
        newX = 0;
        newY = 0;
        starTrail = 0;
        acc = 0;
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
        itemx2 = 0;
        itemy2 = 0;
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

        finalScoreText = chainText = textString1 = textString2 =
                textString3 = textString4 = textString5 = textString6 = scoreString =
                        fireString = shieldString = chainString = rapidS = text =
                                incomingLargeItemText = "";
    }

    private void getShieldsSharedPreferences(SharedPreferences sharedpreferences) {
        if (sharedpreferences.contains("numberOfShields")) {
            numberOfShields = sharedpreferences.getInt("numberOfShields", -1);

            effects = new Effect[numberOfShields];
            for (int i = 0; i < numberOfShields; i++) {

                effects[i] = new Effect();
                if (sharedpreferences.contains("shieldsjx1" + i)) {
                    effects[i].jx1 = sharedpreferences.getInt("shieldsjx1" + i, -1);
                    if (effects[i].jx1 != -1)
                        sharedPreferencesValid = true;
                } else {
                    sharedPreferencesValid = false;
                }

                if (sharedpreferences.contains("shieldsjy1" + i)) {
                    effects[i].jy1 = sharedpreferences.getInt("shieldsjy1" + i, -1);
                    if (effects[i].jy1 != -1)
                        sharedPreferencesValid = true;
                } else {
                    sharedPreferencesValid = false;
                }

                if (sharedpreferences.contains("shieldsjx2" + i)) {
                    effects[i].jx2 = sharedpreferences.getInt("shieldsjx2" + i, -1);
                    if (effects[i].jx2 != -1)
                        sharedPreferencesValid = true;
                } else {
                    sharedPreferencesValid = false;
                }

                if (sharedpreferences.contains("shieldsjy2" + i)) {
                    effects[i].jy2 = sharedpreferences.getInt("shieldsjy2" + i, -1);
                    if (effects[i].jy2 != -1)
                        sharedPreferencesValid = true;
                } else {
                    sharedPreferencesValid = false;
                }

                if (sharedpreferences.contains("shieldsshieldCount" + i)) {
                    effects[i].shieldCount = sharedpreferences.getInt("shieldsshieldCount" + i, -1);
                    if (effects[i].shieldCount != -1)
                        sharedPreferencesValid = true;
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

    private void getBoomsSharedPreferences(SharedPreferences sharedpreferences) {
        if (sharedpreferences.contains("numberOfAllBooms")) {
            numberOfAllBooms = sharedpreferences.getInt("numberOfAllBooms", -1);

            allBooms2 = new Boom[numberOfAllBooms];
            for (int i = 0; i < numberOfAllBooms; i++) {

                allBooms2[i] = new Boom();
                if (sharedpreferences.contains("allBooms2x" + i)) {
                    allBooms2[i].x = sharedpreferences.getInt("allBooms2x" + i, -1);
                    if (allBooms2[i].x != -1)
                        sharedPreferencesValid = true;
                } else {
                    sharedPreferencesValid = false;
                }

                if (sharedpreferences.contains("allBooms2y" + i)) {
                    allBooms2[i].y = sharedpreferences.getInt("allBooms2y" + i, -1);
                    if (allBooms2[i].y != -1)
                        sharedPreferencesValid = true;
                } else {
                    sharedPreferencesValid = false;
                }

                if (sharedpreferences.contains("allBooms2frames" + i)) {
                    allBooms2[i].frames = sharedpreferences.getInt("allBooms2frames" + i, -1);
                    if (allBooms2[i].frames != -1)
                        sharedPreferencesValid = true;
                } else {
                    sharedPreferencesValid = false;
                }

                if (sharedpreferences.contains("allBooms2currentFrame" + i)) {
                    allBooms2[i].currentFrame = sharedpreferences.getInt("allBooms2currentFrame" + i, -1);
                    if (allBooms2[i].currentFrame != -1)
                        sharedPreferencesValid = true;
                } else {
                    sharedPreferencesValid = false;
                }

                if (sharedpreferences.contains("allBooms2currentfps" + i)) {
                    allBooms2[i].fps = sharedpreferences.getInt("allBooms2currentfps" + i, -1);
                    if (allBooms2[i].fps != -1)
                        sharedPreferencesValid = true;
                } else {
                    sharedPreferencesValid = false;
                }

                if (sharedpreferences.contains("allBooms2currentcounter" + i)) {
                    allBooms2[i].counter = sharedpreferences.getInt("allBooms2currentcounter" + i, -1);
                    if (allBooms2[i].counter != -1)
                        sharedPreferencesValid = true;
                } else {
                    sharedPreferencesValid = false;
                }

                if (sharedpreferences.contains("allBooms2critx" + i)) {
                    allBooms2[i].critx = sharedpreferences.getInt("allBooms2critx" + i, -1);
                    if (allBooms2[i].critx != -1)
                        sharedPreferencesValid = true;
                } else {
                    sharedPreferencesValid = false;
                }

                if (sharedpreferences.contains("allBooms2crity" + i)) {
                    allBooms2[i].crity = sharedpreferences.getInt("allBooms2crity" + i, -1);
                    if (allBooms2[i].crity != -1)
                        sharedPreferencesValid = true;
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
                    if (!allBooms2[i].c.equals(""))
                        sharedPreferencesValid = true;
                    else
                        sharedPreferencesValid = false;
                } else {
                    sharedPreferencesValid = false;
                }
            }
        }
    }

    private void getLargeItemSharedPreferences(SharedPreferences sharedpreferences) {
        if (sharedpreferences.contains("largeItemPieces")) {
            largeItemPieces = sharedpreferences.getInt("largeItemPieces", -1);
            largeItem = new LargeItem(incSpeed, largeItemPieces);
            for (int i = 0; i < largeItemPieces; i++) {

                if (sharedpreferences.contains("largeItempieceslargex" + i)) {
                    largeItem.pieces[i].largex = sharedpreferences.getInt("largeItempieceslargex" + i, -1);
                    if (largeItem.pieces[i].largex != -1)
                        sharedPreferencesValid = true;
                } else {
                    sharedPreferencesValid = false;
                }

                if (sharedpreferences.contains("largeItempieceslargey" + i)) {
                    largeItem.pieces[i].largey = sharedpreferences.getInt("largeItempieceslargey" + i, -1);
                    if (largeItem.pieces[i].largey != -1)
                        sharedPreferencesValid = true;
                } else {
                    sharedPreferencesValid = false;
                }

                if (sharedpreferences.contains("largeItempiecesArrayx" + i)) {
                    largeItem.pieces[i].x = sharedpreferences.getInt("largeItempiecesArrayx" + i, -1);
                    if (largeItem.pieces[i].x != -1)
                        sharedPreferencesValid = true;
                } else {
                    sharedPreferencesValid = false;
                }

                if (sharedpreferences.contains("largeItempiecesArrayy" + i)) {
                    largeItem.pieces[i].y = sharedpreferences.getInt("largeItempiecesArrayy" + i, -1);
                    if (largeItem.pieces[i].y != -1)
                        sharedPreferencesValid = true;
                } else {
                    sharedPreferencesValid = false;
                }

                if (sharedpreferences.contains("largeItempiecesArrayItemSpeed" + i)) {
                    largeItem.pieces[i].itemSpeed = sharedpreferences.getInt("largeItempiecesArrayItemSpeed" + i, -1);
                    if (largeItem.pieces[i].itemSpeed != -1)
                        sharedPreferencesValid = true;
                } else {
                    sharedPreferencesValid = false;
                }

                if (sharedpreferences.contains("largeItempiecesArraycenter" + i)) {
                    largeItem.pieces[i].center = sharedpreferences.getInt("largeItempiecesArraycenter" + i, -1);
                    if (largeItem.pieces[i].center != -1)
                        sharedPreferencesValid = true;
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

    private void getFireArraySharedPreferences(SharedPreferences sharedpreferences) {
        if (sharedpreferences.contains("numberOfFire")) {
            numberOfFire = sharedpreferences.getInt("numberOfFire", -1);

            if (numberOfFire != -1) {
                //create new star array and populate it
                bulletArray = new Bullet[numberOfFire];
                for (int i = 0; i < numberOfFire; i++) {
                    bulletArray[i] = new Bullet();
                    if (sharedpreferences.contains("FireArrayx" + i)) {
                        bulletArray[i].x = sharedpreferences.getInt("FireArrayx" + i, -1);
                        if (bulletArray[i].x != -1)
                            sharedPreferencesValid = true;
                    } else {
                        sharedPreferencesValid = false;
                    }

                    if (sharedpreferences.contains("FireArrayy" + i)) {
                        bulletArray[i].y = sharedpreferences.getInt("FireArrayy" + i, -1);
                        if (bulletArray[i].y != -1)
                            sharedPreferencesValid = true;
                    } else {
                        sharedPreferencesValid = false;
                    }

                    if (sharedpreferences.contains("FireArrayFireSpeed" + i)) {
                        bulletArray[i].fireSpeed = sharedpreferences.getInt("FireArrayFireSpeed" + i, -1);
                        if (bulletArray[i].fireSpeed != -1)
                            sharedPreferencesValid = true;
                    } else {
                        sharedPreferencesValid = false;
                    }

                    if (sharedpreferences.contains("FireArraycenter" + i)) {
                        bulletArray[i].center = sharedpreferences.getInt("FireArraycenter" + i, -1);
                        if (bulletArray[i].center != -1)
                            sharedPreferencesValid = true;
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

    private void getStarYIntArraySharedPreferences(SharedPreferences sharedpreferences) {
        if (sharedpreferences.contains("numberOfStarsY")) {
            numberOfStarsY = sharedpreferences.getInt("numberOfStarsY", -1);

            if (numberOfStarsY != -1) {
                //create new star array and populate it
                starsY = new int[numberOfStarsY];
                for (int i = 0; i < numberOfStarsY; i++) {
                    if (sharedpreferences.contains("starsY" + i)) {
                        starsY[i] = sharedpreferences.getInt("starsY" + i, -1);
                        if (starsY[i] != -1)
                            sharedPreferencesValid = true;
                    } else {
                        sharedPreferencesValid = false;
                    }
                }
            }
        }
    }

    private void getStarXIntArraySharedPreferences(SharedPreferences sharedpreferences) {
        if (sharedpreferences.contains("numberOfStarsX")) {
            numberOfStarsX = sharedpreferences.getInt("numberOfStarsX", -1);

            if (numberOfStarsX != -1) {
                //create new star array and populate it
                starsX = new int[numberOfStarsX];
                for (int i = 0; i < numberOfStarsX; i++) {
                    if (sharedpreferences.contains("starsX" + i)) {
                        starsX[i] = sharedpreferences.getInt("starsX" + i, -1);
                        if (starsX[i] != -1)
                            sharedPreferencesValid = true;
                    } else {
                        sharedPreferencesValid = false;
                    }
                }
            }
        }
    }

    private void getStarArray(SharedPreferences sharedpreferences) {
        if (sharedpreferences.contains("numberOfStars")) {
            numberOfStars = sharedpreferences.getInt("numberOfStars", -1);

            if (numberOfStars != -1) {
                //create new star array and populate it
                lightArray = new Light[numberOfStars];

                for (int i = 0; i < numberOfStars; i++) {
                    lightArray[i] = new Light();
                    if (sharedpreferences.contains("starArraystarW" + i)) {
                        lightArray[i].starW = sharedpreferences.getInt("starArraystarW" + i, -1);
                        if (lightArray[i].starW != -1)
                            sharedPreferencesValid = true;
                    } else {
                        sharedPreferencesValid = false;
                    }

                    if (sharedpreferences.contains("starArraystarH" + i)) {
                        lightArray[i].starH = sharedpreferences.getInt("starArraystarH" + i, -1);
                        if (lightArray[i].starH != -1)
                            sharedPreferencesValid = true;
                    } else {
                        sharedPreferencesValid = false;
                    }

                    if (sharedpreferences.contains("starArraystarX" + i)) {
                        lightArray[i].starX = sharedpreferences.getInt("starArraystarX" + i, -1);
                        if (lightArray[i].starX != -1)
                            sharedPreferencesValid = true;
                    } else {
                        sharedPreferencesValid = false;
                    }

                    if (sharedpreferences.contains("starArraystarY" + i)) {
                        lightArray[i].starY = sharedpreferences.getInt("starArraystarY" + i, -1);
                        if (lightArray[i].starY != -1)
                            sharedPreferencesValid = true;
                    } else {
                        sharedPreferencesValid = false;
                    }

                    if (sharedpreferences.contains("starArrayspeed" + i)) {
                        lightArray[i].speed = sharedpreferences.getInt("starArrayspeed" + i, -1);
                        if (lightArray[i].speed != -1)
                            sharedPreferencesValid = true;
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

    private void getItemArraySharedPreferences(SharedPreferences sharedpreferences) {
        if (sharedpreferences.contains("numberOfItemsInItemArray")) {
            numberOfItemsInItemArray = sharedpreferences.getInt("numberOfItemsInItemArray", -1);

            if (numberOfItemsInItemArray != -1) {
                //create new array of Items and populate it
                itemArray = new Item[numberOfItemsInItemArray];
                for (int i = 0; i < numberOfItemsInItemArray; i++) {
                    itemArray[i] = new Item();
                    if (sharedpreferences.contains("ItemArraylargex" + i)) {
                        itemArray[i].largex = sharedpreferences.getInt("ItemArraylargex" + i, -1);
                        if (itemArray[i].largex != -1)
                            sharedPreferencesValid = true;
                    } else {
                        sharedPreferencesValid = false;
                    }

                    if (sharedpreferences.contains("ItemArraylargey" + i)) {
                        itemArray[i].largey = sharedpreferences.getInt("ItemArraylargey" + i, -1);
                        if (itemArray[i].largey != -1)
                            sharedPreferencesValid = true;
                    } else {
                        sharedPreferencesValid = false;
                    }

                    if (sharedpreferences.contains("ItemArrayx" + i)) {
                        itemArray[i].x = sharedpreferences.getInt("ItemArrayx" + i, -1);
                        if (itemArray[i].x != -1)
                            sharedPreferencesValid = true;
                    } else {
                        sharedPreferencesValid = false;
                    }

                    if (sharedpreferences.contains("ItemArrayy" + i)) {
                        itemArray[i].y = sharedpreferences.getInt("ItemArrayy" + i, -1);
                        if (itemArray[i].y != -1)
                            sharedPreferencesValid = true;
                    } else {
                        sharedPreferencesValid = false;
                    }

                    if (sharedpreferences.contains("ItemArrayItemSpeed" + i)) {
                        itemArray[i].itemSpeed = sharedpreferences.getInt("ItemArrayItemSpeed" + i, -1);
                        if (itemArray[i].itemSpeed != -1)
                            sharedPreferencesValid = true;
                    } else {
                        sharedPreferencesValid = false;
                    }

                    if (sharedpreferences.contains("ItemArraymovementType" + i)) {
                        itemArray[i].movementType = sharedpreferences.getInt("ItemArraymovementType" + i, -1);
                        if (itemArray[i].movementType != -1)
                            sharedPreferencesValid = true;
                    } else {
                        sharedPreferencesValid = false;
                    }

                    if (sharedpreferences.contains("ItemArraycenter" + i)) {
                        itemArray[i].center = sharedpreferences.getInt("ItemArraycenter" + i, -1);
                        if (itemArray[i].center != -1)
                            sharedPreferencesValid = true;
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

    private void getStringSharedPreferences(SharedPreferences sharedpreferences) {
        if (sharedpreferences.contains("text")) {
            text = sharedpreferences.getString("text", "");
            if (text.equals(""))
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("chainText")) {
            chainText = sharedpreferences.getString("chainText", "");
            if (chainText.equals(""))
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("rapidS")) {
            rapidS = sharedpreferences.getString("rapidS", "");
            if (rapidS.equals(""))
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("FireString")) {
            fireString = sharedpreferences.getString("FireString", "");
            if (fireString.equals(""))
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("textString1")) {
            textString1 = sharedpreferences.getString("textString1", "");
            if (textString1.equals(""))
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("textString2")) {
            textString2 = sharedpreferences.getString("textString2", "");
            if (textString2.equals(""))
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("textString3")) {
            textString3 = sharedpreferences.getString("textString3", "");
            if (textString3.equals(""))
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("textString4")) {
            textString4 = sharedpreferences.getString("textString4", "");
            if (textString4.equals(""))
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("textString5")) {
            textString5 = sharedpreferences.getString("textString5", "");
            if (textString5.equals(""))
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("textString6")) {
            textString6 = sharedpreferences.getString("textString6", "");
            if (textString6.equals(""))
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("incomingLargeItemText")) {
            incomingLargeItemText = sharedpreferences.getString("incomingLargeItemText", "");
            if (incomingLargeItemText.equals(""))
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("doughString")) {
            shieldString = sharedpreferences.getString("doughString", "");
            if (shieldString.equals(""))
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("chainString")) {
            chainString = sharedpreferences.getString("chainString", "");
            if (chainString.equals(""))
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("finalScoreText")) {
            finalScoreText = sharedpreferences.getString("finalScoreText", "");
            if (finalScoreText.equals(""))
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }
    }

    private void getLongSharedPreferences(SharedPreferences sharedpreferences) {
        if (sharedpreferences.contains("score")) {
            score = sharedpreferences.getInt("score", -1);
            if (score != -1l)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("tempTimer2")) {
            tempTimer2 = sharedpreferences.getLong("tempTimer2", -1l);
            if (tempTimer2 != -1l)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("tempTimer3")) {
            tempTimer3 = sharedpreferences.getLong("tempTimer3", -1l);
            if (tempTimer3 != -1l)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("finalScore")) {
            finalScore = sharedpreferences.getInt("finalScore", -1);
            if (finalScore != -1l)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("displayScoreTimer")) {
            displayScoreTimer = sharedpreferences.getLong("displayScoreTimer", -1l);
            if (displayScoreTimer != -1l)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("chainTextTimer")) {
            chainTextTimer = sharedpreferences.getLong("chainTextTimer", -1l);
            if (chainTextTimer != -1l)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }
    }

    private void getFloatSharedPreferences(SharedPreferences sharedpreferences) {
        if (sharedpreferences.contains("volume")) {
            volume = sharedpreferences.getFloat("volume", -1f);
            if (volume != -1f)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("prevVolume")) {
            prevVolume = sharedpreferences.getFloat("prevVolume", -1f);
            if (prevVolume != -1f)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("currVolume")) {
            currVolume = sharedpreferences.getFloat("currVolume", -1f);
            if (currVolume != -1f)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }
    }

    private void getIntSharedPreferences(SharedPreferences sharedpreferences) {
        if (sharedpreferences.contains("acc")) {
            acc = sharedpreferences.getInt("acc", -1);
            if (acc != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("largeW")) {
            largeW = sharedpreferences.getInt("largeW", -1);
            if (largeW != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("largeH")) {
            largeH = sharedpreferences.getInt("largeH", -1);
            if (largeH != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("aCurr")) {
            aCurr = sharedpreferences.getInt("aCurr", -1);
            if (aCurr != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("sCurr")) {
            sCurr = sharedpreferences.getInt("sCurr", -1);
            if (sCurr != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("timer")) {
            timer = sharedpreferences.getInt("timer", -1);
            if (timer != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("myX")) {
            myX = sharedpreferences.getInt("myX", -1);
            if (myX != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("myY")) {
            myY = sharedpreferences.getInt("myY", -1);
            if (myY != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("maxItems")) {
            maxItems = sharedpreferences.getInt("maxItems", -1);
            if (maxItems != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("ItemCount")) {
            itemCount = sharedpreferences.getInt("ItemCount", -1);
            if (itemCount != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("bonusSpeed")) {
            bonusSpeed = sharedpreferences.getInt("bonusSpeed", -1);
            if (bonusSpeed != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("ItemH")) {
            itemH = sharedpreferences.getInt("ItemH", -1);
            if (itemH != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("ItemW")) {
            itemW = sharedpreferences.getInt("ItemW", -1);
            if (itemW != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("displayBonusTextTimer")) {
            displayBonusTextTimer = sharedpreferences.getInt("displayBonusTextTimer", -1);
            if (displayBonusTextTimer != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("textSize1")) {
            textSize1 = sharedpreferences.getInt("textSize1", -1);
            if (textSize1 != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("jx1")) {
            jx1 = sharedpreferences.getInt("jx1", -1);
            if (jx1 != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("jx2")) {
            jx2 = sharedpreferences.getInt("jx2", -1);
            if (jx2 != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("jy1")) {
            jy1 = sharedpreferences.getInt("jy1", -1);
            if (jy1 != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("jy2")) {
            jy2 = sharedpreferences.getInt("jy2", -1);
            if (jy2 != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("roundChain")) {
            roundChain = sharedpreferences.getInt("roundChain", -1);
            if (roundChain != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("starTrail")) {
            starTrail = sharedpreferences.getInt("starTrail", -1);
            if (starTrail != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("wallbX")) {
            wallbX = sharedpreferences.getInt("wallbX", -1);
            if (wallbX != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("wallbY")) {
            wallbY = sharedpreferences.getInt("wallbY", -1);
            if (wallbY != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("wallX")) {
            wallX = sharedpreferences.getInt("wallX", -1);
            if (wallX != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("wallY")) {
            wallY = sharedpreferences.getInt("wallY", -1);
            if (wallY != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("FireW")) {
            fireW = sharedpreferences.getInt("FireW", -1);
            if (fireW != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("FireH")) {
            fireH = sharedpreferences.getInt("FireH", -1);
            if (fireH != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("rapidH")) {
            rapidH = sharedpreferences.getInt("rapidH", -1);
            if (rapidH != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("noteHeight")) {
            noteHeight = sharedpreferences.getInt("noteHeight", -1);
            if (noteHeight != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("noteWidth")) {
            noteWidth = sharedpreferences.getInt("noteWidth", -1);
            if (noteWidth != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("incSpeed")) {
            incSpeed = sharedpreferences.getInt("incSpeed", -1);
            if (incSpeed != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("maxFires")) {
            maxFire = sharedpreferences.getInt("maxFires", -1);
            if (maxFire != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("currChain")) {
            currChain = sharedpreferences.getInt("currChain", -1);
            if (currChain != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("playerMapW")) {
            playerW = sharedpreferences.getInt("playerMapW", -1);
            if (playerW != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("playerMapH")) {
            playerH = sharedpreferences.getInt("playerMapH", -1);
            if (playerH != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("sMax")) {
            sMax = sharedpreferences.getInt("sMax", -1);
            if (sMax != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("aMax")) {
            aMax = sharedpreferences.getInt("aMax", -1);
            if (aMax != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("speed")) {
            speed = sharedpreferences.getInt("speed", -1);
            if (speed != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("i")) {
            i = sharedpreferences.getInt("i", -1);
            if (i != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("newX")) {
            newX = sharedpreferences.getInt("newX", -1);
            if (newX != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("newY")) {
            newY = sharedpreferences.getInt("newY", -1);
            if (newY != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("rapidCount")) {
            rapidCount = sharedpreferences.getInt("rapidCount", -1);
            if (rapidCount != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("Itemx1")) {
            itemx1 = sharedpreferences.getInt("Itemx1", -1);
            if (itemx1 != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("Itemx2")) {
            itemx2 = sharedpreferences.getInt("Itemx2", -1);
            if (itemx2 != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("Itemy1")) {
            itemy1 = sharedpreferences.getInt("Itemy1", -1);
            if (itemy1 != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("Itemy2")) {
            itemy2 = sharedpreferences.getInt("Itemy2", -1);
            if (itemy2 != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("temp1")) {
            temp1 = sharedpreferences.getInt("temp1", -1);
            if (temp1 != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("temp2")) {
            temp2 = sharedpreferences.getInt("temp2", -1);
            if (temp2 != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("tempscore")) {
            tempscore = sharedpreferences.getInt("tempscore", -1);
            if (tempscore != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("maxChain")) {
            maxChain = sharedpreferences.getInt("maxChain", -1);
            if (maxChain != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("critBonus")) {
            critBonus = sharedpreferences.getInt("critBonus", -1);
            if (critBonus != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("chainBonus")) {
            chainBonus = sharedpreferences.getInt("chainBonus", -1);
            if (chainBonus != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("Firex1")) {
            firex1 = sharedpreferences.getInt("Firex1", -1);
            if (firex1 != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("Firex2")) {
            firex2 = sharedpreferences.getInt("Firex2", -1);
            if (firex2 != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("ItemW")) {
            itemW = sharedpreferences.getInt("ItemW", -1);
            if (itemW != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("ItemH")) {
            itemH = sharedpreferences.getInt("ItemH", -1);
            if (itemH != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("px1")) {
            px1 = sharedpreferences.getInt("px1", -1);
            if (px1 != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("px2")) {
            px2 = sharedpreferences.getInt("px2", -1);
            if (px2 != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("py1")) {
            py1 = sharedpreferences.getInt("py1", -1);
            if (py1 != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("py2")) {
            py2 = sharedpreferences.getInt("py2", -1);
            if (py2 != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("bossNumber")) {
            bossNumber = sharedpreferences.getInt("bossNumber", -1);
            if (bossNumber != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("numStars")) {
            numStars = sharedpreferences.getInt("numStars", -1);
            if (numStars != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("startSpeedOffset")) {
            startSpeedOffset = sharedpreferences.getInt("startSpeedOffset", -1);
            if (startSpeedOffset != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("speedOffset")) {
            speedOffset = sharedpreferences.getInt("speedOffset", -1);
            if (speedOffset != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("currSpeedOffset")) {
            currSpeedOffset = sharedpreferences.getInt("currSpeedOffset", -1);
            if (currSpeedOffset != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("FireSpeed")) {
            fireSpeed = sharedpreferences.getInt("FireSpeed", -1);
            if (fireSpeed != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("textSize1")) {
            textSize1 = sharedpreferences.getInt("textSize1", -1);
            if (textSize1 != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("textSize2")) {
            textSize2 = sharedpreferences.getInt("textSize2", -1);
            if (textSize2 != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("textSize3")) {
            textSize3 = sharedpreferences.getInt("textSize3", -1);
            if (textSize3 != -1)
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

    }

    private void getBooleanSharedPreferences(SharedPreferences sharedpreferences) {
        if (sharedpreferences.contains("loadingScreen")) {
            loadingScreen = sharedpreferences.getBoolean("loadingScreen", false);
            if (loadingScreen)
                sharedPreferencesValid = false;
            else
                sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("introScreenPlaying")) {
            introScreenPlaying = sharedpreferences.getBoolean("introScreenPlaying", false);
            if (introScreenPlaying) {
                sharedPreferencesValid = false;
            } else {
                sharedPreferencesValid = true;
            }
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
            mp4winplaying = sharedpreferences.getBoolean("mp4winplaying", false);
            sharedPreferencesValid = true;
        } else {
            sharedPreferencesValid = false;
        }

        if (sharedpreferences.contains("mp2gameplayplaying")) {
            mp2gameplayplaying = sharedpreferences.getBoolean("mp2gameplayplaying", false);
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
        if (soundPool == null)
            initSoundPool();
    }

    public void onActivityResult() {
        resetGameVars();
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus)
            isWindowReady = true;
        else
            isWindowReady = false;
    }

    public void onRestart() {
        Thread.State threadState = myThread1.getState();
        Log.d(TAG, "thread state " + threadState);
        switch (threadState) {
            case BLOCKED:
                break;
            case NEW:
                break;
            case RUNNABLE:
                continueThread = true;
                break;
            case TERMINATED:
                break;
            case TIMED_WAITING:
                break;
            case WAITING:
                break;
            default:
                break;
        }
    }

    public void onPause() {
        getThread().pause();
    }

    public void onStop() {
        if (isLogging)
            Log.d(TAG, "storing Shared Pres");

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
        editor.putBoolean("mp4winplaying", mp4winplaying);
        editor.putBoolean("mp2gameplayplaying", mp2gameplayplaying);
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
        editor.putInt("wallbX", wallbX);
        editor.putInt("wallbY", wallbY);
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
        editor.putInt("Itemx1", itemx1);
        editor.putInt("Itemx2", itemx2);
        editor.putInt("Itemy1", itemy1);
        editor.putInt("Itemy2", itemy2);
        editor.putInt("temp1", temp1);
        editor.putInt("temp2", temp2);
        editor.putInt("tempscore", tempscore);
        editor.putInt("maxChain", maxChain);
        editor.putInt("critBonus", critBonus);
        editor.putInt("chainBonus", chainBonus);
        editor.putInt("Firex1", firex1);
        editor.putInt("Firex2", firex2);
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

        //Item array is slightly more complex
        //get the number of Items in this array at this time
        //set each variable to the nthItem and the vars to be set to it as well
        numberOfItemsInItemArray = itemArray.length;
        editor.putInt("numberOfItemsInItemArray", numberOfItemsInItemArray);
        //now that we know the number of Items in the array we can save each Item variables
        /**
         * public int largex, largey;
         * public int x, y, ItemSpeed;
         * public boolean exists;
         * public int movementType;
         * public int center;
         * public boolean moveRight;
         */
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

        //starArray is another complex save
        /**
         * 	public int starW, starH, starX, starY, speed;
         public boolean exists = false;
         */
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
        /**
         * 	public int x,y;
         public int FireSpeed;
         public boolean exists;
         public int center;
         */
        for (int i = 0; i < numberOfFire; i++) {
            editor.putInt("FireArrayx" + i, bulletArray[i].x);
            editor.putInt("FireArrayy" + i, bulletArray[i].y);
            editor.putInt("FireArrayFireSpeed" + i, bulletArray[i].fireSpeed);
            editor.putInt("FireArraycenter" + i, bulletArray[i].center);
            editor.putBoolean("FireArrayexists" + i, bulletArray[i].exists);
        }

        // largeItem has pieces to it
        largeItemPieces = largeItem.pieces.length;
        editor.putInt("largeItemPieces", largeItemPieces);
        //now that we know the number of Items in the array we can save each Item variables
        /**
         * public int largex, largey;
         * public int x, y, ItemSpeed;
         * public boolean exists;
         * public int movementType;
         * public int center;
         * public boolean moveRight;
         */
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

        //allBooms are also complex
        numberOfAllBooms = allBooms2.length;
        editor.putInt("numberOfAllBooms", numberOfAllBooms);
        /**
         * 	public Bitmap[] booms;
         public int x;
         public int y;
         public int frames;
         public int currentFrame;
         public int fps;
         public int counter;
         public boolean booming;
         public boolean crit = false;
         public int critx, crity;
         public Paint paint;
         public String c;
         */
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
        /**
         * 	public int jx1, jy1, jx2, jy2;
         public Paint shieldColor;
         public boolean exists;
         public int shieldCount;
         */
        for (int i = 0; i < numberOfShields; i++) {
            editor.putInt("shieldsjx1" + i, effects[i].jx1);
            editor.putInt("shieldsjy1" + i, effects[i].jy1);
            editor.putInt("shieldsjx2" + i, effects[i].jx2);
            editor.putInt("shieldsjy2" + i, effects[i].jy2);
            editor.putInt("shieldsshieldCount" + i, effects[i].shieldCount);
            editor.putBoolean("shieldsexists" + i, effects[i].exists);
        }

        // Commit the edits!
        editor.commit();
    }

    public void onDestroy() {
        if (soundPool != null)
            releaseSoundPool();
    }
}