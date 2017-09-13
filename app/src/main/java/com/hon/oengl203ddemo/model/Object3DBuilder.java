package com.hon.oengl203ddemo.model;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.AssetManager;
import android.opengl.GLES20;
import android.os.AsyncTask;
import android.util.Log;

import com.hon.oengl203ddemo.model.object3d_child.Object3DV0;
import com.hon.oengl203ddemo.model.object3d_child.Object3DV1;
import com.hon.oengl203ddemo.model.object3d_child.Object3DV2;
import com.hon.oengl203ddemo.model.object3d_child.Object3DV3;
import com.hon.oengl203ddemo.model.object3d_child.Object3DV4;
import com.hon.oengl203ddemo.model.object3d_child.Object3DV5;
import com.hon.oengl203ddemo.model.object3d_child.Object3DV6;
import com.hon.oengl203ddemo.model.object3d_child.Object3DV7;
import com.hon.oengl203ddemo.model.object3d_child.Object3DV8;
import com.hon.oengl203ddemo.services.WavefrontLoader;
import com.hon.oengl203ddemo.utils.Math3DUtils;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import static com.wikitude.native_android_sdk.a.e;

/**
 * Created by Frank_Hon on 2017/4/22.
 * e-mail:frank_hon@foxmail.com
 */

public class Object3DBuilder {
    public interface Callback {
        void onLoadError(Exception ex);

        void onLoadComplete(Object3DData data);
    }

    /**
     * Default vertices colors
     */
    private static float[] DEFAULT_COLOR = {1.0f, 1.0f, 0, 1.0f};

    private Object3DV0 object3dv0;
    private Object3DV1 object3dv1;
    private Object3DV2 object3dv2;
    private Object3DV3 object3dv3;
    private Object3DV4 object3dv4;
    private Object3DV5 object3dv5;
    private Object3DV6 object3dv6;
    private Object3DV7 object3dv7;
    private Object3DV8 object3dv8;

    public static Object3DData buildPoint(float[] point) {
        return new Object3DData(createNativeByteBuffer(point.length * 4).asFloatBuffer().put(point))
                .setDrawMode(GLES20.GL_POINTS);
    }

    public static void loadV5Async(Activity parent, File file,String assetsDir, String assetName,
                                   final Callback callback) {
        final InputStream modelDataStream;
        final InputStream modelDataStream2;
        try {
            if (file != null) {
                modelDataStream = new FileInputStream(file);
                modelDataStream2 = new FileInputStream(file);
            } else if (assetsDir != null) {
                modelDataStream = parent.getAssets().open(assetsDir + assetName);
                modelDataStream2 = parent.getAssets().open(assetsDir + assetName);
            } else {
                throw new IllegalArgumentException("Model data source not specified");
            }
        } catch (IOException ex) {

           throw new RuntimeException(
                    "There was a problem opening file/asset '" + (file != null ? file : assetsDir + assetName) + "'"+ex.getMessage());
        }

        Log.i("Loader", "Loading model...");
        LoaderTask loaderTask = new LoaderTask(parent, file != null ? file.getParentFile() : null, assetsDir,
                file != null ? file.getName() : assetName) {

            @Override
            protected void onPostExecute(Object3DData data) {
                super.onPostExecute(data);
                try {
                    modelDataStream2.close();
                    modelDataStream.close();
                } catch (IOException ex) {
                    Log.e("Menu", "Problem closing stream: " + ex.getMessage(), ex);
                }
                if (error != null) {
                    callback.onLoadError(error);
                } else {
                    callback.onLoadComplete(data);
                }
            }
        };
        loaderTask.execute(modelDataStream2, modelDataStream);
    }

    public Object3D getDrawer(Object3DData obj, boolean usingTextures, boolean usingLights) throws IOException {

        if (object3dv1 == null) {
            object3dv1 = new Object3DV1();
            object3dv2 = new Object3DV2();
            object3dv3 = new Object3DV3();
            object3dv4 = new Object3DV4();
            object3dv5 = new Object3DV5();
            object3dv6 = new Object3DV6();
            object3dv7 = new Object3DV7();
            object3dv8 = new Object3DV8();
        }

        if (usingTextures && usingLights && obj.getVertexColorsArrayBuffer() != null && obj.getTextureData() != null
                && obj.getTextureCoordsArrayBuffer() != null && obj.getVertexNormalsArrayBuffer() != null
                && obj.getVertexNormalsArrayBuffer() != null) {
            return object3dv6;
        } else if (usingTextures && usingLights && obj.getVertexColorsArrayBuffer() == null && obj.getTextureData() != null
                && obj.getTextureCoordsArrayBuffer() != null && obj.getVertexNormalsArrayBuffer() != null
                && obj.getVertexNormalsArrayBuffer() != null) {
            return object3dv8;
        } else if (usingLights && obj.getVertexColorsArrayBuffer() != null
                && obj.getVertexNormalsArrayBuffer() != null) {
            return object3dv5;
        } else if (usingLights && obj.getVertexNormalsArrayBuffer() != null) {
            return object3dv7;
        } else if (usingTextures && obj.getVertexColorsArrayBuffer() != null && obj.getTextureData() != null
                && obj.getTextureCoordsArrayBuffer() != null) {
            return object3dv4;
        } else if (usingTextures && obj.getVertexColorsArrayBuffer() == null && obj.getTextureData() != null
                && obj.getTextureCoordsArrayBuffer() != null) {
            return object3dv3;
        } else if (obj.getVertexColorsArrayBuffer() != null) {
            return object3dv2;
        } else {
            return object3dv1;
        }
    }

    public static Object3DData generateArrays(AssetManager assets, Object3DData obj) throws IOException {
        int drawMode = GLES20.GL_TRIANGLES;
        int drawSize = 0;

        FloatBuffer vertexBuffer = obj.getVerts();


        WavefrontLoader.Faces faces = obj.getFaces(); // model faces
        WavefrontLoader.FaceMaterials faceMats = obj.getFaceMats();
        WavefrontLoader.Materials materials = obj.getMaterials();

        // TODO: generate face normals
        FloatBuffer vertexArrayBuffer = null;
        IntBuffer drawOrderBuffer = null;
        if (obj.isDrawUsingArrays()) {
            Log.i("Object3DBuilder", "Generating vertex array buffer...");
            vertexArrayBuffer = createNativeByteBuffer(faces.getIndexBuffer().capacity() * 3 * 4).asFloatBuffer();
            for (int i = 0; i < faces.getVerticesReferencesCount(); i++) {
                vertexArrayBuffer.put(vertexBuffer.get(faces.getIndexBuffer().get(i) * 3));
                vertexArrayBuffer.put(vertexBuffer.get(faces.getIndexBuffer().get(i) * 3 + 1));
                vertexArrayBuffer.put(vertexBuffer.get(faces.getIndexBuffer().get(i) * 3 + 2));
            }
        } else {
            // TODO:
            // Log.i("Object3DBuilder", "Generating draw order buffer...");
            // this only works for faces made of a single triangle
            // drawOrderBuffer = faces.facesVertIdxs;
        }

        boolean onlyTriangles = true;
		/*List<int[]> drawModeList = new ArrayList<int[]>();
		int currentVertexPos = 0;
		for (int[] face : faces.facesVertIdxs) {
			if (face.length == 3) {
				drawModeList.add(new int[] { GLES20.GL_TRIANGLES, currentVertexPos, face.length });
			} else {
				onlyTriangles = false;
				drawModeList.add(new int[] { GLES20.GL_TRIANGLE_FAN, currentVertexPos, face.length });
			}
			currentVertexPos += face.length;
		}*/

        if (onlyTriangles) {
            drawMode = GLES20.GL_TRIANGLES;
            drawSize = 0;
			/*drawModeList = null;*/
        }

        FloatBuffer vertexNormalsBuffer = obj.getNormals();

        FloatBuffer vertexNormalsArrayBuffer = createNativeByteBuffer(faces.getIndexBuffer().capacity() / 3 * 9 * 4)
                .asFloatBuffer();

        // load file normals

        if (vertexNormalsBuffer.capacity() > 0) {
            Log.i("Object3DBuilder", "Generating normals array...");
            for (int[] normal : faces.facesNormIdxs) {
                for (int i = 0; i < normal.length; i++) {
                    vertexNormalsArrayBuffer.put(vertexNormalsBuffer.get(normal[i] * 3));
                    vertexNormalsArrayBuffer.put(vertexNormalsBuffer.get(normal[i] * 3 + 1));
                    vertexNormalsArrayBuffer.put(vertexNormalsBuffer.get(normal[i] * 3 + 2));
                }
            }
        } else {
            // calculate normals for all triangles
            Log.i("Object3DBuilder", "Model without normals. Calculating [" + faces.getIndexBuffer().capacity() / 3 + "] normals...");

            final float[] v0 = new float[3], v1 = new float[3], v2 = new float[3];
            for (int i = 0; i < faces.getIndexBuffer().capacity(); i += 3) {
                try {
                    v0[0] = vertexBuffer.get(faces.getIndexBuffer().get(i) * 3);
                    v0[1] = vertexBuffer.get(faces.getIndexBuffer().get(i) * 3 + 1);
                    v0[2] = vertexBuffer.get(faces.getIndexBuffer().get(i) * 3 + 2);

                    v1[0] = vertexBuffer.get(faces.getIndexBuffer().get(i + 1) * 3);
                    v1[1] = vertexBuffer.get(faces.getIndexBuffer().get(i + 1) * 3 + 1);
                    v1[2] = vertexBuffer.get(faces.getIndexBuffer().get(i + 1) * 3 + 2);

                    v2[0] = vertexBuffer.get(faces.getIndexBuffer().get(i + 2) * 3);
                    v2[1] = vertexBuffer.get(faces.getIndexBuffer().get(i + 2) * 3 + 1);
                    v2[2] = vertexBuffer.get(faces.getIndexBuffer().get(i + 2) * 3 + 2);

                    float[] normal = Math3DUtils.calculateFaceNormal2(v0, v1, v2);

                    vertexNormalsArrayBuffer.put(normal);
                    vertexNormalsArrayBuffer.put(normal);
                    vertexNormalsArrayBuffer.put(normal);
                } catch (BufferOverflowException ex) {
                    throw new RuntimeException("Error calculating mormal for face ["+i/3+"]");
                }
            }
        }


        FloatBuffer colorArrayBuffer = null;
        float[] currentColor = DEFAULT_COLOR;
        if (materials != null) {
            materials.readMaterials(obj.getCurrentDir(),obj.getAssetsDir(), assets);
            if (!faceMats.isEmpty()) {
                colorArrayBuffer = createNativeByteBuffer(4 * faces.getVerticesReferencesCount() * 4)
                        .asFloatBuffer();
                boolean anyOk = false;
                for (int i = 0; i < faces.getNumFaces(); i++) {
                    if (faceMats.findMaterial(i) != null) {
                        WavefrontLoader.Material mat = materials.getMaterial(faceMats.findMaterial(i));
                        if (mat != null) {
                            currentColor = mat.getKdColor() != null ? mat.getKdColor() : currentColor;
                            anyOk = anyOk || mat.getKdColor() != null;
                        }
                    }
                    colorArrayBuffer.put(currentColor);
                    colorArrayBuffer.put(currentColor);
                    colorArrayBuffer.put(currentColor);
                }
                if (!anyOk) {
                    Log.i("Object3DBuilder", "Using single color.");
                    colorArrayBuffer = null;
                }
            }
        }

        // materials = null;

        byte[] textureData = null;
        FloatBuffer textureCoordsArraysBuffer = null;
        if (materials != null && !materials.materials.isEmpty()) {

            // TODO: process all textures
            String texture = null;
            for (WavefrontLoader.Material mat : materials.materials.values()) {
                if (mat.getTexture() != null) {
                    texture = mat.getTexture();
                    break;
                }
            }
            if (texture != null) {
                if (obj.getCurrentDir() != null) {
                    File file = new File(obj.getCurrentDir(), texture);
                    Log.i("materials", "Loading texture '" + file + "'...");
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    FileInputStream fis = new FileInputStream(file);
                    IOUtils.copy(fis, bos);
                    fis.close();
                    textureData = bos.toByteArray();
                    bos.close();
                }else{
                    Log.i("materials", "Loading texture '" + obj.getAssetsDir() + texture + "'...");
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    InputStream fis = assets.open(obj.getAssetsDir() + texture);
                    IOUtils.copy(fis, bos);//TODO
                    fis.close();
                    textureData = bos.toByteArray();
                    bos.close();
                }

                if (textureData != null) {
                    ArrayList<WavefrontLoader.Tuple3> texCoords = obj.getTexCoords();
                    if (texCoords != null && texCoords.size() > 0) {
                        FloatBuffer textureCoordsBuffer = createNativeByteBuffer(2 * texCoords.size() * 4).asFloatBuffer();
                        for (WavefrontLoader.Tuple3 texCor : texCoords) {
                            textureCoordsBuffer.put(texCor.getX());
                            textureCoordsBuffer.put(obj.isFlipTextCoords() ? 1 - texCor.getY() : texCor.getY());
                        }
                        textureCoordsArraysBuffer = createNativeByteBuffer(2 * faces.getVerticesReferencesCount() * 4)
                                .asFloatBuffer();
                        try {

                            boolean anyTextureOk = false;
                            String currentTexture = null;

                            for (int i = 0; i < faces.facesTexIdxs.size(); i++) {

                                // get current texture
                                if (!faceMats.isEmpty() && faceMats.findMaterial(i) != null) {
                                    WavefrontLoader.Material mat = materials.getMaterial(faceMats.findMaterial(i));
                                    if (mat != null && mat.getTexture() != null) {
                                        currentTexture = mat.getTexture();
                                    }
                                }

                                // check if texture is ok (Because we only support 1 texture currently)
                                boolean textureOk = false;
                                if (currentTexture != null && currentTexture.equals(texture)) {
                                    textureOk = true;
                                }

                                // populate texture coords if ok
                                int[] text = faces.facesTexIdxs.get(i);
                                for (int j = 0; j < text.length; j++) {
                                    if (textureOk) {
                                        anyTextureOk = true;
                                        textureCoordsArraysBuffer.put(textureCoordsBuffer.get(text[j] * 2));
                                        textureCoordsArraysBuffer.put(textureCoordsBuffer.get(text[j] * 2 + 1));
                                    } else {
                                        textureCoordsArraysBuffer.put(0f);
                                        textureCoordsArraysBuffer.put(0f);
                                    }
                                }
                            }

                            if (!anyTextureOk) {
                                Log.i("Object3DBuilder", "Texture is wrong. Applying global texture");
                                textureCoordsArraysBuffer.position(0);
                                for (int[] text : faces.facesTexIdxs) {
                                    for (int i = 0; i < text.length; i++) {
                                        textureCoordsArraysBuffer.put(textureCoordsBuffer.get(text[i] * 2));
                                        textureCoordsArraysBuffer.put(textureCoordsBuffer.get(text[i] * 2 + 1));
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            Log.e("WavefrontLoader", "Failure to load texture coordinates");
                        }
                    }
                }
            } else {
                Log.i("Loader", "Found material(s) but no texture");
            }
        }

        obj.setColor(currentColor);
        obj.setVertexBuffer(vertexBuffer);
        obj.setVertexNormalsBuffer(vertexNormalsBuffer);
        obj.setDrawOrder(drawOrderBuffer);
        obj.setDrawSize(drawSize);

        obj.setVertexArrayBuffer(vertexArrayBuffer);
        obj.setVertexNormalsArrayBuffer(vertexNormalsArrayBuffer);
        obj.setTextureCoordsArrayBuffer(textureCoordsArraysBuffer);
        obj.setVertexColorsArrayBuffer(colorArrayBuffer);

        obj.setDrawModeList(null);
        obj.setDrawMode(drawMode);

        obj.setTextureData(textureData);

        return obj;
    }

    /**
     * Builds a wireframe of the model by drawing all lines (3) of the triangles. This method uses
     * the drawOrder buffer.
     * @param objData the 3d model
     * @return the 3d wireframe
     */
    public static Object3DData buildWireframe(Object3DData objData) {
        try {
            FloatBuffer vertexArrayBuffer = objData.getVertexArrayBuffer();
            IntBuffer drawOrder = createNativeByteBuffer(vertexArrayBuffer.capacity() / 3 * 2 * 4).asIntBuffer();
            for (int i = 0; i < vertexArrayBuffer.capacity()/3; i+=3) {
                drawOrder.put((i));
                drawOrder.put((i+1));
                drawOrder.put((i+1));
                drawOrder.put((i+2));
                drawOrder.put((i+2));
                drawOrder.put((i));
            }
            return new Object3DData(vertexArrayBuffer).setDrawOrder(drawOrder)
                    .setVertexNormalsArrayBuffer(objData.getVertexNormalsArrayBuffer())
                    .setColor(objData.getColor())
                    .setVertexColorsArrayBuffer(objData.getVertexColorsArrayBuffer())
                    .setTextureCoordsArrayBuffer(objData.getTextureCoordsArrayBuffer())
                    .setPosition(objData.getPosition())
                    .setRotation(objData.getRotation())
                    .setDrawMode(GLES20.GL_LINES)
                    .setDrawSize(-1);
        } catch (Exception ex) {
            Log.e("Object3DBuilder", ex.getMessage(), ex);
        }
        return objData;
    }


    private static ByteBuffer createNativeByteBuffer(int length) {
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(length);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());
        return bb;
    }

    public Object3D getPointDrawer() {
        if (object3dv0 == null) {
            object3dv0 = new Object3DV0();
        }
        return object3dv0;
    }

    private static class LoaderTask extends AsyncTask<InputStream, Integer, Object3DData> {

        /**
         * The parent activity
         */
        private final Activity parent;
        /**
         * Directory where the model is located (null when its loaded from asset)
         */
        private final File currentDir;
        /**
         * Asset directory where the model is loaded (null when its loaded from the filesystem)
         */
        private final String assetsDir;
        /**
         * Id of the data being loaded
         */
        private final String modelId;
        /**
         * The dialog that will show the progress of the loading
         */
        private final ProgressDialog dialog;
        /**
         * Exception when loading data (if any)
         */
        protected Exception error;

        /**
         * Build a new progress dialog for loading the data model asynchronously
         *
         * @param modelId    the id the data being loaded
         */
        public LoaderTask(Activity parent,File currentDir, String assetsDir, String modelId) {
            this.parent = parent;
            this.currentDir=currentDir;
            this.assetsDir = assetsDir;
            this.modelId = modelId;
            this.dialog = new ProgressDialog(parent);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // this.dialog = ProgressDialog.show(this.parent, "Please wait ...", "Loading model data...", true);
            // this.dialog.setTitle(modelId);
            // this.dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            this.dialog.setCancelable(false);
            this.dialog.show();
        }

        @Override
        protected Object3DData doInBackground(InputStream... params) {
            try {
                publishProgress(0);

                WavefrontLoader wfl = new WavefrontLoader("");
                wfl.reserveData(params[0]);
                publishProgress(1);

                wfl.loadModel(params[1]);
                publishProgress(2);

                Object3DData data3D = new Object3DData(wfl.getVerts(), wfl.getNormals(), wfl.getTexCoords(), wfl.getFaces(),
                        wfl.getFaceMats(), wfl.getMaterials());
                data3D.setId(modelId);
                data3D.setCurrentDir(currentDir);
                data3D.setAssetsDir(assetsDir);

                Object3DBuilder.generateArrays(parent.getAssets(), data3D);
                publishProgress(3);
                return data3D;
            } catch (Exception ex) {
                error = ex;
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            switch (values[0]) {
                case 0:
                    this.dialog.setMessage("Analyzing model...");
                    break;
                case 1:
                    this.dialog.setMessage("Loading data...");
                    break;
                case 2:
                    this.dialog.setMessage("Building 3D model...");
                    break;
                case 3:
                    this.dialog.setMessage("Model '" + modelId + "' built");
                    break;
            }
        }

        @Override
        protected void onPostExecute(Object3DData success) {
            super.onPostExecute(success);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }
}
