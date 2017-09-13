package com.hon.oengl203ddemo.model.object3d_child;

import com.hon.oengl203ddemo.model.Object3DImpl;

/**
 * draw a single point
 * Created by Frank_Hon on 2017/4/23.
 * e-mail:frank_hon@foxmail.com
 */

public class Object3DV0 extends Object3DImpl{
    // @formatter:off
    private static final String pointVertexShader =
            "uniform mat4 u_MVPMatrix;      \n"
                    +	"attribute vec4 a_Position;     \n"
                    + "void main()                    \n"
                    + "{                              \n"
                    + "   gl_Position = u_MVPMatrix  * a_Position;   \n"
                    + "   gl_PointSize = 20.0;         \n"
                    + "}                              \n";
    // @formatter:on

    // @formatter:off
    private static final String pointFragmentShader =
            "precision mediump float;       \n"
                    + "void main()                    \n"
                    + "{                              \n"
                    + "   gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);             \n"
                    + "}                              \n";
    // @formatter:on

    public Object3DV0() {
        super("V0", pointVertexShader, pointFragmentShader, "a_Position");
    }
}
