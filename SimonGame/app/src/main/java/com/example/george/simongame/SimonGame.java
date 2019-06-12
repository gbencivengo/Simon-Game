package com.example.george.simongame;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Random;

public class SimonGame extends AppCompatActivity {

    //enumerate the colors into numbers
    private final static int GREEN = 1;
    private final static int RED = 2;
    private final static int YELLOW = 3;
    private final static int BLUE = 4;

    //the flow of the game should be
    //generate the sequence, play the sequence for the player, player plays it back
    private static final int GENERATE_SEQ = 1;
    private static final int PLAY_SEQ = 2;
    private static final int PLAYER_GUESS = 3;
    private static final int DEAD_STATE = 4;
    private static int CURRENT_STATE = 0;

    private static Bitmap GreenUnlit, GreenLit;
    private static Bitmap RedUnlit, RedLit;
    private static Bitmap BlueUnlit, BlueLit;
    private static Bitmap YellowUnlit, YellowLit;

    private static TextView Score;

    //the two arrays that will dictate the game
    private static ArrayList<Integer> gameSequence;
    private static ArrayList<Integer> playerSequence;

    private static long mLastMove;
    private static long mMoveDelay;
    private static int playSeqCounter;
    private static int colorTouched;
    private static boolean winOrLose;
    private static int scoreCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simon_game);
        Score = (TextView)findViewById(R.id.scoreString);
        setBitMaps();
        initGame();

    }

    buttonHandler SimonButtonHandler = new buttonHandler();
    class buttonHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            SimonGame.this.update();
        }

        public void sleep(long delayMillis) {
            this.removeMessages(0);
            sendMessageDelayed(obtainMessage(0), delayMillis);
        }
    }

    private void initGame()
    {
        gameSequence = new ArrayList<Integer>();
        playerSequence = new ArrayList<Integer>();
        mMoveDelay = 750;
        playSeqCounter = 0;
        colorTouched = 0;
        scoreCounter = 0;
        CURRENT_STATE = GENERATE_SEQ;
        update();
    }

    /*
    This is the update function, that eseentially serves as a tick function that is prominent in most game engines.
    It determines the state of the game, and directs the flow.
    There are three states in the game.
    1. Generate the Sequence
    2. Playback the Sequence
    3. User repeats the Sequence
    This goes on infinitely, and only ends when either the user presses quit or he/she loses.
    */
    private void update()
    {
        //Generate Sequence State
        if (CURRENT_STATE == GENERATE_SEQ) {
            clearPlayerSequence();
            generateSequence();
            playSeqCounter = 0; //reset the player sequence counter
            CURRENT_STATE = PLAY_SEQ;

        }

        //Play Sequence State
        if (CURRENT_STATE == PLAY_SEQ) {
            playSequence();
            if (playSeqCounter == gameSequence.size())
                CURRENT_STATE = PLAYER_GUESS;
        }

        //Player repeats/guesses state
        if (CURRENT_STATE == PLAYER_GUESS)
        {
            darkenLastButton();

            if (colorTouched != 0) {
                playerSequence.add(colorTouched);
                colorTouched = 0; //so it doesnt add the same color again
            }

            if (playerSequence.size() == gameSequence.size()) {
                winOrLose = compareSequence();

                if (!winOrLose) {
                    CURRENT_STATE = DEAD_STATE;
                    gameOver();
                }
                else {
                    CURRENT_STATE = GENERATE_SEQ;
                    scoreCounter++;
                    Score.setText("Score: " + Integer.toString(scoreCounter));
                }
            }

        }
        //What this does is, if you're in the player guess state, remove the time delay, so the user can enter the sequence as fast as he/she desires
        if (CURRENT_STATE == PLAYER_GUESS)
            SimonButtonHandler.sleep(0);
        //otherwise, use the normal delay
        else
            SimonButtonHandler.sleep(mMoveDelay);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: {
                float x = event.getX(); //normalizes X
                float y = event.getY(); //normalizes Y
                colorTouched = isTouched(x,y);
            }
        }
        return true;
    }

    //This method determines whether or not a button was pressed.
    //If one was pressed then it uses distance between the finger position and the position of the button to see if it "pressed" a button
    //It calculates this by checking to see if the difference between the finger position and the position of the button is less than the width and height of the button.
    private int isTouched(float xFinger, float yFinger)
    {
        ImageView getGreenButton = (ImageView)findViewById(R.id.greenButton);
        ImageView getRedButton = (ImageView)findViewById(R.id.redButton);
        ImageView getBlueButton = (ImageView)findViewById(R.id.blueButton);
        ImageView getYellowButton = (ImageView)findViewById(R.id.yellowButton);

        int[] greenButtonLocation = new int[2];
        int[] redButtonLocation = new int[2];
        int[] blueButtonLocation = new int[2];
        int[] yellowButtonLocation = new int[2];

        getGreenButton.getLocationInWindow(greenButtonLocation);
        getRedButton.getLocationInWindow(redButtonLocation);
        getBlueButton.getLocationInWindow(blueButtonLocation);
        getYellowButton.getLocationInWindow(yellowButtonLocation);

        if ((Math.abs(xFinger-greenButtonLocation[0]) <= getGreenButton.getWidth()) && (Math.abs(yFinger-greenButtonLocation[1]) <= getGreenButton.getHeight()))
        {
            return 1;
        }
        else if ((Math.abs(xFinger-redButtonLocation[0]) <= getRedButton.getWidth()) && (Math.abs(yFinger-redButtonLocation[1]) <= getRedButton.getHeight()))
        {
            return 2;
        }
        else if ((Math.abs(xFinger-yellowButtonLocation[0]) <= getYellowButton.getWidth()) && (Math.abs(yFinger-yellowButtonLocation[1]) <= getYellowButton.getHeight()))
        {
            return 3;
        }
        else if ((Math.abs(xFinger-blueButtonLocation[0]) <= getBlueButton.getWidth()) && (Math.abs(yFinger-blueButtonLocation[1]) <= getBlueButton.getHeight()))
        {
            return 4;
        }
        else
        {
            return 0;
        }
    }

    private void clearPlayerSequence()
    {
        colorTouched = 0;
        playerSequence.clear();
    }

    //This compares the two sequences, the game sequence and the player sequence and checks for equality.
    //If they aren't equal, then the player guessed wrong and loses
    private boolean compareSequence()
    {
        ListIterator<Integer> gameSeqITR = gameSequence.listIterator();
        ListIterator<Integer> playerSeqITR = playerSequence.listIterator();

        int gameSeqPointer, playerSeqPointer;

        while (gameSeqITR.hasNext() && playerSeqITR.hasNext())
        {
            gameSeqPointer = gameSeqITR.next();
            playerSeqPointer = playerSeqITR.next();

            System.out.println(gameSeqPointer + " : " + playerSeqPointer);

            if (gameSeqPointer != playerSeqPointer)
                return false;
        }
        return true;
    }

    //if you click the quit button, goes to quit screen
    public void quitButton(View view)
    {
        Intent intent = new Intent(this, GameOver.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("scoreCounter",scoreCounter);
        startActivity(intent);
    }

    //Game over state
    private void gameOver()
    {
        Intent intent = new Intent(this, GameOver.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("scoreCounter",scoreCounter);
        startActivity(intent);
    }

    //Generates a new portion of the sequence and adds it onto the Array List.
    private void generateSequence()
    {
        Random RNG = new Random(); //who doesn't love a little randomness :)
        gameSequence.add(RNG.nextInt(3) + 1);
    }

    //This plays the sequence
    private void playSequence()
    {
        long time = System.currentTimeMillis();

        if (time - mLastMove > mMoveDelay) {
            if (playSeqCounter != 0) {
                darkenButton(playSeqCounter - 1);
            }
            if (playSeqCounter < gameSequence.size()) {
                lightButton(playSeqCounter);
                System.out.println(playSeqCounter);
                playSeqCounter++;
            }
            mLastMove = time;
        }
    }

    //All this function does is darken the last button in the sequence.
    //I was running into a wierd bug where the last button in the sequence would remain lit, and I could figure it out, so I just made this little function
    private void darkenLastButton()
    {
        long time = System.currentTimeMillis();

        if (time - mLastMove > mMoveDelay) {
            darkenButton(playSeqCounter-1);
        }
    }

    //Lightens the given button index
    private void lightButton(int index)
    {
        ImageView litImage;

        switch (gameSequence.get(index)) {
            case GREEN:
                litImage = (ImageView) findViewById(R.id.greenButton);
                litImage.setImageBitmap(GreenLit);
                System.out.println("GREEN");
                break;
            case RED:
                litImage = (ImageView) findViewById(R.id.redButton);
                litImage.setImageBitmap(RedLit);
                System.out.println("RED");
                break;
            case BLUE:
                litImage = (ImageView) findViewById(R.id.blueButton);
                litImage.setImageBitmap(BlueLit);
                System.out.println("BLUE");
                break;
            case YELLOW:
                litImage = (ImageView) findViewById(R.id.yellowButton);
                litImage.setImageBitmap(YellowLit);
                System.out.println("YELLOW");
                break;
        }
    }

    //Darkens the given button index
    private void darkenButton(int index)
    {
        ImageView litImage;

        switch (gameSequence.get(index)) {
            case GREEN:
                litImage = (ImageView) findViewById(R.id.greenButton);
                litImage.setImageBitmap(GreenUnlit);
                break;
            case RED:
                litImage = (ImageView) findViewById(R.id.redButton);
                litImage.setImageBitmap(RedUnlit);
                break;
            case BLUE:
                litImage = (ImageView) findViewById(R.id.blueButton);
                litImage.setImageBitmap(BlueUnlit);
                break;
            case YELLOW:
                litImage = (ImageView) findViewById(R.id.yellowButton);
                litImage.setImageBitmap(YellowUnlit);
                break;
        }
    }

    //sets the bitmaps of all the regions
    private void setBitMaps()
    {
        GreenUnlit = BitmapFactory.decodeResource(getResources(),R.drawable.green_portion_unlit);
        GreenLit = BitmapFactory.decodeResource(getResources(),R.drawable.green_portion_lit);
        RedUnlit = BitmapFactory.decodeResource(getResources(),R.drawable.red_portion_unlit);
        RedLit = BitmapFactory.decodeResource(getResources(),R.drawable.red_portion_lit);
        BlueUnlit = BitmapFactory.decodeResource(getResources(),R.drawable.blue_portion_unlit);
        BlueLit = BitmapFactory.decodeResource(getResources(),R.drawable.blue_portion_lit);
        YellowUnlit = BitmapFactory.decodeResource(getResources(),R.drawable.yellow_portion_unlit);
        YellowLit = BitmapFactory.decodeResource(getResources(),R.drawable.yellow_portion_lit);
    }

}
