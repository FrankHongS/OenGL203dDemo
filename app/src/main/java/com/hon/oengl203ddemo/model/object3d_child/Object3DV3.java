package com.hon.oengl203ddemo.model.object3d_child;

import com.hon.oengl203ddemo.model.Object3DImpl;

/**
 * draw using single color & textures
 * Created by Frank_Hon on 2017/4/23.
 * e-mail:frank_hon@foxmail.com
 */

public class Object3DV3 extends Object3DImpl {
    // @formatter:off
    private final static String vertexShaderCode =
            "uniform mat4 u_MVPMatrix;" +
                    "attribute vec4 a_Position;" +
                    "attribute vec2 a_TexCoordinate;"+ // Per-vertex texture coordinate information we will pass in.
                    "varying vec2 v_TexCoordinate;"+   // This will be passed into the fragment shader.
                    "void main() {" +
                    "  v_TexCoordinate = a_TexCoordinate;"+
                    "  gl_Position = u_MVPMatrix * a_Position;" +
                    "}";
    // @formatter:on

    // @formatter:off
    private final static String fragmentShaderCode =
            "precision mediump float;"+
                    "uniform vec4 vColor;"+
                    "uniform sampler2D u_Texture;"+
                    "varying vec2 v_TexCoordinate;"+
                    "void main() {"	+
                    "  gl_FragColor = vColor * texture2D(u_Texture, v_TexCoordinate);"+
                    "}";
    // @formatter:on

    public Object3DV3() {
        super("V3", vertexShaderCode, fragmentShaderCode, "a_Position", "a_TexCoordinate");
    }

    @Override
    protected boolean supportsTextures() {
        return true;
    }
}
