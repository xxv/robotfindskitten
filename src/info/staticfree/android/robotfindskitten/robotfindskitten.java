package info.staticfree.android.robotfindskitten;

/*
 *robotfindskitten: A Zen simulation
 *
 * Copyright ©2009 Steve Pomeroy <steve@staticfree.info>
 * some bits Copyright (C) 1997,2000 Leonard Richardson
 *                        leonardr@segfault.org
 *                        http://www.crummy.com/devel/
 *
 *   This program is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU General Public License as
 *   published by the Free Software Foundation; either version 2 of
 *   the License, or (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or EXISTANCE OF KITTEN.  See the GNU General
 *   Public License for more details.
 *
 *   http://www.gnu.org/copyleft/gpl.html
 *
 */

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

import org.json.JSONArray;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

// TODO make fonts scaled on tablets so that they'd be approx 80x25 - Dan Bornstein <danfuzz@milk.com>
public class robotfindskitten extends Activity implements OnGestureListener {
    public enum InputMode {
        ANY_KEY, DIRECTIONAL, NO_INPUT
    };

    public enum Direction {
        UP, RIGHT, DOWN, LEFT
    };

    public enum GameState {
        INTRO, INGAME, ENDGAME
    };

    static final int ABOUT_DIALOG = 0;

    private final Random rand = new Random();

    private RFKView rfkView; // our kitten-seeking arena
    private Thing robot;
    private Thing kitten;

    private boolean thingMessageAtTop;
    private Thing recentCollision;

    // all valid messages and Thing characters
    private final ArrayList<String> messages = new ArrayList<String>();
    private String validChars;

    private InputMode inputMode = InputMode.ANY_KEY;
    private GameState gameState = GameState.INTRO;
    private TextView thingMessage;
    private Animation messageDisappear;
    private Animation messageAppear;

    private GestureDetector gestureDetector;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gestureDetector = new GestureDetector(this, this);
        // It's not a game if you can still see the chrome
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        showIntro();

        // load all the characters. Eventually should pull unicode chars, too.
        final StringBuilder chars = new StringBuilder();
        for (int i = 0x21; i < 127; i++) {
            final char c = Character.toChars(i)[0];
            if (!Character.isWhitespace(c) && !Character.isISOControl(c) && c != '#') {
                chars.append(c);
            }
        }
        validChars = chars.toString();

        loadMessages();

        messageDisappear = AnimationUtils.loadAnimation(this, R.anim.message_disappear);
        messageAppear = AnimationUtils.loadAnimation(this, R.anim.message_appear);
    }

    /**
     * Pull in all the messages from a JSON file.
     *
     * Eventually, one could have a JSON-serving URL for more!
     */
    private void loadMessages() {
        final InputStream is = getResources().openRawResource(R.raw.messages);

        final StringBuilder jsonString = new StringBuilder();
        try {

            for (final BufferedReader isReader = new BufferedReader(new InputStreamReader(is),
                    16000); isReader.ready();) {
                jsonString.append(isReader.readLine());
            }

            final JSONArray msgJson = new JSONArray(jsonString.toString());

            for (int i = 0; i < msgJson.length(); i++) {
                messages.add(msgJson.getString(i));
            }
        } catch (final Exception e) {
            Toast.makeText(this, "error reading NKI messages: " + e.toString(), Toast.LENGTH_LONG)
                    .show();
            e.printStackTrace();
            finish();
        }
    }

    /**
     * Show the intro screen.
     */
    public void showIntro() {
        setContentView(R.layout.intro);
        final Animation fadeInAnim = AnimationUtils.loadAnimation(this, R.anim.fadein);
        fadeInAnim.setFillAfter(true);

        // ease on in.
        ((RelativeLayout) findViewById(R.id.intro_layout)).startAnimation(fadeInAnim);
    }

    /**
     * Start the game. Use this when the main view isn't the gameboard.
     */
    public void startGame() {
        gameState = GameState.INGAME;
        thingMessageAtTop = true;
        // thingMessageHidden = true;
        setContentView(R.layout.main);

        thingMessage = (TextView) findViewById(R.id.thing_message);
        final Animation fadeInAnim = AnimationUtils.loadAnimation(this, R.anim.fadein);
        fadeInAnim.setFillAfter(true);

        // ease on in.
        ((RelativeLayout) findViewById(R.id.screen)).startAnimation(fadeInAnim);
        rfkView = (RFKView) findViewById(R.id.rfk);

        resetGame();
    }

    /**
     * Wipe and repopulate the gameboard.
     */
    public void resetGame() {
        gameState = GameState.INGAME;
        rfkView.clearBoard();
        hideSystemUi();

        addThings();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void hideSystemUi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            rfkView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        }
    }

    public char randomChar() {
        return validChars.charAt(rand.nextInt(validChars.length()));
    }

    /**
     * Add all the important Things to the arena. Adds unimportant Things, too.
     */
    public void addThings() {
        robot = new Thing(Thing.ROBOT);
        rfkView.addThing(robot);

        kitten = new Thing(Thing.KITTEN);
        kitten.character = Character.toString(randomChar());
        rfkView.addThing(kitten);

        // add in the other things that aren't kitten
        for (int i = 0; i < 20; i++) {
            final Thing something = new Thing(Thing.NKI);
            something.character = Character.toString(randomChar());

            // give that something a unique message
            while (something.message == null) {
                something.message = messages.get(rand.nextInt(messages.size()));
                for (final Thing someOtherThing : rfkView.getThings()) {
                    if (something.message == someOtherThing.message) {
                        something.message = null;
                        break;
                    }
                }
            }
            rfkView.addThing(something);
        }
    }

    /**
     * Determine/handle if movement to x, y would result in a collision. If it does, handle
     * collision.
     *
     * @param x
     * @param y
     * @return true if a collision occurred, false if we're free to wiggle.
     */
    public boolean isCollision(int x, int y) {
        final Thing thing = rfkView.thingAt(x, y);

        if (thing != null) {
            // we've already handled this collision, no need to repeat.
            if (thing == recentCollision) {
                return true;
            }
            recentCollision = thing;

            if (thing.type == Thing.KITTEN) {
                thingMessage.setVisibility(View.INVISIBLE);
                endGameAnimation();

            } else if (thing.type == Thing.NKI) {
                setThingMessage(thing.message);

            }
            return true;
        } else {
            hideThingMessage();
            recentCollision = null;
            return false;
        }
    }

    /**
     * MOVE 'ZIG' FOR GREAT JUSTICE.
     *
     * @param d
     */
    public void moveRobot(Direction d) {
        final int width = rfkView.getBoardWidth();
        final int height = rfkView.getBoardHeight();
        switch (d) {
            case UP:
                if (robot.y == 0) {
                    break;
                }
                if (!isCollision(robot.x, robot.y - 1)) {
                    robot.y--;
                }
                break;

            case DOWN:
                if (robot.y == height - 1) {
                    break;
                }
                if (!isCollision(robot.x, robot.y + 1)) {
                    robot.y++;
                }
                break;

            case LEFT:
                if (robot.x == 0) {
                    break;
                }
                if (!isCollision(robot.x - 1, robot.y)) {
                    robot.x--;
                }
                break;

            case RIGHT:
                if (robot.x == width - 1) {
                    break;
                }
                if (!isCollision(robot.x + 1, robot.y)) {
                    robot.x++;
                }
                break;

        }
        rfkView.invalidate();
    }

    private final static RelativeLayout.LayoutParams TOP_LAYOUT = new RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    private final static RelativeLayout.LayoutParams BOTTOM_LAYOUT = new RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    static {
        TOP_LAYOUT.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        BOTTOM_LAYOUT.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
    }

    private void setThingMessage(String message) {
        thingMessage.setText(message);

        // reposition the message window so it doesn't block you from moving around.
        if (thingMessageAtTop && robot.y < (0.25 * rfkView.getBoardHeight())) {
            thingMessage.setLayoutParams(BOTTOM_LAYOUT);
            thingMessageAtTop = false;

        } else if (!thingMessageAtTop && robot.y > (0.75 * rfkView.getBoardHeight())) {
            thingMessage.setLayoutParams(TOP_LAYOUT);
            thingMessageAtTop = true;
        }

        // show the message text
        if (thingMessage.getVisibility() == View.INVISIBLE) {
            thingMessage.startAnimation(messageAppear);
            thingMessage.setVisibility(View.VISIBLE);
        }
    }

    public void hideThingMessage() {
        if (thingMessage.getVisibility() == View.INVISIBLE) {
            return;
        }

        thingMessage.startAnimation(messageDisappear);
        thingMessage.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                showDialog(ABOUT_DIALOG);
                return true;
        }
        return false;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case ABOUT_DIALOG:
                final Builder builder = new AlertDialog.Builder(this);

                builder.setTitle(R.string.about_title);
                builder.setIcon(R.drawable.icon);

                // using this instead of setMessage lets us have clickable links.
                final LayoutInflater factory = LayoutInflater.from(this);
                builder.setView(factory.inflate(R.layout.about, null));

                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        setResult(RESULT_OK);
                    }
                });

                return builder.create();
        }
        return null;
    }

    private void advanceGame() {
        switch (gameState) {

            case INGAME:
                resetGame();
                break;
            case ENDGAME:
            case INTRO:
                startGame();
                break;
        }
        inputMode = InputMode.DIRECTIONAL;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (super.onKeyDown(keyCode, event)) {
            return true;
        }

        if (inputMode == InputMode.ANY_KEY) {
            // ok, this isn't really /any/ key.
            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                advanceGame();
                return true;
            }

        } else if (inputMode == InputMode.DIRECTIONAL) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                moveRobot(Direction.LEFT);
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                moveRobot(Direction.UP);
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                moveRobot(Direction.RIGHT);
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                moveRobot(Direction.DOWN);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    public boolean onDown(MotionEvent e) {
        return false;
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    public void onLongPress(MotionEvent e) {

    }

    private float dxSum;
    private float dySum;
    private static final int SENSITIVITY = 40; // This feels pretty good.
                                               // Slightly slower than finger
                                               // movement on a G1

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (inputMode == InputMode.DIRECTIONAL) {
            dxSum += distanceX;
            dySum += distanceY;
            if (dxSum > SENSITIVITY) {
                moveRobot(Direction.LEFT);
                dxSum = 0;
            } else if (dxSum < -SENSITIVITY) {
                moveRobot(Direction.RIGHT);
                dxSum = 0;
            }

            if (dySum > SENSITIVITY) {
                moveRobot(Direction.UP);
                dySum = 0;
            } else if (dySum < -SENSITIVITY) {
                moveRobot(Direction.DOWN);
                dySum = 0;
            }
            return true;
        }
        return false;
    }

    public void onShowPress(MotionEvent e) {
    }

    public boolean onSingleTapUp(MotionEvent e) {
        if (inputMode == InputMode.ANY_KEY) {
            advanceGame();
            return true;
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void endGameSetSystemUiVisibility(int visibility) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            final TextView robot = (TextView) findViewById(R.id.anim_robot);
            robot.setSystemUiVisibility(visibility);
        }
    }

    /**
     * Play the endgame animation and move game to next state.
     */
    public void endGameAnimation() {

        gameState = GameState.ENDGAME;

        setContentView(R.layout.endgame);

        inputMode = InputMode.NO_INPUT;

        final TextView robot = (TextView) findViewById(R.id.anim_robot);
        endGameSetSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);

        final TextView kitten = (TextView) findViewById(R.id.anim_kitten);
        kitten.setText(this.kitten.character);
        kitten.setTextColor(this.kitten.color);
        final TextView heart = (TextView) findViewById(R.id.anim_heart);
        final TextView heartAbove = (TextView) findViewById(R.id.anim_heart_above);
        final TextView win = (TextView) findViewById(R.id.anim_wintext);
        final TextView win2 = (TextView) findViewById(R.id.anim_wintext2);

        final Animation zoomAnim = AnimationUtils.loadAnimation(this, R.anim.zoomfade);
        final Animation rightAnim = AnimationUtils.loadAnimation(this, R.anim.moveright);
        final Animation leftAnim = AnimationUtils.loadAnimation(this, R.anim.moveleft);
        final Animation fadeInAnim = AnimationUtils.loadAnimation(this, R.anim.fadeindelay);
        final Animation fadeInLaterAnim = AnimationUtils.loadAnimation(this, R.anim.fadeinlater);
        final Animation moveUpAnim = AnimationUtils.loadAnimation(this, R.anim.moveup);

        // this is supposed to be set via the XML, but it seems to be ignored there.
        leftAnim.setFillAfter(true);
        rightAnim.setFillAfter(true);
        zoomAnim.setFillAfter(true);
        fadeInAnim.setFillAfter(true);
        moveUpAnim.setFillAfter(true);
        fadeInLaterAnim.setFillAfter(true);

        robot.startAnimation(rightAnim);
        heart.startAnimation(zoomAnim);
        heartAbove.startAnimation(moveUpAnim);
        kitten.startAnimation(leftAnim);
        win.startAnimation(fadeInAnim);
        win2.startAnimation(fadeInLaterAnim);

        fadeInAnim.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                inputMode = InputMode.ANY_KEY;
            }
        });

        fadeInLaterAnim.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                endGameSetSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }
        });

        // this is an attempt to prevent flashing
        // of the animation before they are shown.
        // Keep your pants on, ladies.
        win.setVisibility(TextView.VISIBLE);
        win2.setVisibility(TextView.VISIBLE);
        heart.setVisibility(TextView.VISIBLE);
        heartAbove.setVisibility(TextView.VISIBLE);

    }
}
