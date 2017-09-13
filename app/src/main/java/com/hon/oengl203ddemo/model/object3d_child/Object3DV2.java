package com.hon.oengl203ddemo.model.object3d_child;

import com.hon.oengl203ddemo.model.Object3DImpl;

/**
 * draw using multiple colors
 * Created by Frank_Hon on 2017/4/23.
 * e-mail:frank_hon@foxmail.com
 */

public class Object3DV2 extends Object3DImpl {
    // @formatter:off
    private final static String vertexShaderCode =
            "uniform mat4 u_MVPMatrix;" +
                    "attribute vec4 a_Position;" +
                    "attribute vec4 a_Color;"+
                    "varying vec4 vColor;"+
                    "void main() {" +
                    "  vColor = a_Color;"+
                    "  gl_Position = u_MVPMatrix * a_Position;" +
                    "}";
    // @formatter:on

    // @formatter:off
    private final static String fragmentShaderCode =
            "precision mediump float;"+
                    "varying vec4 vColor;"+
                    "void main() {"+
                    "  gl_FragColor = vColor;" +
                    "}";
    // @formatter:on

    public Object3DV2() {
        super("V2", vertexShaderCode, fragmentShaderCode, "a_Position", "a_Color");
    }

    @Override
    protected boolean supportsColors() {
        return true;
    }
}
