package info.staticfree.android.robotfindskitten;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

public class RFKView extends View {

	private final List<Thing> things = new ArrayList<Thing>();

	private Paint robotPaint;
	private Paint robotBg;
	private final Paint background = new Paint();
	private final Paint thingPaint = new Paint();

	private int cellWidth = -1;
	private float cellHeight = 16;

	private int width = -1;
	private int height;

	private final Random rand = new Random();

	public RFKView(Context context, AttributeSet attrs){
		super(context, attrs);
		initPaint();
	}

	public RFKView(Context context) {
		super(context);

		initPaint();

	}

	private void initPaint(){
		final Resources res = getContext().getResources();
		final float scale = res.getDisplayMetrics().density;
		cellHeight = (cellHeight * scale);
		robotPaint = new Paint();
		robotPaint.setColor(res.getColor(R.color.robot_foreground));
		robotPaint.setTypeface(Typeface.MONOSPACE);
		robotPaint.setTextSize(cellHeight);
		robotPaint.setAntiAlias(true);
		robotPaint.setSubpixelText(true);
		robotPaint.setTextAlign(Paint.Align.LEFT);


		robotBg = new Paint();
		robotBg.setColor(res.getColor(R.color.robot_background));

		thingPaint.setTypeface(Typeface.MONOSPACE);
		thingPaint.setTextSize(cellHeight);
		thingPaint.setAntiAlias(true);
		thingPaint.setSubpixelText(true);
		thingPaint.setTextAlign(Paint.Align.LEFT);

		cellWidth = (int)robotPaint.measureText("#");

		background.setColor(res.getColor(R.color.ether_background));
	}

	private final Rect mRect = new Rect();

	@Override
	protected void onDraw(Canvas canvas) {
		if (width == -1){
			width = getWidth()/cellWidth - 1;
			height = (int)(getHeight()/cellHeight - 1);
		}

		canvas.drawPaint(background);

		for (final Thing thing: things){
			if (thing.x == -1){
				placeThing(thing);
			}
			Paint paint;
			if (thing.type == Thing.ROBOT){
				paint = robotPaint;
				mRect.left = (thing.x) * cellWidth;
				mRect.top = (int)((thing.y) * cellHeight + 2);
				mRect.right = (thing.x +1) * cellWidth;
				mRect.bottom = (int)((thing.y + 1) * cellHeight + 2);
				canvas.drawRect(mRect, robotBg);
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
    		final int x = rand.nextInt(width);
    		final int y = rand.nextInt(height);
        	t.x = x;
    		t.y = y;
    		// make sure we don't place a thing on top of something
	    	for (final Thing something: things){
	    		if (something == t) {
					continue; // skip ourselves
				}
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
		for (final Thing thing: things){
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
