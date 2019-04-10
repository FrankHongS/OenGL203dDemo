package com.hon.oengl203ddemo.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Window;

import com.hon.oengl203ddemo.ar.WikitudeSDKConstants;
import com.hon.oengl203ddemo.ar.renderer.GLRenderer;
import com.hon.oengl203ddemo.ar.rendering.Driver;
import com.hon.oengl203ddemo.utils.ToastUtil;
import com.hon.oengl203ddemo.ar_model_view.ModelSurfaceView;
import com.wikitude.NativeStartupConfiguration;
import com.wikitude.WikitudeSDK;
import com.wikitude.common.camera.CameraSettings;
import com.wikitude.common.rendering.RenderExtension;
import com.wikitude.rendering.ExternalRendering;
import com.wikitude.tracker.ImageTarget;
import com.wikitude.tracker.ImageTracker;
import com.wikitude.tracker.ImageTrackerListener;
import com.wikitude.tracker.TargetCollectionResource;
import com.wikitude.tracker.TargetCollectionResourceLoadingCallback;

/**
 * Created by Frank_Hon on 2017/4/23.
 * e-mail:frank_hon@foxmail.com
 */

public class SimpleClientTrackingActivity extends BaseActivity implements ImageTrackerListener, ExternalRendering {
    private static final String TAG = "SimpleClientTracking";

    private WikitudeSDK mWikitudeSDK;
    private Driver mDriver;
    private GLRenderer mRenderer;

    private TargetCollectionResource mTargetCollectionResource;

    private long deltaTime=0;
    private long previousTime=0;
    private long currentTime=0;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        mWikitudeSDK = new WikitudeSDK(this);
        NativeStartupConfiguration startupConfiguration = new NativeStartupConfiguration();
        startupConfiguration.setLicenseKey(WikitudeSDKConstants.WIKITUDE_SDK_KEY);
        startupConfiguration.setCameraPosition(CameraSettings.CameraPosition.BACK);
        startupConfiguration.setCameraResolution(CameraSettings.CameraResolution.AUTO);

        mWikitudeSDK.onCreate(getApplicationContext(), this, startupConfiguration);

        //识别的二维图片
        mTargetCollectionResource = mWikitudeSDK.getTrackerManager().createTargetCollectionResource("file:///android_asset/art01.wtc", new TargetCollectionResourceLoadingCallback() {
            @Override
            public void onError(int errorCode, String errorMessage) {
                Log.v(TAG, "Failed to load target collection resource. Reason: " + errorMessage);
            }

            @Override
            public void onFinish() {
                mWikitudeSDK.getTrackerManager().createImageTracker(mTargetCollectionResource, SimpleClientTrackingActivity.this, null);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        mWikitudeSDK.onResume();
        if(mSurfaceView!=null){
            mSurfaceView.onResume();
        }
        if(mRenderer!=null){
        mRenderer.onResume();
    }
        if(mDriver!=null){
            mDriver.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        mWikitudeSDK.onPause();
        if(mSurfaceView!=null)
            mSurfaceView.onPause();
        if(mRenderer!=null)
            mRenderer.onPause();
        if(mDriver!=null){
            mDriver.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWikitudeSDK.clearCache();
        mWikitudeSDK.onDestroy();
    }

    @Override
    public void onRenderExtensionCreated(RenderExtension renderExtension) {
        mRenderer=new GLRenderer(renderExtension,this);
        mSurfaceView=new ModelSurfaceView(this,mRenderer);
        mDriver=new Driver(mSurfaceView,30);
        setContentView(mSurfaceView);

        initSceneLoader();
    }

    @Override
    public void onTargetsLoaded(ImageTracker imageTracker) {

    }

    @Override
    public void onErrorLoadingTargets(ImageTracker imageTracker, int i, String s) {

    }

    @Override
    public void onImageRecognized(ImageTracker imageTracker, String s) {
        previousTime=System.currentTimeMillis();
        runOnUiThread(new Runnable() {
           @Override
           public void run() {
               ToastUtil.showToast(getApplicationContext(),"onImageRecognized :)");
           }
       });
    }

    @Override
    public void onImageTracked(ImageTracker imageTracker, ImageTarget target) {
        mRenderer.setCurrentlyRecognizedTarget(target);
        currentTime=System.currentTimeMillis();
        deltaTime=currentTime-previousTime;
        if(deltaTime>=1000*5){
            deltaTime=0;
            previousTime=currentTime=System.currentTimeMillis();
            startActivity(DisplayActivity.class);
        }
    }

    @Override
    public void onImageLost(ImageTracker imageTracker, String s) {
        mRenderer.setCurrentlyRecognizedTarget(null);
        previousTime=currentTime=System.currentTimeMillis();
    }

    @Override
    public void onExtendedTrackingQualityChanged(ImageTracker imageTracker, String s, int i, int i1) {

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        previousTime=currentTime=System.currentTimeMillis();
        mRenderer.setCurrentlyRecognizedTarget(null);
    }

    private void startActivity(Class<? extends Activity> target){
        Intent intent=new Intent(this,target);
        startActivity(intent);
    }

}
