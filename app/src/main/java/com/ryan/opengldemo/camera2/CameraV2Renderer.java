package com.ryan.opengldemo.camera2;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.ryan.opengldemo.utils.Logger;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;

public class CameraV2Renderer implements GLSurfaceView.Renderer {

    private Context mContext;
    CameraV2GLSurfaceView mCameraV2GLSurfaceView;
    CameraV2Api mCamera;
    boolean bIsPreviewStarted;
    private int mOESTextureId = -1;
    private SurfaceTexture mSurfaceTexture;
    //private float[] transformMatrix = new float[16];
    private FilterEngine mFilterEngine;
    private FloatBuffer mDataBuffer;
    private int mShaderProgram = -1;
    private int aPositionLocation = -1;
    private int aTextureCoordLocation = -1;
    //private int uTextureMatrixLocation = -1;
    private int uTextureSamplerLocation = -1;
    //private int[] mFBOIds = new int[1];

    public static final String POSITION_ATTRIBUTE = "aPosition";
    public static final String TEXTURE_COORD_ATTRIBUTE = "aTextureCoordinate";
    public static final String TEXTURE_MATRIX_UNIFORM = "uTextureMatrix";
    public static final String TEXTURE_SAMPLER_UNIFORM = "uTextureSampler";

    public void init(CameraV2GLSurfaceView surfaceView, CameraV2Api camera, boolean isPreviewStarted, Context context) {
        mContext = context;
        mCameraV2GLSurfaceView = surfaceView;
        mCamera = camera;
        bIsPreviewStarted = isPreviewStarted;
    }

    /**
     * Android的Camera及Camera2都允许使用SurfaceTexture作为预览载体，
     * 但是它们所使用的SurfaceTexture传入的OpenGL texture object name必须为GLES11Ext.GL_TEXTURE_EXTERNAL_OES。
     * 这种方式，实际上就是两个OpenGL Thread共享一个Texture，不再需要数据导入导出，从Camera采集的数据直接在GPU中完成转换和渲染。
     */
    private int createOESTextureObject() {
        int[] tex = new int[1];
        GLES20.glGenTextures(1, tex, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex[0]); // 这里就告诉TEXTURE0 ，绑定的是GL_TEXTURE_EXTERNAL_OES
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0); // 解绑
        return tex[0];
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mOESTextureId = createOESTextureObject();
        mFilterEngine = new FilterEngine(mOESTextureId, mContext);
        mDataBuffer = mFilterEngine.getBuffer();
        mShaderProgram = mFilterEngine.getShaderProgram();

        initSurfaceTexture();
        //glGenFramebuffers(1, mFBOIds, 0);
        //glBindFramebuffer(GL_FRAMEBUFFER, mFBOIds[0]);
        //Logger.d("onSurfaceCreated: mFBOId: " + mFBOIds[0]+", mOESTextureId="+mOESTextureId);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);
        Logger.d("onSurfaceChanged: " + width + ", " + height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        Long t1 = System.currentTimeMillis();
        if (mSurfaceTexture != null) {
            //所以每当摄像头有新的数据来时，我们需要通过surfaceTexture.updateTexImage()更新预览上的图像
            mSurfaceTexture.updateTexImage();
            //mSurfaceTexture.getTransformMatrix(transformMatrix);
        }

//        if (!bIsPreviewStarted) {
//            bIsPreviewStarted = initSurfaceTexture();
//            bIsPreviewStarted = true;
//            return;
//        }

        //glClear(GL_COLOR_BUFFER_BIT);
        glClearColor(1.0f, 0.0f, 0.0f, 0.0f);

        aPositionLocation = glGetAttribLocation(mShaderProgram, POSITION_ATTRIBUTE);
        aTextureCoordLocation = glGetAttribLocation(mShaderProgram, TEXTURE_COORD_ATTRIBUTE);
        //uTextureMatrixLocation = glGetUniformLocation(mShaderProgram, TEXTURE_MATRIX_UNIFORM);
        uTextureSamplerLocation = glGetUniformLocation(mShaderProgram, TEXTURE_SAMPLER_UNIFORM);

        glActiveTexture(GL_TEXTURE_EXTERNAL_OES); // 激活
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mOESTextureId); // 操作之前先绑定
        glUniform1i(uTextureSamplerLocation, 0); // 我们用的是Texture0
        //glUniformMatrix4fv(uTextureMatrixLocation, 1, false, transformMatrix, 0);

        if (mDataBuffer != null) { // 设置顶点坐标
            mDataBuffer.position(0);
            glEnableVertexAttribArray(aPositionLocation);
            glVertexAttribPointer(aPositionLocation, 2, GL_FLOAT, false, 16, mDataBuffer);

            mDataBuffer.position(2); // 设置纹理坐标
            glEnableVertexAttribArray(aTextureCoordLocation);
            glVertexAttribPointer(aTextureCoordLocation, 2, GL_FLOAT, false, 16, mDataBuffer);
        }

        //glDrawElements(GL_TRIANGLE_FAN, 6,GL_UNSIGNED_INT, 0);
        //glDrawArrays(GL_TRIANGLE_FAN, 0 , 6);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        //glDrawArrays(GL_TRIANGLES, 3, 3);

        //glBindFramebuffer(GL_FRAMEBUFFER, 0);

        long t2 = System.currentTimeMillis();
        long t = t2 - t1;
        Logger.d("onDrawFrame: time: " + t);
    }

    private boolean initSurfaceTexture() {
        if (mCamera == null || mCameraV2GLSurfaceView == null) {
            Logger.e("mCamera or mGLSurfaceView is null!");
            return false;
        }
        mSurfaceTexture = new SurfaceTexture(mOESTextureId);
        mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                mCameraV2GLSurfaceView.requestRender();
            }
        });
        mCamera.setPreviewTexture(mSurfaceTexture);
        mCamera.startPreview();
        return true;
    }
}
