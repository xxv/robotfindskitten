package info.staticfree.android.robotfindskitten;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class RFKView extends ViewGroup {

    private final List<Thing> things = new ArrayList<Thing>();

    private int width = -1;
    private int height;

    private final Random rand = new Random();

    private View mRobot;

    private TextView mNki;

    public RFKView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs);
    }

    public RFKView(Context context) {
        super(context);

        init(context, null);
    }

    private void init(Context context, AttributeSet attrs) {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mRobot == null) {
            mRobot = findViewById(R.id.robot);
        }
        if (mNki == null) {
            mNki = (TextView) findViewById(R.id.nki);
        }

        mRobot.measure(MeasureSpec.makeMeasureSpec(widthMeasureSpec, MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(heightMeasureSpec, MeasureSpec.AT_MOST));

        mNki.measure(MeasureSpec.makeMeasureSpec(widthMeasureSpec, MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(heightMeasureSpec, MeasureSpec.AT_MOST));

        width = (int) Math.floor((double) getMeasuredWidth() / mNki.getMeasuredWidth());

        height = getMeasuredHeight() / mNki.getMeasuredHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // super.onDraw(canvas);

        // canvas.drawPaint(background);

        for (final Thing thing : things) {
            if (thing.x == -1) {
                placeThing(thing);
            }
            // final Paint paint;
            if (thing.type == Thing.ROBOT) {
                final int robotW = mRobot.getMeasuredWidth();
                final int robotH = mRobot.getMeasuredHeight();
                mRobot.layout(0, 0, robotW, robotH);

                // mRobot.layout(thing.x * robotW, thing.y * robotH, (thing.x + 1) * robotW,
                // (thing.y + 1) * robotH);
                canvas.save();
                canvas.translate(thing.x * robotW, thing.y * robotH);
                mRobot.draw(canvas);
                canvas.restore();

            } else {
                mNki.setText(thing.character);
                mNki.setTextColor(thing.color);

                mNki.measure(MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.AT_MOST),
                        MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST));
                final int nkiW = mNki.getMeasuredWidth();
                final int nkiH = mNki.getMeasuredHeight();
                mNki.layout(0, 0, nkiW, nkiH);

                canvas.save();
                canvas.translate(thing.x * nkiW, thing.y * nkiH);

                mNki.draw(canvas);
                canvas.restore();
            }
        }
    }

    /**
     * Places a Thing randomly and non-overlappingly on the screen. onDraw must be called first to
     * size the screen.
     *
     * @param t
     */
    public void placeThing(Thing t) {
        t.x = -1; // unplaced

        while (t.x == -1) {
            final int x = rand.nextInt(width);
            final int y = rand.nextInt(height);
            t.x = x;
            t.y = y;
            // make sure we don't place a thing on top of something
            for (final Thing something : things) {
                if (something == t) {
                    continue; // skip ourselves
                }
                if (something.x == t.x && something.y == t.y) {
                    t.x = -1;
                    break;
                }
            }
        }
    }

    public void addThing(Thing thing) {
        things.add(thing);
    }

    public Thing thingAt(int x, int y) {
        for (final Thing thing : things) {
            if (thing.x == x && thing.y == y) {
                return thing;
            }
        }
        return null;
    }

    public int getBoardWidth() {
        return width;
    }

    public int getBoardHeight() {
        return height;
    }

    public List<Thing> getThings() {
        return things;
    }

    public void clearBoard() {
        things.clear();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // we fake this, so we don't duplicate a ton of views.
    }
}
