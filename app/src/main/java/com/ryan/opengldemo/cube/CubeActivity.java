package com.ryan.opengldemo.cube;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.widget.SeekBar;

import com.ryan.opengldemo.R;

public class CubeActivity extends Activity implements SeekBar.OnSeekBarChangeListener {

    private GLSurfaceView mCubeSurfaceView;

    private CubeRenderer mCubeRenderer;

    private SeekBar mSeekX;
    private SeekBar mSeekY;
    private SeekBar mSeekZ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cube);

        mSeekX = findViewById(R.id.seek_x);
        mSeekX.setOnSeekBarChangeListener(this);
        mSeekY = findViewById(R.id.seek_y);
        mSeekY.setOnSeekBarChangeListener(this);
        mSeekZ = findViewById(R.id.seek_z);
        mSeekZ.setOnSeekBarChangeListener(this);

        mCubeRenderer = new CubeRenderer(this);

        mCubeSurfaceView = findViewById(R.id.gl_cube);
        // 设置opengl es 2.0
        mCubeSurfaceView.setEGLContextClientVersion(2);
        mCubeSurfaceView.setRenderer(mCubeRenderer);
        mCubeSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCubeSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCubeSurfaceView.onPause();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int id = seekBar.getId();
        if (id == R.id.seek_x) {
            mCubeRenderer.setRotateX(progress);
        }
        else if (id == R.id.seek_y) {
            mCubeRenderer.setRotateY(progress);
        }
        else if (id == R.id.seek_z) {
            mCubeRenderer.setRotateZ(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
