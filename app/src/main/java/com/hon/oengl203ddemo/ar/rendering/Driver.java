package com.hon.oengl203ddemo.ar.rendering;

import android.opengl.GLSurfaceView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Frank_Hon on 2017/4/23.
 * e-mail:frank_hon@foxmail.com
 */

public class Driver {
    private final GLSurfaceView mModelSurfaceView;
    private final int mFps;
    private Timer mRenderTimer = null;


    public Driver(final GLSurfaceView glSurfaceView, int fps) {
        mModelSurfaceView = glSurfaceView;
        mFps = fps;

    }


    public void start() {
        if (mRenderTimer != null) {
            mRenderTimer.cancel();
        }

        mRenderTimer = new Timer();
        mRenderTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                mModelSurfaceView.requestRender();
            }
        }, 0, 1000 / mFps);
    }

    public void stop() {
        if(mRenderTimer!=null)
            mRenderTimer.cancel();
        mRenderTimer = null;
    }
}
