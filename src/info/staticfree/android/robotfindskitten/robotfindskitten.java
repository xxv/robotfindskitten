package info.staticfree.android.robotfindskitten;

import info.staticfree.android.robotfindskitten.Thing.ThingType;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class robotfindskitten extends Activity {
	public enum InputMode { ANY_KEY, DIRECTIONAL };
	public enum Direction { UP, RIGHT, DOWN, LEFT };
	
	// these define the playing field. Mostly useful for detecting where things are.
	
	private Random rand = new Random();
	
	private RFKView rfkView;
	private Thing robot;
	private Thing kitten;
	private Toast recentMessage;
	private Thing recentCollision;
	
	private List<String> messages = new ArrayList<String>();
	private String validChars;
	
	private InputMode inputMode = InputMode.DIRECTIONAL;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // load all the characters
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

        rfkView = (RFKView)findViewById(R.id.rfk);
        
        resetGame();
    }
    
    
    
    public void loadMessages(){
    	InputStream is = getResources().openRawResource(R.raw.messages);
    	
    	StringBuilder jsonString = new StringBuilder();
    	try{
    		
	    	for (BufferedReader isReader = new BufferedReader(new InputStreamReader(is), 16000); isReader.ready();){
	    		jsonString.append(isReader.readLine());
	    	}
	    	
	    	JSONArray msgJson = new JSONArray(jsonString.toString());
	    	
	    	for (int i = 0; i < msgJson.length(); i++){
	    		messages.add(msgJson.getString(i));
	    	}
    	}catch (Exception e){
    		Toast.makeText(this, "error reading messages: "+e.toString(), 
    				Toast.LENGTH_LONG).show();
    	}
    }
    
    public void resetGame(){
    	rfkView.clearBoard();
    	
        addThings();
    }
    
    public void randomizeThingColor(Thing t){
    	int base = 30;
    	t.color = Color.argb(255, 
    			base + rand.nextInt(256 - base),
    			base + rand.nextInt(256 - base),
    			base + rand.nextInt(256 - base)); 
    }
   
    public char randomChar(){
    	return validChars.charAt(rand.nextInt(validChars.length()));
    }
    
    public void addThings(){
    	robot = new Thing(ThingType.ROBOT);    	
    	rfkView.addThing(robot);
    	
    	kitten = new Thing(ThingType.KITTEN);
    	kitten.character = "" + randomChar();
    	randomizeThingColor(kitten);
    	rfkView.addThing(kitten);
    	
    	// add in the other things that aren't kitten
    	for(int i = 0; i < 20; i++){
    		Thing something = new Thing(ThingType.NKI);
        	something.character = "" + randomChar();
        	randomizeThingColor(something);
        	
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

    public boolean isCollision(int x, int y){
    	Thing thing = rfkView.thingAt(x, y);

    	if (thing != null){
        	// we've already handled this collision, no need to repeat.
    		if (thing == recentCollision) return true;
    		recentCollision = thing;
    		
    		if (thing.type == ThingType.KITTEN){
    			if (recentMessage != null ) recentMessage.cancel();
    			endGameAnimation();
    			
    		}else if (thing.type == ThingType.NKI){
    			if (recentMessage != null ) recentMessage.cancel();
    			recentMessage = Toast.makeText(this, thing.message, Toast.LENGTH_LONG);
    			recentMessage.show();
    		}
    		return true;
    	}else {
    		recentCollision = null;
    		return false;
    	}
    }
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (inputMode == InputMode.ANY_KEY){
    		if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER){
    		resetGame();
    		
    		setContentView(R.layout.main);
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
    			endGameAnimation();
    		}
    	}
    	return false;
    }
    
    /**
     * Play the endgame animation and move game to next state.
     */
    public void endGameAnimation(){
    	setContentView(R.layout.endgame);
    	
    	inputMode = InputMode.ANY_KEY;
    	
    	TextView robot = (TextView)findViewById(R.id.anim_robot);
    	TextView kitten = (TextView)findViewById(R.id.anim_kitten);
    	kitten.setText(this.kitten.character);
    	kitten.setTextColor(this.kitten.color);
    	TextView win = (TextView)findViewById(R.id.anim_wintext);
    	TextView heart = (TextView)findViewById(R.id.anim_heart);
    	TextView heartAbove = (TextView)findViewById(R.id.anim_heart_above);
    	
    	Animation zoomAnim = AnimationUtils.loadAnimation(this, R.anim.zoomfade);
    	Animation rightAnim =  AnimationUtils.loadAnimation(this, R.anim.moveright);
    	Animation leftAnim =  AnimationUtils.loadAnimation(this, R.anim.moveleft);
    	Animation fadeInAnim =  AnimationUtils.loadAnimation(this, R.anim.fadein);
    	Animation moveUpAnim =  AnimationUtils.loadAnimation(this, R.anim.moveup);
    	
    	// this is supposed to be set via the XML, but it seems to be ignored there.
    	leftAnim.setFillAfter(true);
    	rightAnim.setFillAfter(true);
    	zoomAnim.setFillAfter(true);
    	fadeInAnim.setFillAfter(true);
    	moveUpAnim.setFillAfter(true);
    	
    	robot.startAnimation(rightAnim);
    	heart.startAnimation(zoomAnim);
    	heartAbove.startAnimation(moveUpAnim);
    	kitten.startAnimation(leftAnim);
    	win.startAnimation(fadeInAnim);
    	
    	win.setVisibility(TextView.VISIBLE);
    	heart.setVisibility(TextView.VISIBLE);
    	heartAbove.setVisibility(TextView.VISIBLE);

    	
    }
}