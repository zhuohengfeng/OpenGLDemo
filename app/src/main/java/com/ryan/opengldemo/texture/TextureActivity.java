package com.ryan.opengldemo.texture;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.ryan.opengldemo.R;

public class TextureActivity extends Activity {

    private GLSurfaceView mTextureSurfaceView;

    private TextureRenderer mTextureRenderer;

    private CheckBox mCbFilter;
    private CheckBox mCbEye;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texture);

        mTextureRenderer = new TextureRenderer(this);

        mTextureSurfaceView = findViewById(R.id.gl_texture);
        mTextureSurfaceView.setEGLContextClientVersion(2);
        mTextureSurfaceView.setRenderer(mTextureRenderer);
        mTextureSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        mCbFilter = findViewById(R.id.cb_filter);
        mCbFilter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mTextureRenderer.setFilter(isChecked);
                mTextureSurfaceView.requestRender();
            }
        });
        mCbEye = findViewById(R.id.cb_eys);
        mCbEye.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mTextureRenderer.setBigEye(isChecked);
                mTextureSurfaceView.requestRender();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTextureSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mTextureSurfaceView.onPause();
    }

}
