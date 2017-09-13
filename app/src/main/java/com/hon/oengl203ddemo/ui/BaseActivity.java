package com.hon.oengl203ddemo.ui;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.hon.oengl203ddemo.services.SceneLoader;
import com.hon.oengl203ddemo.utils.ToastUtil;

import java.io.File;

/**
 * Created by Frank_Hon on 2017/4/23.
 * e-mail:frank_hon@foxmail.com
 */

public class BaseActivity extends AppCompatActivity {

    protected String paramAssetDir="models/";

    //模型文件名
    protected String paramAssetFilename="ToyPlane.obj";

    /**
     * The file to load. Passed as input parameter
     */
    protected String paramFilename=null;

    protected GLSurfaceView mSurfaceView;

    protected SceneLoader sceneLoader;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle b=getIntent().getExtras();

        if(b!=null){
            this.paramFilename=b.getString("uri");
        }
    }

    protected void initSceneLoader(){
        sceneLoader=new SceneLoader(this);
        sceneLoader.init();
    }

    public String getParamAssetDir() {
        return paramAssetDir;
    }

    public String getParamAssetFilename() {
        return paramAssetFilename;
    }

    public File getParamFile() {
        return getParamFilename() != null ? new File(getParamFilename()) : null;
    }

    public String getParamFilename() {
        return paramFilename;
    }

    public GLSurfaceView getGLSurfaceView(){
        return mSurfaceView;
    }

    public SceneLoader getSceneLoader(){
        return sceneLoader;
    }
}
