package com.hon.oengl203ddemo.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Window;
import android.widget.FrameLayout;

import com.hon.oengl203ddemo.R;

/**
 * Created by Frank_Hon on 2017/4/24.
 * e-mail:frank_hon@foxmail.com
 */

public class DisplayActivity extends BaseActivity {

    private FrameLayout fl_container;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_display_layout);
        fl_container=(FrameLayout)findViewById(R.id.fl_container);
        mSurfaceView=new DisplaySurfaceView(this);
        fl_container.addView(mSurfaceView);

        initSceneLoader();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSurfaceView.onPause();
    }

}
