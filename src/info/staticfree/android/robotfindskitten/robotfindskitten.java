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
	
	// these define the playing field. Mostly useful for detecting where things are.
	
	private Random rand = new Random();
	
	private RFKView rfkView;
	
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
    	
    	RelativeLayout screenLayout = (RelativeLayout)findViewById(R.id.screen);
    	Thing robot = new Thing(ThingType.ROBOT);    	
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
    			
    		}
    		return true;
    	}
    	return false;
    }
}