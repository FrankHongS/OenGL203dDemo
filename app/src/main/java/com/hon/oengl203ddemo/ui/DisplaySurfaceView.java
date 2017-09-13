package com.hon.oengl203ddemo.ui;

import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import com.hon.oengl203ddemo.controller.TouchController;
import com.hon.oengl203ddemo.ar_model_view.ModelRenderer;

/**
 * Created by Frank_Hon on 2017/4/24.
 * e-mail:frank_hon@foxmail.com
 */

public class DisplaySurfaceView extends GLSurfaceView{

    private ModelRenderer mRenderer;
    private TouchController touchHandler;

    public DisplaySurfaceView(BaseActivity context) {
        super(context);

        setEGLContextClientVersion(2);
        mRenderer=new ModelRenderer(context);
        setRenderer(mRenderer);

        touchHandler=new TouchController(context,this,mRenderer);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return touchHandler.onTouchEvent(event);
    }

}
