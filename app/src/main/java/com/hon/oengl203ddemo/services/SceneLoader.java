package com.hon.oengl203ddemo.services;

import android.util.Log;
import android.widget.Toast;

import com.hon.oengl203ddemo.model.Object3DBuilder;
import com.hon.oengl203ddemo.model.Object3DData;
import com.hon.oengl203ddemo.ui.BaseActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Frank_Hon on 2017/4/22.
 * e-mail:frank_hon@foxmail.com
 */

public class SceneLoader {

    /**
     * Object selected by the user
     */
    private Object3DData selectedObject = null;

    /**
     * Parent component
     */
    protected final BaseActivity parent;

    /**
     * List of data objects containing info for building the opengl objects
     */
    private List<Object3DData> objects = new ArrayList<Object3DData>();
    /**
     * Light toggle feature: whether to draw using lights
     */
    private boolean drawLighting = true;
    /**
     * Whether to draw objects as wireframes
     */
    private boolean drawWireframe = false;

    /**
     * Initial light position
     */
    private float[] lightPosition = new float[]{0, 0, 3, 1};

    /**
     * Light bulb 3d data
     */
    private final Object3DData lightPoint = Object3DBuilder.buildPoint(new float[1]).setId("light").setPosition(lightPosition);
    /**
     * Whether to draw using textures
     */
    private boolean drawTextures = true;

    public SceneLoader(BaseActivity activity) {
        this.parent = activity;
    }

    public void init() {

        // Load object
        if (parent.getParamFile() != null || parent.getParamAssetDir() != null) {
            Object3DBuilder.loadV5Async(parent, parent.getParamFile(), parent.getParamAssetDir(),
                    parent.getParamAssetFilename(), new Object3DBuilder.Callback() {

                        @Override
                        public void onLoadComplete(Object3DData data) {
                            data.centerAndScale(5.0f);
                            addObject(data);
                        }

                        @Override
                        public void onLoadError(Exception ex) {
                            Log.e("SceneLoader", ex.getMessage(), ex);
                            Toast.makeText(parent.getApplicationContext(),
                                    "There was a problem building the model: " + ex.getMessage(), Toast.LENGTH_LONG)
                                    .show();
                        }
                    });
        }
    }

    public Object3DData getLightBulb() {
        return lightPoint;
    }

    protected synchronized void addObject(Object3DData obj) {
        List<Object3DData> newList = new ArrayList<Object3DData>(objects);
        newList.add(obj);
        this.objects = newList;
        requestRender();
    }

    private void requestRender() {
        parent.getGLSurfaceView().requestRender();
    }

    public synchronized List<Object3DData> getObjects() {
        return objects;
    }

    public boolean isDrawLighting() {
        return drawLighting;
    }

    public void toggleTextures() {
        this.drawTextures = !drawTextures;
    }

    public boolean isDrawTextures() {
        return drawTextures;
    }

    public void toggleWireframe() {
        this.drawWireframe = !this.drawWireframe;
        requestRender();
    }

    public boolean isDrawWireframe() {
        return this.drawWireframe;
    }

    public Object3DData getSelectedObject() {
        return selectedObject;
    }

    public void setSelectedObject(Object3DData selectedObject) {
        this.selectedObject = selectedObject;
    }
}