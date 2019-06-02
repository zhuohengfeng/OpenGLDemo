package com.ryan.opengldemo.texture;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.ryan.opengldemo.R;

public class TextureActivity extends Activity {

    private GLSurfaceView mTextureSurfaceView;

    private TextureRenderer mTextureRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texture);

        mTextureRenderer = new TextureRenderer(this);

        mTextureSurfaceView = findViewById(R.id.gl_texture);
        // 设置opengl es 2.0
        mTextureSurfaceView.setEGLContextClientVersion(2);
        mTextureSurfaceView.setRenderer(mTextureRenderer);
        mTextureSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
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
