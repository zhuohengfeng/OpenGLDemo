package com.ryan.opengldemo.camera;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class CameraV2GLSurfaceView extends GLSurfaceView {
    private CameraV2Renderer mCameraV2Renderer;

    public void init(CameraV2Api camera, boolean isPreviewStarted, Context context) {
        setEGLContextClientVersion(2);

        mCameraV2Renderer = new CameraV2Renderer();
        mCameraV2Renderer.init(this, camera, isPreviewStarted, context);
        setRenderer(mCameraV2Renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public CameraV2GLSurfaceView(Context context) {
        super(context);
    }
}
