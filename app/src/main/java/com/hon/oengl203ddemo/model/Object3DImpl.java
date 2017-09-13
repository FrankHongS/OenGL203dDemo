package com.hon.oengl203ddemo.model;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.hon.oengl203ddemo.utils.GLUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

/**
 * Created by Frank_Hon on 2017/4/23.
 * e-mail:frank_hon@foxmail.com
 */

public class Object3DImpl implements Object3D {
    private final String id;
    // Transformations
    private final float[] mMatrix = new float[16];
    // mvp matrix
    private final float[] mvMatrix = new float[16];
    private final float[] mvpMatrix = new float[16];
    // OpenGL data
    private final int mProgram;
    // put 0 to draw progressively, -1 to draw at once
    private long counter = -1;

    public Object3DImpl(String id, String vertexShaderCode, String fragmentShaderCode, String... variables) {
        this.id = id;
        // prepare shaders and OpenGL program
        int vertexShader = GLUtils.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = GLUtils.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        mProgram = GLUtils.createAndLinkProgram(vertexShader, fragmentShader, variables);
    }

    @Override
    public void draw(Object3DData obj, float[] pMatrix, float[] vMatrix, int textureId, float[] lightPos) {
        this.draw(obj, pMatrix, vMatrix, obj.getDrawMode(), obj.getDrawSize(), textureId, lightPos);
    }

    @Override
    public void draw(Object3DData obj, float[] pMatrix, float[] vMatrix, int drawMode, int drawSize, int textureId,
                     float[] lightPos) {

        // Log.d("Object3DImpl", "Drawing '" + obj.getId() + "' using shader '" + id + "'...");

        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);

        float[] mMatrix = getMMatrix(obj);
        float[] mvMatrix = getMvMatrix(mMatrix, vMatrix);
        float[] mvpMatrix = getMvpMatrix(mvMatrix, pMatrix);

        setMvpMatrix(mvpMatrix);

        int mPositionHandle = setPosition(obj);

        int mColorHandle = -1;
        if (supportsColors()) {
            mColorHandle = setColors(obj);
        } else {
            setColor(obj);
        }

        int mTextureHandle = -1;
        if (textureId != -1 && supportsTextures()) {
            setTexture(obj, textureId);
        }

        int mNormalHandle = -1;
        if (supportsNormals()) {
            mNormalHandle = setNormals(obj);
        }

        if (supportsMvMatrix()) {
            setMvMatrix(mvMatrix);
        }

        if (lightPos != null && supportsLighting()) {
            // float[] lightPosInEyeSpace = new float[4];
            // Matrix.multiplyMV(lightPosInEyeSpace, 0, vMatrix, 0, lightPos, 0);
            // float[] mvMatrixLight = new float[16];
            // // Matrix.multiplyMM(mvMatrixLight, 0, vMatrix, 0, mMatrixLight, 0);
            // float[] mvpMatrixLight = new float[16];
            // Matrix.multiplyMM(mvpMatrixLight, 0, pMatrix, 0, mvMatrixLight, 0);
            setLightPos(lightPos);
        }

        drawShape(obj, drawMode, drawSize);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);

        if (mColorHandle != -1) {
            GLES20.glDisableVertexAttribArray(mColorHandle);
        }

        // Disable vertex array
        if (mTextureHandle != -1) {
            GLES20.glDisableVertexAttribArray(mTextureHandle);
        }

        if (mNormalHandle != -1) {
            GLES20.glDisableVertexAttribArray(mNormalHandle);
        }
    }

    public float[] getMMatrix(Object3DData obj) {

        // calculate object transformation
        Matrix.setIdentityM(mMatrix, 0);
        if (obj.getRotation() != null) {
            Matrix.rotateM(mMatrix, 0, obj.getRotation()[0], 1f, 0f, 0f);
            Matrix.rotateM(mMatrix, 0, obj.getRotation()[1], 0, 1f, 0f);
            Matrix.rotateM(mMatrix, 0, obj.getRotationZ(), 0, 0, 1f);
        }
        Matrix.translateM(mMatrix, 0, obj.getPositionX(), obj.getPositionY(), obj.getPositionZ());
        return mMatrix;
    }

    public float[] getMvMatrix(float[] mMatrix, float[] vMatrix) {
        Matrix.multiplyMM(mvMatrix, 0, vMatrix, 0, mMatrix, 0);
        return mvMatrix;
    }

    protected float[] getMvpMatrix(float[] mvMatrix, float[] pMatrix) {
        Matrix.multiplyMM(mvpMatrix, 0, pMatrix, 0, mvMatrix, 0);
        return mvpMatrix;
    }

    protected void setMvpMatrix(float[] mvpMatrix) {

        // get handle to shape's transformation matrix
        int mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_MVPMatrix");
        GLUtils.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        GLUtils.checkGlError("glUniformMatrix4fv");
    }

    protected boolean supportsColors() {
        return false;
    }

    protected void setColor(Object3DData obj) {

        // get handle to fragment shader's vColor member
        int mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        GLUtils.checkGlError("glGetUniformLocation");

        // Set color for drawing the triangle
        float[] color = obj.getColor() != null ? obj.getColor() : DEFAULT_COLOR;
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);
        GLUtils.checkGlError("glUniform4fv");
    }

    protected int setColors(Object3DData obj) {

        // get handle to fragment shader's vColor member
        int mColorHandle = GLES20.glGetAttribLocation(mProgram, "a_Color");
        GLUtils.checkGlError("glGetAttribLocation");

        // Pass in the color information
        GLES20.glEnableVertexAttribArray(mColorHandle);
        GLUtils.checkGlError("glEnableVertexAttribArray");

        obj.getVertexColorsArrayBuffer().position(0);
        GLES20.glVertexAttribPointer(mColorHandle, 4, GLES20.GL_FLOAT, false, 0, obj.getVertexColorsArrayBuffer());
        GLUtils.checkGlError("glVertexAttribPointer");

        return mColorHandle;
    }

    protected int setPosition(Object3DData obj) {

        // get handle to vertex shader's a_Position member
        int mPositionHandle = GLES20.glGetAttribLocation(mProgram, "a_Position");
        GLUtils.checkGlError("glGetAttribLocation");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLUtils.checkGlError("glEnableVertexAttribArray");

        FloatBuffer vertexBuffer = obj.getVertexArrayBuffer() != null ? obj.getVertexArrayBuffer()
                : obj.getVertexBuffer();
        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, VERTEX_STRIDE,
                vertexBuffer);

        return mPositionHandle;
    }

    protected boolean supportsNormals() {
        return false;
    }

    protected int setNormals(Object3DData obj) {
        int mNormalHandle = GLES20.glGetAttribLocation(mProgram, "a_Normal");
        GLUtils.checkGlError("glGetAttribLocation");

        GLES20.glEnableVertexAttribArray(mNormalHandle);
        GLUtils.checkGlError("glEnableVertexAttribArray");

        // Pass in the normal information
        obj.getVertexNormalsArrayBuffer().position(0);
        GLES20.glVertexAttribPointer(mNormalHandle, 3, GLES20.GL_FLOAT, false, 0, obj.getVertexNormalsArrayBuffer());

        return mNormalHandle;
    }

    protected boolean supportsLighting() {
        return true;
    }

    protected void setLightPos(float[] lightPosInEyeSpace) {
        int mLightPosHandle = GLES20.glGetUniformLocation(mProgram, "u_LightPos");
        // Pass in the light position in eye space.
        GLES20.glUniform3f(mLightPosHandle, lightPosInEyeSpace[0], lightPosInEyeSpace[1], lightPosInEyeSpace[2]);
    }

    protected boolean supportsMvMatrix() {
        return false;
    }

    protected void setMvMatrix(float[] mvMatrix) {
        int mMVMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_MVMatrix");
        GLUtils.checkGlError("glGetUniformLocation");

        // Pass in the modelview matrix.
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mvMatrix, 0);
        GLUtils.checkGlError("glUniformMatrix4fv");
    }

    protected boolean supportsTextures() {
        return false;
    }

    protected int setTexture(Object3DData obj, int textureId) {
        int mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram, "u_Texture");
        GLUtils.checkGlError("glGetUniformLocation");

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLUtils.checkGlError("glActiveTexture");

        // Bind to the texture in OpenGL
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLUtils.checkGlError("glBindTexture");

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0);
        GLUtils.checkGlError("glUniform1i");

        int mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgram, "a_TexCoordinate");
        GLUtils.checkGlError("glGetAttribLocation");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);
        GLUtils.checkGlError("glEnableVertexAttribArray");

        // Prepare the triangle coordinate data
        obj.getTextureCoordsArrayBuffer().position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, 2, GLES20.GL_FLOAT, false, 0,
                obj.getTextureCoordsArrayBuffer());
        GLUtils.checkGlError("glVertexAttribPointer");

        return mTextureCoordinateHandle;
    }

    protected void drawShape(Object3DData obj, int drawMode, int drawSize) {
        FloatBuffer vertexBuffer = obj.getVertexArrayBuffer() != null ? obj.getVertexArrayBuffer()
                : obj.getVertexBuffer();
        vertexBuffer.position(0);
        List<int[]> drawModeList = obj.getDrawModeList();
        IntBuffer drawOrderBuffer = obj.getDrawOrder();

        if (drawModeList != null) {
            if (drawOrderBuffer == null) {
                Log.d(obj.getId(),"Drawing single polygons using arrays...");
                for (int j=0; j<drawModeList.size(); j++) {
                    int[] polygon = drawModeList.get(j);
                    int drawModePolygon = polygon[0];
                    int vertexPos = polygon[1];
                    int drawSizePolygon = polygon[2];
                    if (drawMode == GLES20.GL_LINE_LOOP && polygon[2] > 3) {
                        // is this wireframe?
                        // Log.v("Object3DImpl","Drawing wireframe for '" + obj.getId() + "' (" + drawSizePolygon + ")...");
                        for (int i = 0; i < polygon[2] - 2; i++) {
                            // Log.v("Object3DImpl","Drawing wireframe triangle '" + i + "' for '" + obj.getId() + "'...");
                            GLES20.glDrawArrays(drawMode, polygon[1] + i, 3);
                        }
                    } else {
                        GLES20.glDrawArrays(drawMode, polygon[1], polygon[2]);
                    }
                }
            } else {
                // Log.d(obj.getId(),"Drawing single polygons using elements...");
                for (int i=0; i<drawModeList.size(); i++) {
                    int[] drawPart = drawModeList.get(i);
                    int drawModePolygon = drawPart[0];
                    int vertexPos = drawPart[1];
                    int drawSizePolygon = drawPart[2];
                    drawOrderBuffer.position(vertexPos);
                    GLES20.glDrawElements(drawModePolygon, drawSizePolygon, GLES20.GL_UNSIGNED_INT, drawOrderBuffer);
                }
            }
        } else {
            if (drawOrderBuffer != null) {
                if (drawSize <= 0) {
                    //Log.d(obj.getId(),"Drawing all elements with mode '"+drawMode+"'...");
                    drawOrderBuffer.position(0);
                    GLES20.glDrawElements(drawMode, drawOrderBuffer.capacity(), GLES20.GL_UNSIGNED_INT,
                            drawOrderBuffer);
                } else {
                    //Log.d(obj.getId(),"Drawing single elements of size '"+drawSize+"'...");
                    for (int i = 0; i < drawOrderBuffer.capacity(); i += drawSize) {
                        drawOrderBuffer.position(i);
                        GLES20.glDrawElements(drawMode, drawSize, GLES20.GL_UNSIGNED_INT, drawOrderBuffer);
                    }
                }
            } else {
                if (drawSize <= 0) {
                    int drawCount = vertexBuffer.capacity() / COORDS_PER_VERTEX;

                    // if we want to animate, initialize counter=0 at variable declaration
                    // Log.d(obj.getId(),"Drawing all triangles using arrays...");
                    if (this.counter >= 0) {
                        counter += 100;
                        counter = counter % Integer.MAX_VALUE;
                        drawCount = (int)counter % drawCount + 1;
                    }

                    GLES20.glDrawArrays(drawMode, 0, drawCount);
                } else {
                    //Log.d(obj.getId(),"Drawing single triangles using arrays...");
                    for (int i = 0; i < vertexBuffer.capacity() / COORDS_PER_VERTEX; i += drawSize) {
                        GLES20.glDrawArrays(drawMode, i, drawSize);
                    }
                }
            }
        }
    }
}
