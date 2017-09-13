package com.hon.oengl203ddemo.model.object3d_child;

import com.hon.oengl203ddemo.model.Object3DImpl;

/**
 * draw using multiple colors & textures
 * Created by Frank_Hon on 2017/4/23.
 * e-mail:frank_hon@foxmail.com
 */

public class Object3DV4 extends Object3DImpl {
    // @formatter:off
    protected final static String vertexShaderCode =
            "uniform mat4 u_MVPMatrix;" +
                    "attribute vec4 a_Position;"+
                    "attribute vec4 a_Color;"+
                    "varying vec4 vColor;"+
                    "attribute vec2 a_TexCoordinate;"+
                    "varying vec2 v_TexCoordinate;"+
                    "void main() {" +
                    "  vColor = a_Color;"+
                    "  v_TexCoordinate = a_TexCoordinate;"+
                    "  gl_Position = u_MVPMatrix * a_Position;"+
                    "}";
    // @formatter:on

    // @formatter:off
    protected final static String fragmentShaderCode =
            "precision mediump float;"+
                    "varying vec4 vColor;"+
                    "uniform sampler2D u_Texture;"+
                    "varying vec2 v_TexCoordinate;"+
                    "void main() {"	+
                    "  gl_FragColor = vColor * texture2D(u_Texture, v_TexCoordinate);"+
                    "}";
    // @formatter:on

    public Object3DV4() {
        super("V4", vertexShaderCode, fragmentShaderCode, "a_Position", "a_Color", "a_TexCoordinate");
    }

    @Override
    protected boolean supportsColors() {
        return true;
    }

    @Override
    protected boolean supportsTextures() {
        return true;
    }

}
