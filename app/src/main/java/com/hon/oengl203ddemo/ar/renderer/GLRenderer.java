package com.hon.oengl203ddemo.ar.renderer;

import android.opengl.GLSurfaceView;

import com.hon.oengl203ddemo.ar.model3d.Display3DModel;
import com.hon.oengl203ddemo.ui.BaseActivity;
import com.wikitude.common.rendering.RenderExtension;
import com.wikitude.tracker.Target;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Frank_Hon on 2017/4/24.
 * e-mail:frank_hon@foxmail.com
 */

public class GLRenderer implements GLSurfaceView.Renderer {

    private RenderExtension mWikitudeRenderExtension=null;
    protected Target mCurrentlyRecognizedTarget = null;
    private Display3DModel mDisplay;
    private BaseActivity mActivity;

    public GLRenderer(RenderExtension wikitudeRenderExtension,BaseActivity activity) {
        this.mActivity=activity;

        mWikitudeRenderExtension = wikitudeRenderExtension;
        /*
         * Until Wikitude SDK version 2.1 onDrawFrame triggered also a logic update inside the SDK core.
         * This behaviour is deprecated and onUpdate should be used from now on to update logic inside the SDK core. <br>
         *
         * The default behaviour is that onDrawFrame also updates logic. <br>
         *
         * To use the new separated drawing and logic update methods, RenderExtension.useSeparatedRenderAndLogicUpdates should to be called.
         * Otherwise the logic will still be updated in onDrawFrame.
         */
        mWikitudeRenderExtension.useSeparatedRenderAndLogicUpdates();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        if (mWikitudeRenderExtension != null) {
            mWikitudeRenderExtension.onSurfaceCreated(gl, config);
        }

        mDisplay=new Display3DModel(mActivity);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (mWikitudeRenderExtension != null) {
            mWikitudeRenderExtension.onSurfaceChanged(gl, width, height);
        }
        mDisplay.onSurfaceChanged(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (mWikitudeRenderExtension != null) {
            // Will trigger a logic update in the SDK
            mWikitudeRenderExtension.onUpdate();
            // will trigger drawing of the camera frame
            mWikitudeRenderExtension.onDrawFrame(gl);
        }
        if (mCurrentlyRecognizedTarget != null) {
            mDisplay.onDrawFrame(mCurrentlyRecognizedTarget);
        }
    }

    public void onResume() {
        if (mWikitudeRenderExtension != null) {
            mWikitudeRenderExtension.onResume();
        }
    }

    public void onPause() {
        if (mWikitudeRenderExtension != null) {
            mWikitudeRenderExtension.onPause();
        }
    }

    public void setCurrentlyRecognizedTarget(Target currentlyRecognizedTarget) {
        this.mCurrentlyRecognizedTarget = currentlyRecognizedTarget;
    }

}
