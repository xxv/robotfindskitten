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
import android.graphics.Color;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class robotfindskitten extends Activity {
	public enum InputMode { ANY_KEY, DIRECTIONAL };
	public enum Direction { UP, RIGHT, DOWN, LEFT };
	
	// these define the playing field. Mostly useful for detecting where things are.
	private List<Thing> screen = new ArrayList<Thing>();
	private Thing robot;
	private Thing kitten;
	
	private int cellWidth;
	private int cellHeight;
	private int width;
	private int height;
	
	private Random rand = new Random();
	
	private List<String> messages = new ArrayList<String>();
	private String validChars;
	
	private InputMode inputMode = InputMode.ANY_KEY;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //setContentView(R.layout.intro);
        
        // load all the characters
       validChars = "";
        for (int i = 0x21; i < 127; i++){
        	char c = Character.toChars(i)[0];
        	if (! Character.isWhitespace(c) && ! Character.isISOControl(c) && c != '#'){
        		validChars += c;
        	}
        }
        System.err.println("valid characters: " + validChars);

        loadMessages();
        
    }
    
    private void initializeScreen(){
    	RelativeLayout screenLayout = (RelativeLayout)findViewById(R.id.screen);
    	TextView robotText = (TextView)findViewById(R.id.robot);
    	
    	cellWidth = robotText.getWidth();
    	width = screenLayout.getWidth()/cellWidth;
    	cellHeight = robotText.getHeight();
    	height = screenLayout.getHeight()/cellHeight;
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
    	
    	if (t.representation != null){
    		((TextView)t.representation).setTextColor(
    				Color.argb(255, rand.nextInt(256),rand.nextInt(256),rand.nextInt(256))); 
    	}
    }
   
    public char randomChar(){
    	return validChars.charAt(rand.nextInt(validChars.length()));
    }
    
    public void addThings(){
    	
    	RelativeLayout screenLayout = (RelativeLayout)findViewById(R.id.screen);
    	robot = new Thing(ThingType.ROBOT);
    	robot.representation = findViewById(R.id.robot);
    	placeThing(robot);
    	updateThing(robot);
    	
    	kitten = new Thing(ThingType.KITTEN);
    	kitten.representation = findViewById(R.id.kitten);
    	((TextView)kitten.representation).setText(randomChar());
    	randomizeThingColor(kitten);
    	placeThing(kitten);
    	updateThing(kitten);
    	
    	// add in the other things that aren't kitten
    	for(int i = 0; i < 20; i++){
    		Thing something = new Thing(ThingType.NKI);
    		
    		// give that something a unique message
    		while (something.message == null){
    			something.message =  messages.get(rand.nextInt(messages.size()));
    			for (Thing someOtherThing: screen){
    				if (something.message == someOtherThing.message){
    					something.message = null;
    					break;
    				}
    			}
    		}
    		something.representation = new TextView(screenLayout.getContext());
    		((TextView)something.representation).setText(randomChar());
    		placeThing(something);
    		updateThing(something);
    	}
    }
    
    public void updateThing(Thing t){
    	TextView tv = (TextView)t.representation;
    	
    	tv.setPadding(t.x * cellWidth, t.y * cellHeight, 
    			tv.getPaddingRight(), tv.getPaddingBottom());
    	
    }
    
    /**
     * Places a Thing randomly and non-overlappingly on the screen.
     * 
     * @param t
     */
    public void placeThing(Thing t){
    	Random rand = new Random();
    	t.x = -1; // unplaced
    	
    	while (t.x == -1){
    		int x = rand.nextInt(width);
    		int y = rand.nextInt(height);
        	t.x = x;
    		t.y = y;
    		// make sure we don't place a thing on top of something
	    	for (Thing something: screen){
	    		if (something.x == t.x && something.y == t.y){
	    			t.x = -1;
	    			break;
	    		}
	    	}
    	}
    	screen.add(t);
    }
    
    public void moveRobot(Direction d){
    	TextView robotText = (TextView)findViewById(R.id.robot);
    	switch (d){
    	case UP:
    		if (robot.y == 0) break;
    		robot.y--;
    		break;
    	
    	}
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
    		setContentView(R.layout.main);
            initializeScreen();
            addThings();
    		return true;
    	}else if (inputMode == InputMode.DIRECTIONAL){
    		if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
    			
    		}
    	}
    	return false;
    }
}