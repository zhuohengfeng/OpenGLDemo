package com.ryan.opengldemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.ryan.opengldemo.camera.CameraActivity;
import com.ryan.opengldemo.cube.CubeActivity;
import com.ryan.opengldemo.egl.EGLActivity;
import com.ryan.opengldemo.fbo.FBOActivity;
import com.ryan.opengldemo.texture.TextureActivity;
import com.ryan.opengldemo.triangle.TriangleActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    // 绘制三角形
    public void onStartTriangle(View view) {
        startRenderActivity(TriangleActivity.class);
    }

    // 绘制立方体
    public void onStartCube(View view) {
        startRenderActivity(CubeActivity.class);
    }

    // 绘制图片
    public void onStartTexture(View view) {
        startRenderActivity(TextureActivity.class);
    }

    // 绘制相机预览数据
    public void onStartCamera(View view) {
        startRenderActivity(CameraActivity.class);
    }

    // FBO的使用
    public void onStartFBO(View view) {
        startRenderActivity(FBOActivity.class);
    }

    // EGL的使用
    public void onStartEGL(View view) {
        startRenderActivity(EGLActivity.class);
    }

    private void startRenderActivity(Class<?> toClass) {
        Intent intent = new Intent(this, toClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
    }


}
