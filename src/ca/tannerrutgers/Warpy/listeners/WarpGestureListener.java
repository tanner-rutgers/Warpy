package ca.tannerrutgers.Warpy.listeners;

import android.gesture.Gesture;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import ca.tannerrutgers.Warpy.activities.MainActivity;

/**
 * Created by Tanner on 10/02/14.
 */
public class WarpGestureListener implements GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener,
        ScaleGestureDetector.OnScaleGestureListener {

    private MainActivity mActivity;

    public WarpGestureListener(MainActivity activity) {
        mActivity = activity;
    }

    @Override
    public boolean onDown(MotionEvent event) {
        mActivity.debugLog( "onDown: " + event.toString());
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2,
                           float velocityX, float velocityY) {
        mActivity.debugLog( "onFling: " + event1.toString() + event2.toString());
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        mActivity.debugLog( "onDoubleTap: " + event.toString());
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent event) {
        mActivity.debugLog( "onLongPress: " + event.toString());
    }


    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        mActivity.debugLog("onScale: " + detector.toString());
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }
}
