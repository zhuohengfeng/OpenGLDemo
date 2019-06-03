package com.ryan.opengldemo.camera;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.ryan.opengldemo.R;

public class CameraActivity extends Activity {

    private GLSurfaceView mTextureSurfaceView;

    private CameraRenderer mTextureRenderer;

    private CameraV2Api mCamera2Api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mTextureRenderer = new CameraRenderer(this);

        mTextureSurfaceView = findViewById(R.id.gl_camera);
        mTextureSurfaceView.setEGLContextClientVersion(2);
        mTextureSurfaceView.setRenderer(mTextureRenderer);
        mTextureSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        mCamera2Api = new CameraV2Api(this);
        mTextureRenderer.setCamera(mCamera2Api);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTextureSurfaceView.onResume();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mCamera2Api.openCamera(dm.widthPixels, dm.heightPixels);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mTextureSurfaceView.onPause();
        mCamera2Api.closeCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCamera2Api.releaseCamera();
    }

    public GLSurfaceView getSurfaceView() {
        return mTextureSurfaceView;
    }
}
