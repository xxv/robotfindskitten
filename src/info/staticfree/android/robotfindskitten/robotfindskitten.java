package info.staticfree.android.robotfindskitten;

import info.staticfree.android.robotfindskitten.Thing.ThingType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.content.res.Resources.Theme;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;

public class robotfindskitten extends Activity {
	public enum InputMode { ANY_KEY, DIRECTIONAL };
	public enum Direction { UP, RIGHT, DOWN, LEFT };
	
	// these define the playing field. Mostly useful for detecting where things are.
	
	private Random rand = new Random();
	
	private RFKView rfkView;
	private Thing robot;
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
        
        addThings();
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
    
    public void randomizeThingColor(Thing t){
    	t.color = Color.argb(255, rand.nextInt(256),rand.nextInt(256),rand.nextInt(256)); 
    }
   
    public char randomChar(){
    	return validChars.charAt(rand.nextInt(validChars.length()));
    }
    
    public void addThings(){
    	robot = new Thing(ThingType.ROBOT);    	
    	rfkView.addRobot(robot);
    	
    	Thing kitten = new Thing(ThingType.KITTEN);
    	kitten.character = "" + randomChar();
    	randomizeThingColor(kitten);
    	rfkView.addKitten(kitten);
    	
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
    			recentMessage = Toast.makeText(this, "Way to go, robot! You found kitten!", Toast.LENGTH_LONG);
    			recentMessage.show();
    			
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
    public boolean onTrackballEvent(MotionEvent event) {
    	// TODO Auto-generated method stub
    	return super.onTrackballEvent(event);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	// TODO Auto-generated method stub
    	return super.onTouchEvent(event);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (inputMode == InputMode.ANY_KEY){

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
    		}
    	}
    	return false;
    }
}