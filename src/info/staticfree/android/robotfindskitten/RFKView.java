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

    private final List<Thing> mThings = new ArrayList<Thing>();

    private int mWidth = -1;
    private int mHeight;

    private final Random rand = new Random();

    private View mRobot;

    private TextView mNki;

    public RFKView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public RFKView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs);
    }

    public RFKView(Context context) {
        super(context);

        init(context, null);
    }

    private void init(Context context, AttributeSet attrs) {
        setWillNotDraw(false);
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

        final int measuredWidth = (int) Math.floor((double) getMeasuredWidth()
                / mNki.getMeasuredWidth());

        final int measuredHeight = (int) Math.floor((double) getMeasuredHeight()
                / mNki.getMeasuredHeight());
        boolean shrunk = false;
        if (mWidth == -1) {
            mWidth = measuredWidth;
            mHeight = measuredHeight;
        } else {
            // growing is ok.
            if (measuredWidth > mWidth) {
                mWidth = measuredWidth;
            } else if (measuredWidth < mWidth) {

                mWidth = measuredWidth;
                shrunk = true;
            }

            if (measuredHeight > mHeight) {
                mHeight = measuredHeight;

            } else if (measuredHeight < mHeight) {
                mHeight = measuredHeight;
                shrunk = true;
            }

            if (shrunk) {
                for (final Thing thing : mThings) {
                    if (thing.x >= measuredWidth || thing.y >= measuredHeight) {
                        thing.x = -1;
                        thing.y = -1;
                        placeThing(thing, measuredWidth, measuredHeight);
                    }
                }
            }
        }
        setMeasuredDimension(mWidth * mNki.getMeasuredWidth(), mHeight * mNki.getMeasuredHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (final Thing thing : mThings) {
            if (thing.x == -1) {
                placeThing(thing, mWidth, mHeight);
            }
            // final Paint paint;
            if (thing.type == Thing.ROBOT) {
                final int robotW = mRobot.getWidth();
                final int robotH = mRobot.getHeight();

                canvas.save();
                canvas.translate(thing.x * robotW, thing.y * robotH);
                canvas.clipRect(0, 0, robotW, robotH);
                mRobot.draw(canvas);
                canvas.restore();

            } else {
                mNki.setText(thing.character);
                mNki.setTextColor(thing.color);

                final int nkiW = mNki.getWidth();
                final int nkiH = mNki.getHeight();

                canvas.save();
                canvas.translate(thing.x * nkiW, thing.y * nkiH);
                canvas.clipRect(0, 0, nkiW, nkiH);
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
    public void placeThing(Thing t, int width, int height) {
        t.x = -1; // unplaced

        while (t.x == -1) {
            final int x = rand.nextInt(width);
            final int y = rand.nextInt(height);
            t.x = x;
            t.y = y;
            // make sure we don't place a thing on top of something
            for (final Thing something : mThings) {
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
        mThings.add(thing);
    }

    public Thing thingAt(int x, int y) {
        for (final Thing thing : mThings) {
            if (thing.x == x && thing.y == y) {
                return thing;
            }
        }
        return null;
    }

    public int getBoardWidth() {
        return mWidth;
    }

    public int getBoardHeight() {
        return mHeight;
    }

    public List<Thing> getThings() {
        return mThings;
    }

    public void clearBoard() {
        mThings.clear();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int nkiW = mNki.getMeasuredWidth();
        final int nkiH = mNki.getMeasuredHeight();
        mNki.layout(0, 0, nkiW, nkiH);

        final int robotW = mRobot.getMeasuredWidth();
        final int robotH = mRobot.getMeasuredHeight();
        mRobot.layout(0, 0, robotW, robotH);
    }
}
