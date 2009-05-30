package info.staticfree.android.robotfindskitten;

import info.staticfree.android.robotfindskitten.Thing.ThingType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

public class RFKView extends View {
	
	private List<Thing> things = new ArrayList<Thing>();
	
	private Paint robotPaint;
	private Paint robotBg;
	private Paint background = new Paint();
	private Paint thingPaint = new Paint();
	
	private int cellWidth = -1;
	private int cellHeight = 16;
	
	private int width = -1;
	private int height;
	
	private Random rand = new Random();
	
	public RFKView(Context context, AttributeSet attrs){
		super(context, attrs);
		initPaint();
	}
	
	public RFKView(Context context) {
		super(context);

		initPaint();

	}

	private void initPaint(){
		robotPaint = new Paint();
		robotPaint.setARGB(255, 0, 0, 0);
		robotPaint.setTypeface(Typeface.MONOSPACE);
		robotPaint.setTextSize(cellHeight);
		robotPaint.setAntiAlias(true);
		robotPaint.setSubpixelText(true);
		robotPaint.setTextAlign(Paint.Align.RIGHT);
		

		robotBg = new Paint();
		robotBg.setARGB(255, 255, 0, 0);
		
		thingPaint.setTypeface(Typeface.MONOSPACE);
		thingPaint.setTextSize(cellHeight);
		thingPaint.setAntiAlias(true);
		thingPaint.setSubpixelText(true);
		thingPaint.setTextAlign(Paint.Align.RIGHT);
		
		cellWidth = (int)robotPaint.measureText("#");
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		if (width == -1){
			width = getWidth()/cellWidth;
			height = getHeight()/cellHeight;
		}

		background.setARGB(255, 0, 0, 0);
		getContext().getTheme();
		
		canvas.drawPaint(background);
		
		for (Thing thing: things){
			if (thing.x == -1){
				placeThing(thing);
			}
			Paint paint;
			if (thing.type == ThingType.ROBOT){
				paint = robotPaint;
				Rect r = new Rect((thing.x-1) * cellWidth, (thing.y) * cellHeight + 2, thing.x * cellWidth, (thing.y + 1) * cellHeight + 2);
				canvas.drawRect(r, robotBg);
			}else{
				paint = thingPaint;
				paint.setColor(thing.color);

			}

			canvas.drawText(thing.character, thing.x * cellWidth, (1+thing.y) * cellHeight, paint);
		}
	}
	
    /**
     * Places a Thing randomly and non-overlappingly on the screen.
     * onDraw must be called first to size the screen. 
     * 
     * @param t
     */
    public void placeThing(Thing t){
    	t.x = -1; // unplaced
    	
    	while (t.x == -1){
    		int x = rand.nextInt(width);
    		int y = rand.nextInt(height);
        	t.x = x;
    		t.y = y;
    		// make sure we don't place a thing on top of something
	    	for (Thing something: things){
	    		if (something == t) continue; // skip ourselves
	    		if (something.x == t.x && something.y == t.y){
	    			t.x = -1;
	    			break;
	    		}
	    	}
    	}
    }
    
	public void addThing(Thing thing){
		things.add(thing);
	}
	
	public Thing thingAt(int x, int y){
		for (Thing thing: things){
			if (thing.x == x && thing.y == y){
				return thing;
			}
		}
		return null;
	}

	public int getBoardWidth(){
		return width;
	}
	
	public int getBoardHeight(){
		return height;
	}
	
	public List<Thing> getThings(){
		return things;
	}

	public void clearBoard(){
		things.clear();
	}
}
