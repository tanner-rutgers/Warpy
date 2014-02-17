package ca.tannerrutgers.Warpy.listeners;

import android.graphics.Bitmap;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import ca.tannerrutgers.Warpy.activities.WarpyActivity;
import ca.tannerrutgers.Warpy.models.ImageBulge;
import ca.tannerrutgers.Warpy.models.ImageKaleidoscope;
import ca.tannerrutgers.Warpy.models.ImageRipple;
import ca.tannerrutgers.Warpy.models.ImageWarp;
import ca.tannerrutgers.Warpy.tasks.WarpTask;

/**
 * Created by Tanner on 10/02/14.
 */
public class WarpGestureListener implements GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener,
        ScaleGestureDetector.OnScaleGestureListener {

    private WarpyActivity mActivity;

    private float mScaleFactor;

    public WarpGestureListener(WarpyActivity activity) {
        mActivity = activity;
    }

    @Override
    public boolean onDown(MotionEvent event) {
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
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        WarpTask warpTask = new WarpTask(mActivity);
        warpTask.execute(new ImageRipple(mActivity, mActivity.getCurrentBitmapCopy()));
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent event) {
        WarpTask warpTask = new WarpTask(mActivity);
        warpTask.execute(new ImageKaleidoscope(mActivity, mActivity.getCurrentBitmapCopy()));
    }


    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        mScaleFactor = detector.getScaleFactor();
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        if (mScaleFactor > 1.0) {
            WarpTask warpTask = new WarpTask(mActivity);
            warpTask.execute(new ImageBulge(mActivity, mActivity.getCurrentBitmapCopy()));
        }
    }
}
