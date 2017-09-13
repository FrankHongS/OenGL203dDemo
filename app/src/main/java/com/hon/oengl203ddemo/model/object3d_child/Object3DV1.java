package com.hon.oengl203ddemo.model.object3d_child;

import com.hon.oengl203ddemo.model.Object3DImpl;

/**
 * draw using single color
 * Created by Frank_Hon on 2017/4/23.
 * e-mail:frank_hon@foxmail.com
 */

public class Object3DV1 extends Object3DImpl {
    // @formatter:off
    private final static String vertexShaderCode =
            "uniform mat4 u_MVPMatrix;" +
                    "attribute vec4 a_Position;" +
                    "void main() {" +
                    "  gl_Position = u_MVPMatrix * a_Position;" +
                    "}";
    // @formatter:on

    // @formatter:off
    private final static String fragmentShaderCode =
            "precision mediump float;"+
                    "uniform vec4 vColor;" +
                    "void main() {"+
                    "  gl_FragColor = vColor;" +
                    "}";
    // @formatter:on

    public Object3DV1() {
        super("V1", vertexShaderCode, fragmentShaderCode, "a_Position");
    }

    @Override
    protected boolean supportsColors() {
        return false;
    }
}
