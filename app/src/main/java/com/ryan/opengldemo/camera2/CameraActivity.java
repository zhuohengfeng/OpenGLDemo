package com.ryan.opengldemo.camera2;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;

import com.ryan.opengldemo.utils.Logger;

public class CameraActivity extends Activity {

    private CameraV2GLSurfaceView mCameraV2GLSurfaceView;
    private CameraV2Api mCamera;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCameraV2GLSurfaceView = new CameraV2GLSurfaceView(this);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mCamera = new CameraV2Api(this);
        mCamera.setupCamera(dm.widthPixels, dm.heightPixels);
        if (!mCamera.openCamera()) {
            Logger.e("failed to open camera");
            return;
        }
        mCameraV2GLSurfaceView.init(mCamera, false, CameraActivity.this);
        setContentView(mCameraV2GLSurfaceView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraV2GLSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraV2GLSurfaceView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCamera != null) {
            mCamera.releaseCamera();
        }
    }
}
