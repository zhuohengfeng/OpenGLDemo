package com.ryan.opengldemo.triangle;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.ryan.opengldemo.R;

public class TriangleActivity extends Activity {

    private GLSurfaceView mTriangleSurfaceView;

    private TriangleRenderer mTriangleRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_triangle);

        mTriangleRenderer = new TriangleRenderer(this);

        mTriangleSurfaceView = findViewById(R.id.gl_triangle);
        // 设置opengl es 2.0
        mTriangleSurfaceView.setEGLContextClientVersion(2);
        mTriangleSurfaceView.setRenderer(mTriangleRenderer);
        mTriangleSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTriangleSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mTriangleSurfaceView.onPause();
    }

}
