package info.staticfree.android.robotfindskitten;

import java.util.Random;

import android.graphics.Color;

public class Thing {
	public enum ThingType {ROBOT, KITTEN, NKI};
	
	// getters/setters are for wusses. Make it public; live on the edge.
	public int x = -1;
	public int y = -1;
	public String message;
	public ThingType type;
	public String character;
	public int color;
	
	private final static Random rand = new Random();
	
	public Thing(ThingType type){
		this.type = type;
		if (type == ThingType.ROBOT){
			character = "#";
		}else{
			randomizeColor();
		}
	}
	
	public void setLocation(int x, int y){
		this.x = x;
		this.y = y;
	}
	
    /**
     * Gives the Thing a random color. 
     * @param t
     */
    public void randomizeColor(){
    	 // minimum possible color. prevents thing from blending into background.
    	int base = 30;
    	color = Color.argb(255, 
    			base + rand.nextInt(256 - base),
    			base + rand.nextInt(256 - base),
    			base + rand.nextInt(256 - base)); 
    }
}
