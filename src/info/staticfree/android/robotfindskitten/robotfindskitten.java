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

import info.staticfree.android.robotfindskitten.Thing.ThingType;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class robotfindskitten extends Activity {
	public enum InputMode { ANY_KEY, DIRECTIONAL, NO_INPUT };
	public enum Direction { UP, RIGHT, DOWN, LEFT };
	public enum GameState {INTRO, INGAME, ENDGAME };
	static final int ABOUT_DIALOG = 0;
	
	private Random rand = new Random();
	
	private RFKView rfkView; // our kitten-seeking arena
	private Thing robot;
	private Thing kitten;
	
	private boolean thingMessageAtTop;
	private boolean thingMessageHidden;
	private Thing recentCollision;
	
	// all valid messages and Thing characters
	private List<String> messages = new ArrayList<String>();
	private String validChars;
	
	private InputMode inputMode = InputMode.ANY_KEY;
	private GameState gameState = GameState.INTRO;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // It's not a game if you can still see the chrome
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);

        showIntro();
        
        // load all the characters. Eventually should pull unicode chars, too.
        StringBuilder chars = new StringBuilder();
        for (int i = 0x21; i < 127; i++){
        	char c = Character.toChars(i)[0];
        	if (! Character.isWhitespace(c) && ! Character.isISOControl(c) && c != '#'){
        		chars.append(c);
        	}
        }
        validChars = chars.toString();
        System.err.println("valid characters: " + validChars);

        loadMessages();
        
    }
    
    /**
     * Pull in all the messages from a JSON file.
     * 
     * Eventually, one could have a JSON-serving URL for more!
     */
    private void loadMessages(){
    	InputStream is = getResources().openRawResource(R.raw.messages);
    	
    	StringBuilder jsonString = new StringBuilder();
    	try{
    		
	    	for (BufferedReader isReader = new BufferedReader(new InputStreamReader(is), 16000);
	    			isReader.ready();){
	    		jsonString.append(isReader.readLine());
	    	}
	    	
	    	JSONArray msgJson = new JSONArray(jsonString.toString());
	    	
	    	for (int i = 0; i < msgJson.length(); i++){
	    		messages.add(msgJson.getString(i));
	    	}
    	}catch (Exception e){
    		Toast.makeText(this, "error reading messages: "+e.toString(), 
    				Toast.LENGTH_LONG).show();
    		e.printStackTrace();
    	}
    }
    
    /**
     * Show the intro screen.
     */
    public void showIntro(){
        setContentView(R.layout.intro);
		Animation fadeInAnim =  AnimationUtils.loadAnimation(this, R.anim.fadein);
		fadeInAnim.setFillAfter(true);
		
		// ease on in.
		((RelativeLayout)findViewById(R.id.intro_layout)).startAnimation(fadeInAnim);
    }
    
    /**
     * Start the game. Use this when the main view isn't the gameboard. 
     */
    public void startGame(){
    	thingMessageAtTop = true;
    	thingMessageHidden = true;
    	setContentView(R.layout.main);
		Animation fadeInAnim =  AnimationUtils.loadAnimation(this, R.anim.fadein);
		fadeInAnim.setFillAfter(true);
		
		// ease on in.
		((RelativeLayout)findViewById(R.id.screen)).startAnimation(fadeInAnim);
    	rfkView = (RFKView)findViewById(R.id.rfk);
    	resetGame();
    }
    
    /**
     * Wipe and repopulate the gameboard. 
     */
    public void resetGame(){
    	rfkView.clearBoard();
    	
        addThings();
    }
   
    public char randomChar(){
    	return validChars.charAt(rand.nextInt(validChars.length()));
    }
    
    /**
     * Add all the important Things to the arena.
     * Adds unimportant Things, too.
     */
    public void addThings(){
    	robot = new Thing(ThingType.ROBOT);    	
    	rfkView.addThing(robot);
    	
    	kitten = new Thing(ThingType.KITTEN);
    	kitten.character = "" + randomChar();
    	rfkView.addThing(kitten);
    	
    	// add in the other things that aren't kitten
    	for(int i = 0; i < 20; i++){
    		Thing something = new Thing(ThingType.NKI);
        	something.character = "" + randomChar();
        	
    		// give that something a unique message
    		while (something.message == null){
    			something.message =  messages.get(rand.nextInt(messages.size()));
    			for (Thing someOtherThing: rfkView.getThings()){
    				if (something.message == someOtherThing.message){
    					something.message = null;
    					break;
    				}
    			}
    		}
    		rfkView.addThing(something);
    	}
    }

    /**
     * Determine/handle if movement to x, y would result in a
     * collision. If it does, handle collision.
     * 
     * @param x
     * @param y
     * @return true if a collision occurred, false if we're free to wiggle.
     */
    public boolean isCollision(int x, int y){
    	Thing thing = rfkView.thingAt(x, y);
    	TextView thingMessage = (TextView)findViewById(R.id.thing_message);
    	
    	if (thing != null){
        	// we've already handled this collision, no need to repeat.
    		if (thing == recentCollision) return true;
    		recentCollision = thing;
    		
    		if (thing.type == ThingType.KITTEN){
    			thingMessage.setVisibility(View.INVISIBLE);
    			endGameAnimation();
    			
    		}else if (thing.type == ThingType.NKI){
    			setThingMessage(thing.message);
    			
    		}
    		return true;
    	}else {
    		hideThingMessage();
    		recentCollision = null;
    		return false;
    	}
    }
    
    /**
     * MOVE 'ZIG'
     * FOR GREAT JUSTICE.
     * 
     * @param d
     */
    public void moveRobot(Direction d){
    	int width = rfkView.getBoardWidth();
    	int height = rfkView.getBoardHeight();
    	switch (d){
    	case UP:
    		if (robot.y == 0) break;
    		if (! isCollision(robot.x, robot.y - 1)){
    			robot.y--;
    		}
    		break;

    	case DOWN:
    		if (robot.y == height) break;
    		if (! isCollision(robot.x, robot.y + 1)){
    			robot.y++;
    		}
    		break;

    	case LEFT:
    		if (robot.x == 0) break;
    		if (! isCollision(robot.x - 1, robot.y)){
    			robot.x--;
    		}
    		break;

    	case RIGHT:
    		if (robot.x == width) break;
    		if (! isCollision(robot.x + 1, robot.y)){
    			robot.x++;
    		}
    		break;

    	}
    	rfkView.invalidate();
    }

    private void setThingMessage(String message){
    	final TextView thingMessage = (TextView)findViewById(R.id.thing_message);
    	thingMessage.setText(message);

    	// reposition the message window so it doesn't block you from moving around.
    	if (thingMessageAtTop && robot.y < (0.25 * rfkView.getBoardHeight())){
			RelativeLayout.LayoutParams rp = new RelativeLayout.LayoutParams(
					ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			rp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			thingMessage.setLayoutParams(rp);
			thingMessageAtTop = false;
			
		}else if(! thingMessageAtTop && robot.y > (0.75 * rfkView.getBoardHeight())){
			RelativeLayout.LayoutParams rp = new RelativeLayout.LayoutParams(
					ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			rp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			thingMessage.setLayoutParams(rp);
			thingMessageAtTop = true;
		}
    	
    	// show the message text
		if (thingMessage.getVisibility() == View.INVISIBLE){
			thingMessage.setVisibility(View.VISIBLE);
			Animation fadeIn = new AlphaAnimation((float)0.0, (float)1.0);
			fadeIn.setDuration(200);
			thingMessage.startAnimation(fadeIn);
			thingMessageHidden = false;
		}
    }
    
    public void hideThingMessage(){
    	
		final TextView thingMessage = (TextView)findViewById(R.id.thing_message);
		if (thingMessageHidden) return;
		thingMessageHidden = true;
		
		Animation fadeOut = new AlphaAnimation((float)1.0, (float)0.0);
		fadeOut.setDuration(200);
		thingMessage.startAnimation(fadeOut);
		
		fadeOut.setFillAfter(false);
		thingMessage.startAnimation(fadeOut);
		fadeOut.setAnimationListener(new Animation.AnimationListener(){
			public void onAnimationEnd(Animation animation) {
				thingMessage.setVisibility(View.INVISIBLE);
				//rfkView.invalidate();
			}

			public void onAnimationRepeat(Animation animation) {}

			public void onAnimationStart(Animation animation) {}
		});
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getMenuInflater().inflate(R.menu.options, menu);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()){
    	/* 
    	case R.id.settings:

            Intent preferencesIntent = new Intent().setClass(this, Preferences.class);
            startActivityForResult(preferencesIntent, REQUEST_CODE_PREFERENCES);

    		return true;
    		*/
    	case R.id.about:
    		showDialog(ABOUT_DIALOG);
    		return true;
    	}
    	return false;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
    	switch (id){
    	case ABOUT_DIALOG:
        	Builder builder = new AlertDialog.Builder(this);
        	
        	builder.setTitle(R.string.about_title);
        	builder.setIcon(R.drawable.icon);
        	
        	// using this instead of setMessage lets us have clickable links.
        	LayoutInflater factory = LayoutInflater.from(this);
        	builder.setView(factory.inflate(R.layout.about, null));
        	
        	builder.setPositiveButton("Ok", new DialogInterface.OnClickListener(){
        		public void onClick(DialogInterface dialog, int which) {
        			setResult(RESULT_OK);
        		}
        	});
        	
        	return builder.create();
    	}
        return null;
    }
    
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (super.onKeyDown(keyCode, event)) return true;
    	
    	if (inputMode == InputMode.ANY_KEY){
    		// ok, this isn't really /any/ key.
    		if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER){
    			switch (gameState){
    			case ENDGAME:
    				resetGame();
    				break;
    			case INTRO:
    				startGame();
    				break;
    			}
    			inputMode = InputMode.DIRECTIONAL;
    		}
    		
    		return true;
    	}else if (inputMode == InputMode.DIRECTIONAL){
    		if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
    			moveRobot(Direction.LEFT);
    			return true;
    		}else if (keyCode == KeyEvent.KEYCODE_DPAD_UP){
    			moveRobot(Direction.UP);
    			return true;
    		}else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
    			moveRobot(Direction.RIGHT);
    			return true;
    		}else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
    			moveRobot(Direction.DOWN);
    			return true;
    		}else if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER){
    			// debug endGameAnimation();
    			// return true;
    		}
    	}
    	return false;
    }
    
    /**
     * Play the endgame animation and move game to next state.
     */
    public void endGameAnimation(){
    	
    	setContentView(R.layout.endgame);
    	
    	inputMode = InputMode.NO_INPUT;
    	
    	TextView robot = (TextView)findViewById(R.id.anim_robot);
    	TextView kitten = (TextView)findViewById(R.id.anim_kitten);
    	kitten.setText(this.kitten.character);
    	kitten.setTextColor(this.kitten.color);
    	TextView heart = (TextView)findViewById(R.id.anim_heart);
    	TextView heartAbove = (TextView)findViewById(R.id.anim_heart_above);
    	TextView win = (TextView)findViewById(R.id.anim_wintext);
    	TextView win2 = (TextView)findViewById(R.id.anim_wintext2);
    	
    	Animation zoomAnim = AnimationUtils.loadAnimation(this, R.anim.zoomfade);
    	Animation rightAnim =  AnimationUtils.loadAnimation(this, R.anim.moveright);
    	Animation leftAnim =  AnimationUtils.loadAnimation(this, R.anim.moveleft);
    	Animation fadeInAnim =  AnimationUtils.loadAnimation(this, R.anim.fadeindelay);
    	Animation fadeInLaterAnim =  AnimationUtils.loadAnimation(this, R.anim.fadeinlater);
    	Animation moveUpAnim =  AnimationUtils.loadAnimation(this, R.anim.moveup);
    	
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
    	
    	// this is an attempt to prevent flashing
    	// of the animation before they are shown.
    	// Keep your pants on, ladies.
    	win.setVisibility(TextView.VISIBLE);
    	win2.setVisibility(TextView.VISIBLE);
    	heart.setVisibility(TextView.VISIBLE);
    	heartAbove.setVisibility(TextView.VISIBLE);
    	
    	// re-enable input once animation is done playing.
    	Runnable r = new Runnable(){
			public void run() {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// *yawn* I was trying to get some sleep!
				}
				inputMode = InputMode.ANY_KEY;
			}
    	};
    	new Thread(r).start();
    }
}