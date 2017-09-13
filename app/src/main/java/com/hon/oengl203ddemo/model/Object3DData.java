package com.hon.oengl203ddemo.model;

import android.util.Log;

import com.hon.oengl203ddemo.services.WavefrontLoader;

import java.io.File;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Frank_Hon on 2017/4/22.
 * e-mail:frank_hon@foxmail.com
 */

public class Object3DData {

    private int version=5;

    /**
     * The directory where the files reside so we can load referenced files in the model like material and textures
     * files
     */
    private File currentDir;

    /**
     * The assets directory where the files reside so we can load referenced files in the model like material and
     * textures files
     */
    private String assetsDir;
    private String id;
    private boolean drawUsingArrays = true;
    private boolean flipTextCoords = true;

    // Model data
    private FloatBuffer verts;
    private FloatBuffer normals;
    private ArrayList<WavefrontLoader.Tuple3> texCoords;
    private WavefrontLoader.Faces faces;
    private WavefrontLoader.FaceMaterials faceMats;
    private WavefrontLoader.Materials materials;

    // Processed data
    private FloatBuffer vertexBuffer = null;
    private FloatBuffer vertexNormalsBuffer = null;
    private IntBuffer drawOrderBuffer = null;

    // Processed arrays
    private FloatBuffer vertexArrayBuffer = null;
    private FloatBuffer vertexColorsArrayBuffer = null;
    private FloatBuffer vertexNormalsArrayBuffer = null;
    private FloatBuffer textureCoordsArrayBuffer = null;
    private List<int[]> drawModeList = null;
    private byte[] textureData = null;
    private List<InputStream> textureStreams = null;

    private float[] color;
    private int drawMode;
    private int drawSize;

    // Transformation data
    protected float[] position = new float[] { 0f, 0f, 0f };
    protected float[] rotation = new float[] { 0f, 0f, 0f };

    public Object3DData(FloatBuffer verts, FloatBuffer normals, ArrayList<WavefrontLoader.Tuple3> texCoords, WavefrontLoader.Faces faces,
                        WavefrontLoader.FaceMaterials faceMats, WavefrontLoader.Materials materials) {
        super();
        this.verts = verts;
        this.normals = normals;
        this.texCoords = texCoords;
        this.faces = faces;
        this.faceMats = faceMats;
        this.materials = materials;
    }

    public Object3DData(FloatBuffer vertexArrayBuffer) {
        this.vertexArrayBuffer = vertexArrayBuffer;
        this.version = 1;
    }

    public Object3DData centerAndScale(float maxSize) {
        float leftPt = Float.MAX_VALUE, rightPt = Float.MIN_VALUE; // on x-axis
        float topPt = Float.MIN_VALUE, bottomPt = Float.MAX_VALUE; // on y-axis
        float farPt = Float.MAX_VALUE, nearPt = Float.MIN_VALUE; // on z-axis

        FloatBuffer vertexBuffer = getVertexArrayBuffer() != null ? getVertexArrayBuffer() : getVertexBuffer();
        if (vertexBuffer == null) {
            Log.v("Object3DData", "Scaling for '" + getId() + "' I found that there is no vertex data");
            return this;
        }

        Log.i("Object3DData", "Calculating dimensions for '" + getId() + "...");
        for (int i = 0; i < vertexBuffer.capacity(); i += 3) {
            if (vertexBuffer.get(i) > rightPt)
                rightPt = vertexBuffer.get(i);
            else if (vertexBuffer.get(i) < leftPt)
                leftPt = vertexBuffer.get(i);
            if (vertexBuffer.get(i + 1) > topPt)
                topPt = vertexBuffer.get(i + 1);
            else if (vertexBuffer.get(i + 1) < bottomPt)
                bottomPt = vertexBuffer.get(i + 1);
            if (vertexBuffer.get(i + 2) > nearPt)
                nearPt = vertexBuffer.get(i + 2);
            else if (vertexBuffer.get(i + 2) < farPt)
                farPt = vertexBuffer.get(i + 2);
        } // end
        Log.i("Object3DData", "Dimensions for '" + getId() + " (X left, X right): ("+leftPt+","+rightPt+")");
        Log.i("Object3DData", "Dimensions for '" + getId() + " (Y top, Y bottom): ("+topPt+","+bottomPt+")");
        Log.i("Object3DData", "Dimensions for '" + getId() + " (Z near, Z far): ("+nearPt+","+farPt+")");

        // calculate center of 3D object
        float xc = (rightPt + leftPt) / 2.0f;
        float yc = (topPt + bottomPt) / 2.0f;
        float zc = (nearPt + farPt) / 2.0f;

        // calculate largest dimension
        float height = topPt - bottomPt;
        float depth = nearPt - farPt;
        float largest = rightPt - leftPt;
        if (height > largest)
            largest = height;
        if (depth > largest)
            largest = depth;
        Log.i("Object3DData", "Largest dimension ["+largest+"]");

        // scale object

        // calculate a scale factor
        float scaleFactor = 1.0f;
        // System.out.println("Largest dimension: " + largest);
        if (largest != 0.0f)
            scaleFactor = (maxSize / largest);
        Log.i("Object3DData",
                "Centering & scaling '" + getId() + "' to (" + xc + "," + yc + "," + zc + ") scale: '" + scaleFactor + "'");

        // modify the model's vertices
        for (int i = 0; i < vertexBuffer.capacity(); i += 3) {
            float x = vertexBuffer.get(i);
            float y = vertexBuffer.get(i + 1);
            float z = vertexBuffer.get(i + 2);
            x = (x - xc) * scaleFactor;
            y = (y - yc) * scaleFactor;
            z = (z - zc) * scaleFactor;
            vertexBuffer.put(i, x);
            vertexBuffer.put(i + 1, y);
            vertexBuffer.put(i + 2, z);
        }

        return this;
    }

    public FloatBuffer getVerts() {
        return verts;
    }

    public FloatBuffer getNormals() {
        return normals;
    }

    public ArrayList<WavefrontLoader.Tuple3> getTexCoords() {
        return texCoords;
    }

    public WavefrontLoader.Faces getFaces() {
        return faces;
    }

    public WavefrontLoader.FaceMaterials getFaceMats() {
        return faceMats;
    }

    public WavefrontLoader.Materials getMaterials() {
        return materials;
    }

    public File getCurrentDir() {
        return currentDir;
    }

    public void setCurrentDir(File currentDir) {
        this.currentDir = currentDir;
    }

    public String getAssetsDir() {
        return assetsDir;
    }

    public void setAssetsDir(String assetsDir) {
        this.assetsDir = assetsDir;
    }

    public String getId() {
        return id;
    }

    public Object3DData setId(String id) {
        this.id = id;
        return this;
    }

    public boolean isDrawUsingArrays() {
        return drawUsingArrays;
    }

    public boolean isFlipTextCoords() {
        return flipTextCoords;
    }

    public void setDrawUsingArrays(boolean drawUsingArrays) {
        this.drawUsingArrays = drawUsingArrays;
    }

    public void setFlipTextCoords(boolean flipTextCoords) {
        this.flipTextCoords = flipTextCoords;
    }

    public FloatBuffer getVertexBuffer() {
        return vertexBuffer;
    }

    public void setVertexBuffer(FloatBuffer vertexBuffer) {
        this.vertexBuffer = vertexBuffer;
    }

    public FloatBuffer getVertexNormalsBuffer() {
        return vertexNormalsBuffer;
    }

    public void setVertexNormalsBuffer(FloatBuffer vertexNormalsBuffer) {
        this.vertexNormalsBuffer = vertexNormalsBuffer;
    }

    public IntBuffer getDrawOrderBuffer() {
        return drawOrderBuffer;
    }

    public Object3DData setDrawOrder(IntBuffer drawOrderBuffer) {
        this.drawOrderBuffer = drawOrderBuffer;
        return this;
    }

    public FloatBuffer getVertexArrayBuffer() {
        return vertexArrayBuffer;
    }

    public void setVertexArrayBuffer(FloatBuffer vertexArrayBuffer) {
        this.vertexArrayBuffer = vertexArrayBuffer;
    }

    public FloatBuffer getVertexColorsArrayBuffer() {
        return vertexColorsArrayBuffer;
    }

    public Object3DData setVertexColorsArrayBuffer(FloatBuffer vertexColorsArrayBuffer) {
        this.vertexColorsArrayBuffer = vertexColorsArrayBuffer;
        return this;
    }

    public FloatBuffer getVertexNormalsArrayBuffer() {
        return vertexNormalsArrayBuffer;
    }

    public Object3DData setVertexNormalsArrayBuffer(FloatBuffer vertexNormalsArrayBuffer) {
        this.vertexNormalsArrayBuffer = vertexNormalsArrayBuffer;
        return this;
    }

    public FloatBuffer getTextureCoordsArrayBuffer() {
        return textureCoordsArrayBuffer;
    }

    public Object3DData setTextureCoordsArrayBuffer(FloatBuffer textureCoordsArrayBuffer) {
        this.textureCoordsArrayBuffer = textureCoordsArrayBuffer;
        return this;
    }

    public List<int[]> getDrawModeList() {
        return drawModeList;
    }

    public void setDrawModeList(List<int[]> drawModeList) {
        this.drawModeList = drawModeList;
    }

    public byte[] getTextureData() {
        return textureData;
    }

    public void setTextureData(byte[] textureData) {
        this.textureData = textureData;
    }

    public List<InputStream> getTextureStreams() {
        return textureStreams;
    }

    public void setTextureStreams(List<InputStream> textureStreams) {
        this.textureStreams = textureStreams;
    }

    public float[] getColor() {
        return color;
    }

    public Object3DData setColor(float[] color) {
        this.color = color;
        return this;
    }

    public int getDrawMode() {
        return drawMode;
    }

    public Object3DData setDrawMode(int drawMode) {
        this.drawMode = drawMode;
        return this;
    }

    public int getDrawSize() {
        return drawSize;
    }

    public Object3DData setDrawSize(int drawSize) {
        this.drawSize = drawSize;
        return this;
    }

    public Object3DData setPosition(float[] position) {
        this.position = position;
        return this;
    }

    public float[] getPosition() {
        return position;
    }

    public float getPositionX() {
        return position != null ? position[0] : 0;
    }

    public float getPositionY() {
        return position != null ? position[1] : 0;
    }

    public float getPositionZ() {
        return position != null ? position[2] : 0;
    }

    public float[] getRotation() {
        return rotation;
    }

    public float getRotationZ() {
        return rotation != null ? rotation[2] : 0;
    }

    public Object3DData setRotation(float[] rotation) {
        this.rotation = rotation;
        return this;
    }

    public Object3DData setRotationY(float rotY) {
        this.rotation[1] = rotY;
        return this;
    }

    public IntBuffer getDrawOrder() {
        return drawOrderBuffer;
    }

}
