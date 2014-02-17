/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Tanner Rutgers
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
