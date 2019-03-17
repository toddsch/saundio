package com.subwranglers.pucker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import java.util.*;
import java.util.function.Consumer;

import static android.view.MotionEvent.*;

public class Pucker extends FrameLayout implements View.OnTouchListener {

    private static final float[] DEFAULT_DISTANCE = {0.0F, 0.0F};

    /**
     * The length of time the puck is considered "clicked" if the {@link MotionEvent#ACTION_UP} fires.
     */
    public static final long CLICK_DURATION_THRESHOLD = 100;

    /**
     * The distance the puck can be moved from origin while still considered "clickable" during the
     * {@link MotionEvent#ACTION_UP} event.
     */
    public static final int CLICK_DISTANCE_THRESHOLD = 15;

    /*

        Coordinates and Translator functions

     */

    public static final Translator FN_ANY_AXIS = new Translator() {
        @Override
        public void accept(Pucker pucker, MotionEvent motionEvent) {
            pucker.movePuckX(motionEvent.getRawX());
            pucker.movePuckY(motionEvent.getRawY());
        }
    };

    public static final Translator FN_X_AXIS_ONLY = new Translator() {
        @Override
        public void accept(Pucker pucker, MotionEvent event) {
            pucker.movePuckX(event.getRawX());
        }
    };

    public static final Translator FN_Y_AXIS_ONLY = new Translator() {
        @Override
        public void accept(Pucker pucker, MotionEvent event) {
            pucker.movePuckY(event.getRawY());
        }
    };

    /**
     * The {@link View} that is meant to be dragged around within the bounds of this {@link Pucker}.
     */
    View puck;

    /**
     * The function used to process coordinates given from {@link MotionEvent MotionEvents} before applying them to the
     * puck.
     */
    protected Translator puckTranslator = FN_ANY_AXIS;

    // The "half"-values of this class's width and height
    protected int hWidth;
    protected int hHeight;

    // The "half"-values of the puck's width and height
    protected int hpWidth;
    protected int hpHeight;

    // The distance from pointer to puck on ACTION_DOWN. Value set to DEFAULT_DISTANCE on ACTION_UP.
    protected float[] initialD = DEFAULT_DISTANCE;

    /*

        Boundaries

     */

    protected final TreeMap<Integer, BoundaryListener> boundaries = new TreeMap<>(new Comparator<Integer>() {
        @Override
        public int compare(Integer o1, Integer o2) {
            return o1.compareTo(o2);
        }
    });

    /*

        Options

     */

    protected int samplingRate = 20; // How often BoundaryListener#moveInBoundary() should be called, in milliseconds
    protected int maxRadius = Integer.MAX_VALUE; // Default to largest positive INT

    /*

        Worker Fields

     */

    protected long samplingThreshold;
    protected PuckListener puckListener = new PuckListener() { // Default no-op so we don't have to null-check
        @Override
        public void onPuckClicked(Pucker pucker) {
        }

        @Override
        public void onPuckMoved(Pucker pucker, float[] distanceFromOrigin) {
        }

        @Override
        public void onPuckReleased(Pucker pucker, float[] distanceFromOrigin) {
        }
    };

    public Pucker(Context context) {
        super(context);
        setup();
    }

    public Pucker(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public Pucker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup();
    }

    private void setup() {
        samplingThreshold = System.currentTimeMillis() + samplingRate;
    }

    protected void enforceChildren() {
        if (getChildCount() != 1)
            // We could just wrap the children of this group in a FrameLayout, but that quickly becomes "undefined"
            // behaviour and could create unnecessary logical bugs. If someone wants a full layout as the puck,
            // let them define it their way.
            throw new UnsupportedOperationException("A Pucker can have exactly one child element");
        else if (puck == null)
            setPuck(getChildAt(0));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        enforceChildren();

        // Update the half-values for widths/heights
        updateHalves();
    }

    private void updateHalves() {
        hWidth = getWidth() / 2;
        hHeight = getHeight() / 2;
        hpWidth = puck.getWidth() / 2;
        hpHeight = puck.getHeight() / 2;
    }

    /*

        Setters

     */

    public Pucker setPuck(View puck) {
        this.puck = puck;
        updateHalves();
        this.puck.setOnTouchListener(this);
        return this;
    }

    public Pucker setTranslator(Translator translator) {
        puckTranslator = translator;
        return this;
    }

    public Pucker setPuckListener(PuckListener puckListener) {
        this.puckListener = puckListener;
        return this;
    }

    /*

        Getters

     */

    public final View getPuck() {
        return puck;
    }

    public final int getMaxRadius() {
        return maxRadius;
    }

    public int getHalfWidth() {
        return hWidth;
    }

    public int getHalfHeight() {
        return hHeight;
    }

    public int getHalfPuckWidth() {
        return hpWidth;
    }

    public int getHalfPuckHeight() {
        return hpHeight;
    }

    /**
     * Gets the cartesian distance from the center of the puck to its origin.
     *
     * @return distance from puck to origin
     */
    public float calcDistFromOrigin() {
//        return (float) Math.sqrt(
//                Math.pow(hWidth - (puck.getX() + hpWidth), 2) +
//                        Math.pow(hHeight - (puck.getY() + hpHeight), 2)
//        );
        return calcPuckDistanceFromXY(hWidth, hHeight);
    }

    public float calcPuckDistanceFromXY(float x, float y) {
        double dx = Math.pow(x - (puck.getX() + hpWidth), 2);
        double dy = Math.pow(y - (puck.getY() + hpHeight), 2);
        return (float) Math.sqrt(dx + dy);
    }

    public float[] getPuckDistanceFromOrigin() {
        return new float[]{puck.getX() - hWidth + hpWidth, puck.getY() - hHeight + hpHeight};
    }

    public final Map<Integer, BoundaryListener> getBoundaries() {
        return boundaries;
    }

    /*

        Mutators

     */

    /**
     * Directly sets the Puck's X coordinate with respect to the initial touch-point if the puck is currently being
     * dragged. Does not invoke the function provided by {@link #setTranslator(Translator)}.
     *
     * @param x the horizontal coordinate to move the puck to
     */
    public void movePuckX(float x) {
        float newX = x - initialD[0];
        if (Math.sqrt(Math.pow(newX - hWidth + hpWidth, 2)) <= maxRadius)
            puck.setX(newX);
    }

    /**
     * Directly sets the Puck's Y coordinate with respect to the initial touch-point if the puck is currently being
     * dragged. Does not invoke the function provided by {@link #setTranslator(Translator)}.
     *
     * @param y the horizontal coordinate to move the puck to
     */
    public void movePuckY(float y) {
        float newY = y - initialD[1];
        if (Math.sqrt(Math.pow(newY - hHeight + hpHeight, 2)) <= maxRadius)
            puck.setY(newY);
    }

    /**
     * Accepts a {@link MotionEvent} and provides it to the function provided to the {@link #setTranslator(Translator)}
     * function. If no function has been given, the default {@link #FN_ANY_AXIS} is used.
     *
     * @param event the {@link MotionEvent} to take coordinates and move the puck with
     */
    public void movePuck(MotionEvent event) {
        puckTranslator.accept(this, event);
    }

    public void moveToCenter() {
        // Subtract half of puck's width + height to properly center it
        puck.setX(hWidth - hpWidth);
        puck.setY(hHeight - hpHeight);
    }

    private void updateSamplingThreshold() {
        samplingThreshold = System.currentTimeMillis() + samplingRate;
    }

    public void putBoundary(int radius, BoundaryListener action) {
        boundaries.put(radius, action);
    }

    /*

        Callbacks

     */

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction()) {
            case ACTION_DOWN:
                // Collect the distance we're starting the touch event from
                initialD = new float[]{event.getRawX() - puck.getX(), event.getRawY() - puck.getY()};
                break;
            case ACTION_MOVE:
                float currentX = puck.getX();
                float currentY = puck.getY();
                movePuck(event);

                if (System.currentTimeMillis() > samplingThreshold) {
                    puckListener.onPuckMoved(this, getPuckDistanceFromOrigin());

                    final int KEY = (int) calcPuckDistanceFromXY(currentX, currentY);
                    final Map.Entry<Integer, BoundaryListener> entry = boundaries.floorEntry(KEY);
                    // TODO: 15/01/19 invoke the onBoundaryCrossed() method such that it only fires once per boundary crossing
//                    if (entry != null) {
//                        entry.getValue().onBoundaryCrossed(
//                                this,
//                                entry.getKey(),
//                                (int) (puck.getX() - currentX),
//                                (int) (puck.getY() - currentY)
//                        );
//                    }

                    updateSamplingThreshold();
                }
                break;
            case ACTION_UP:
                initialD = DEFAULT_DISTANCE;

                if (event.getEventTime() - event.getDownTime() < CLICK_DURATION_THRESHOLD
                        && calcDistFromOrigin() < CLICK_DISTANCE_THRESHOLD) {
                    puckListener.onPuckClicked(this);
                    puck.performClick();
                } else {
                    puckListener.onPuckReleased(this, getPuckDistanceFromOrigin());
                }

                // When the snapToCenter flag is properly implemented, it goes here
                moveToCenter();

                break;
        }
        return true;
    }

    /*

        Options

     */

    /*
        For now, always force snap-to-center behaviour. Otherwise, additional logic needs to be implemented to fine-tune
        how "clicking" the puck works. With snap to center, we can reduce accidental "clicks" by checking if the button
        is <z> amount close to the origin alongside the duration of the press. Allowing clicks away from the origin
        needs better handling-logic.
     */
//    /**
//     * Change behaviour of puck on release after dragging. Defaults to <tt>true</tt>.
//     *
//     * @param snapToCenter <tt>true</tt> to move the puck back to the Pucker's center after dragging, false to leave it
//     *                     where it is.
//     * @return current instance of {@link Pucker} for chaining calls
//     */
//    public Pucker setSnapToCenter(boolean snapToCenter) {
//        this.snapToCenter = snapToCenter;
//        return this;
//    }

    public void setSamplingRate(int samplingRate) {
        this.samplingRate = samplingRate;
        updateSamplingThreshold();
    }

    /**
     * The maximum distance the puck is allowed to move from its origin.
     *
     * @param maxRadius maximum distance in pixels
     */
    public Pucker setMaxRadius(int maxRadius) {
        this.maxRadius = maxRadius;
        return this;
    }

    /*

        Puck Interfaces

     */

    public interface Translator {
        void accept(Pucker puck, MotionEvent event);
    }

    public interface PuckListener {
        void onPuckClicked(Pucker pucker);

        void onPuckMoved(Pucker pucker, float[] distanceFromOrigin);

        void onPuckReleased(Pucker pucker, float[] distanceFromOrigin);
    }

    public interface BoundaryListener {
        /**
         * <p>Called only when the puck crosses a boundary.</p>
         * <p>Boundaries are set as a radius from the puck's origin.</p>
         * @param pucker the {@link Pucker} responsible for the event
         * @param boundaryRadiusId the area of the boundary the event was invoked from. See
         * {@link NavigableMap#floorKey(Object)}
         * @param xDir the x direction the puck is moving (positive or negative)
         * @param yDir the y direction the puck is moving (positive or negative)
         */
        void onBoundaryCrossed(Pucker pucker, int boundaryRadiusId, int xDir, int yDir);
    }
}
