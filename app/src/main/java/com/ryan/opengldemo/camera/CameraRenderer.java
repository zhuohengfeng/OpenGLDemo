package com.ryan.opengldemo.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;

import com.ryan.opengldemo.R;
import com.ryan.opengldemo.utils.Logger;
import com.ryan.opengldemo.utils.ShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glUniform1i;

class CameraRenderer implements GLSurfaceView.Renderer {

    private Context mContext;

    private CameraV2Api mCameraV2Api;

    private int mProgram;
    private int mAttrVertex;
    private int mAttrTexurePos;
    private int mUniTexureId;
    private int mUniBeautyLocation;
    private int uTextureMatrixLocation;

    private boolean mIsBeautyOn = false;

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureCoordsBuffer;

    private float[] transformMatrix = new float[16];
    private int mOESTextureId;

    private float[] vertex_coords_array = new float[] {
        -1.0f, 1.0f,
        -1.0f, -1.0f,
        1.0f, 1.0f,
        1.0f, -1.0f,
    };

    private float[] texture_coords_array = new float[] {
        0.0f, 1.0f,
        0.0f, 0.0f,
        1.0f, 1.0f,
        1.0f, 0.0f,
    };

    // 每个顶点有2个坐标
    private final int COORDS_PER_VERTEX = 2;
    //顶点个数
    private final int vertexCount = vertex_coords_array.length / COORDS_PER_VERTEX;
    //顶点之间的偏移量
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 每个顶点四个字节

    public CameraRenderer(Context context) {
        mContext = context;
    }

    private void initVertexData() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(vertex_coords_array.length *4);
        buffer.order(ByteOrder.nativeOrder());
        mVertexBuffer = buffer.asFloatBuffer();
        mVertexBuffer.put(vertex_coords_array);
        mVertexBuffer.position(0);

        ByteBuffer texture_buffer = ByteBuffer.allocateDirect(texture_coords_array.length *4);
        texture_buffer.order(ByteOrder.nativeOrder());
        mTextureCoordsBuffer = texture_buffer.asFloatBuffer();
        mTextureCoordsBuffer.put(texture_coords_array);
        mTextureCoordsBuffer.position(0);
    }

    /**
     * Android的Camera及Camera2都允许使用SurfaceTexture作为预览载体，
     * 但是它们所使用的SurfaceTexture传入的OpenGL texture object name必须为GLES11Ext.GL_TEXTURE_EXTERNAL_OES。
     * 这种方式，实际上就是两个OpenGL Thread共享一个Texture，不再需要数据导入导出，从Camera采集的数据直接在GPU中完成转换和渲染。
     */
    private int initTextureData() {
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

    private void initShader() {
        String vertexSource = ShaderUtil.loadFromAssetsFile("camera_vertex.glsl" , mContext.getResources());
        String fragmentSource = ShaderUtil.loadFromAssetsFile("camera_fragment.glsl" , mContext.getResources());
        mProgram = ShaderUtil.createProgram(vertexSource, fragmentSource);
        mAttrVertex = GLES20.glGetAttribLocation(mProgram, "aVertex");
        mAttrTexurePos = GLES20.glGetAttribLocation(mProgram, "aTexture");
        mUniTexureId = GLES20.glGetUniformLocation(mProgram, "uTextureId");
        uTextureMatrixLocation = GLES20.glGetUniformLocation(mProgram, "uTextureMatrix");
        mUniBeautyLocation = GLES20.glGetUniformLocation(mProgram, "uBeauty");
    }

    public void setCamera(CameraV2Api camera2Api) {
        mCameraV2Api = camera2Api;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.5f,0.5f,0.5f,1.0f);
        initVertexData();
        initShader();
        mOESTextureId = initTextureData();
        // 启动预览
        mCameraV2Api.startPreview(new SurfaceTexture(mOESTextureId));
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        mCameraV2Api.updateTexImage(transformMatrix);

        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        //指定使用某套shader程序
        GLES20.glUseProgram(mProgram);

        GLES20.glActiveTexture(GL_TEXTURE_EXTERNAL_OES); // 激活
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mOESTextureId); // 操作之前先绑定
        GLES20.glUniform1i(mUniTexureId, 0);
        GLES20.glUniform1i(mUniBeautyLocation, mIsBeautyOn ? 1 : 0);

        GLES20.glUniformMatrix4fv(uTextureMatrixLocation, 1, false, transformMatrix, 0);

        //将顶点位置数据传送进渲染管线
        GLES20.glVertexAttribPointer(
                mAttrVertex, // 顶点坐标引用
                2, // 每个顶点有3个值x, y
                GLES20.GL_FLOAT, // 顶点类型
                false, // 是否需要归一化
                vertexStride, // 每个值占4个字节
                mVertexBuffer
        );
        //将顶点颜色数据传送进渲染管线
        GLES20.glVertexAttribPointer(
                mAttrTexurePos, // 顶点颜色引用
                2,
                GLES20.GL_FLOAT,
                false,
                vertexStride, // 每个值占4个字节,  stride指定从一个属性到下一个属性的字节跨度，允许将顶点和属性打包到单个数组中或存储在单独的数组中
                mTextureCoordsBuffer
        );

        GLES20.glEnableVertexAttribArray(mAttrVertex);//启用顶点位置数据
        GLES20.glEnableVertexAttribArray(mAttrTexurePos);//启用顶点着色数据

        //绘制三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexCount); // 有3个顶点，就是一个普通的三角形

        GLES20.glDisableVertexAttribArray(mAttrVertex);//启用顶点位置数据
        GLES20.glDisableVertexAttribArray(mAttrTexurePos);//启用顶点着色数据
    }


    public void setBeauty(boolean isChecked) {
        mIsBeautyOn = isChecked;
    }
}
