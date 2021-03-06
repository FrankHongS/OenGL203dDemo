package com.hon.oengl203ddemo.ar.model3d;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;
import android.widget.Toast;

import com.hon.oengl203ddemo.entities.Camera;
import com.hon.oengl203ddemo.model.Object3D;
import com.hon.oengl203ddemo.model.Object3DBuilder;
import com.hon.oengl203ddemo.model.Object3DData;
import com.hon.oengl203ddemo.model.Object3DImpl;
import com.hon.oengl203ddemo.services.SceneLoader;
import com.hon.oengl203ddemo.ui.BaseActivity;
import com.hon.oengl203ddemo.utils.GLUtils;
import com.wikitude.tracker.Target;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Frank_Hon on 2017/4/24.
 * e-mail:frank_hon@foxmail.com
 */

public class Display3DModel {

    // width of the screen
    private int width;
    // height of the screen
    private int height;
    // frustrum - nearest pixel
    private float near = 1f;
    // frustrum - fartest pixel
    private float far = 10f;

    //3D window (parent component)
    private BaseActivity main;

    // The loaded textures
    private Map<byte[], Integer> textures = new HashMap<byte[], Integer>();
    // The wireframe associated shape (it should be made of lines only)
    private Map<Object3DData, Object3DData> wireframes = new HashMap<Object3DData, Object3DData>();

    // light position required to render with lighting
    private final float[] lightPosInEyeSpace = new float[4];

    // 3D matrices to project our 3D world
    private float[] modelProjectionMatrix = new float[16];
    private float[] modelViewMatrix = new float[16];
    // mvpMatrix is an abbreviation for "Model View Projection Matrix"
    private float[] mvpMatrix = new float[16];

    private Object3DBuilder drawer;

    private Camera camera;

    public Display3DModel(BaseActivity activity){

        this.main=activity;

        // This component will draw the actual models using OpenGL
        drawer=new Object3DBuilder();

        // Lets create our 3D world components
        camera = new Camera();
    }

    public void onSurfaceChanged(int width,int height){
        this.width=width;
        this.height=height;
    }

    public void onDrawFrame(final float[] modelProjectionMatrix, final float[] modelViewMatrix) {
        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (camera.hasChanged()) {
            Matrix.setLookAtM(modelViewMatrix, 0, camera.xPos, camera.yPos, camera.zPos, camera.xView, camera.yView,
                    camera.zView, camera.xUp, camera.yUp, camera.zUp);
            // Log.d("Camera", "Changed! :"+camera.ToStringVector());
            Matrix.multiplyMM(mvpMatrix, 0, modelProjectionMatrix, 0, modelViewMatrix, 0);
            camera.setChanged(false);
        }

        SceneLoader scene = main.getSceneLoader();
        if (scene == null) {
            // scene not ready
            return;
        }

        // camera should know about objects that collision with it
        camera.setScene(scene);

        // draw light
        if (scene.isDrawLighting()) {

            Object3DImpl lightBulbDrawer = (Object3DImpl) drawer.getPointDrawer();

            float[] lightModelViewMatrix = lightBulbDrawer.getMvMatrix(lightBulbDrawer.getMMatrix(scene.getLightBulb()),modelViewMatrix);

            // Calculate position of the light in eye space to support lighting
            Matrix.multiplyMV(lightPosInEyeSpace, 0, lightModelViewMatrix, 0, scene.getLightBulb().getPosition(), 0);

            // Draw a point that represents the light bulb
            lightBulbDrawer.draw(scene.getLightBulb(), modelProjectionMatrix, modelViewMatrix, -1, lightPosInEyeSpace);
        }

        List<Object3DData> objects = scene.getObjects();
        for (int i=0; i<objects.size(); i++) {
            try {
                Object3DData objData = objects.get(i);
//                boolean changed = objData.isChanged();

                Object3D drawerObject = drawer.getDrawer(objData, scene.isDrawTextures(), scene.isDrawLighting());
                // Log.d("ModelRenderer","Drawing object using '"+drawerObject.getClass()+"'");

                Integer textureId = textures.get(objData.getTextureData());
                if (textureId == null && objData.getTextureData() != null) {
                    ByteArrayInputStream textureIs = new ByteArrayInputStream(objData.getTextureData());
                    textureId = GLUtils.loadTexture(textureIs);
                    textureIs.close();
                    textures.put(objData.getTextureData(), textureId);
                }

                if (scene.isDrawWireframe() && objData.getDrawMode() != GLES20.GL_POINTS
                        && objData.getDrawMode() != GLES20.GL_LINES && objData.getDrawMode() != GLES20.GL_LINE_STRIP
                        && objData.getDrawMode() != GLES20.GL_LINE_LOOP) {
                    Log.d("ModelRenderer","Drawing wireframe model...");
                    try{
                        // Only draw wireframes for objects having faces (triangles)
                        Object3DData wireframe = wireframes.get(objData);
                        if (wireframe == null ) {
                            Log.i("ModelRenderer","Generating wireframe model...");
//							wireframe = Object3DBuilder.buildWireframe4(objData);
//							wireframe.centerAndScale(5.0f);
                            wireframe = Object3DBuilder.buildWireframe(objData);
                            wireframes.put(objData, wireframe);
                        }
                        drawerObject.draw(wireframe,modelProjectionMatrix,modelViewMatrix,wireframe.getDrawMode(),
                                wireframe.getDrawSize(),textureId != null? textureId:-1, lightPosInEyeSpace);
                    }catch(Error e){
                        Log.e("ModelRenderer",e.getMessage(),e);
                    }
                } else {

                    drawerObject.draw(objData, modelProjectionMatrix, modelViewMatrix,
                            textureId != null ? textureId : -1, lightPosInEyeSpace);
                }

                // Draw bounding box
//                if (scene.isDrawBoundingBox() || scene.getSelectedObject() == objData) {
//                    Object3DData boundingBoxData = boundingBoxes.get(objData);
//                    if (boundingBoxData == null || changed) {
//                        boundingBoxData = Object3DBuilder.buildBoundingBox(objData);
//                        boundingBoxes.put(objData, boundingBoxData);
//                    }
//                    Object3D boundingBoxDrawer = drawer.getBoundingBoxDrawer();
//                    boundingBoxDrawer.draw(boundingBoxData, modelProjectionMatrix, modelViewMatrix, -1, null);
//                }

                // Draw bounding box
//                if (scene.isDrawNormals()) {
//                    Object3DData normalData = normals.get(objData);
//                    if (normalData == null || changed) {
//                        normalData = Object3DBuilder.buildFaceNormals(objData);
//                        if (normalData != null) {
//                            // it can be null if object isnt made of triangles
//                            normals.put(objData, normalData);
//                        }
//                    }
//                    if (normalData != null) {
//                        Object3D normalsDrawer = drawer.getFaceNormalsDrawer();
//                        normalsDrawer.draw(normalData, modelProjectionMatrix, modelViewMatrix, -1, null);
//                    }
//                }
                // TODO: enable this only when user wants it
                // obj3D.drawVectorNormals(result, modelViewMatrix);
            } catch (IOException ex) {
                Toast.makeText(main,
                        "There was a problem creating 3D object", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void onDrawFrame(final Target currentlyRecognizedTarget) {
        float[] pm=currentlyRecognizedTarget.getProjectionMatrix();
        float[] vm=currentlyRecognizedTarget.getViewMatrix();
//        Matrix.scaleM(vm,0,0.8f,0.8f,0.8f);
        this.onDrawFrame(pm, vm);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public float getNear() {
        return near;
    }

    public float getFar() {
        return far;
    }

    public Camera getCamera() {
        return camera;
    }

    public float[] getModelProjectionMatrix() {
        return modelProjectionMatrix;
    }

    public float[] getModelViewMatrix() {
        return modelViewMatrix;
    }
}
