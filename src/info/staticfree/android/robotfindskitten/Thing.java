package info.staticfree.android.robotfindskitten;

import java.util.Random;

import android.graphics.Color;

public class Thing {
	public final static int
		NKI    = 0,
		KITTEN = 1,
		ROBOT  = 2;

	//minimum possible color. prevents thing from blending into background.
	private static final int CONTRAST_SKEW = 80;

	// getters/setters are for wusses. Make it public; live on the edge.
	public int x = -1;
	public int y = -1;
	public String message;
	public int type;
	public String character;
	public int color;

	private final static Random rand = new Random();

	public Thing(int type){
		this.type = type;
		if (type == ROBOT){
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

    	color = Color.argb(255,
    			CONTRAST_SKEW + rand.nextInt(256 - CONTRAST_SKEW),
    			CONTRAST_SKEW + rand.nextInt(256 - CONTRAST_SKEW),
    			CONTRAST_SKEW + rand.nextInt(256 - CONTRAST_SKEW));
    }

    /**
     * Gives the Thing a random color.
     * @param t
     */
    public void randomizeColorLightBg(){
    	color = Color.argb(255,
    			rand.nextInt(256 - CONTRAST_SKEW),
    			rand.nextInt(256 - CONTRAST_SKEW),
    			rand.nextInt(256 - CONTRAST_SKEW));
    }
}
