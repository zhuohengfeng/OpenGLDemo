package com.ryan.opengldemo.cube;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.ryan.opengldemo.utils.MatrixState;
import com.ryan.opengldemo.utils.ShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

class CubeRenderer implements GLSurfaceView.Renderer {

    private Context mContext;

    private int mProgram;
    private int mAttrVertex;
    private int mAttrColor;
    private int mUniMVPMatrix; // 总变换矩阵引用

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mColorBuffer;
    private ShortBuffer mIndexBuffer;

    // 一个立方体一共有8个顶点
    final float vertex_array[] = {
            -1.0f,1.0f,1.0f,    //正面左上0
            -1.0f,-1.0f,1.0f,   //正面左下1
            1.0f,-1.0f,1.0f,    //正面右下2
            1.0f,1.0f,1.0f,     //正面右上3
            -1.0f,1.0f,-1.0f,    //反面左上4
            -1.0f,-1.0f,-1.0f,   //反面左下5
            1.0f,-1.0f,-1.0f,    //反面右下6
            1.0f,1.0f,-1.0f,     //反面右上7
    };

    final short index_array[]={
            6,7,4,6,4,5,    //后面
            6,3,7,6,2,3,    //右面
            6,5,1,6,1,2,    //下面
            0,3,2,0,2,1,    //正面
            0,1,5,0,5,4,    //左面
            0,7,3,0,4,7,    //上面
    };

    float color_array[] = {
            0f,1f,0f,1f,
            0f,1f,0f,1f,
            0f,1f,0f,1f,
            0f,1f,0f,1f,
            1f,0f,0f,1f,
            1f,0f,0f,1f,
            1f,0f,0f,1f,
            1f,0f,0f,1f,
    };

    // 每个顶点有3个坐标
    private final int COORDS_PER_VERTEX = 3;
    //顶点之间的偏移量
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 每个顶点四个字节

    public CubeRenderer(Context context) {
        mContext = context;
    }

    private void initVertexData() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(vertex_array.length *4);
        buffer.order(ByteOrder.nativeOrder());
        mVertexBuffer = buffer.asFloatBuffer();
        mVertexBuffer.put(vertex_array);
        mVertexBuffer.position(0);
    }

    private void initColorData() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(color_array.length *4);
        buffer.order(ByteOrder.nativeOrder());
        mColorBuffer = buffer.asFloatBuffer();
        mColorBuffer.put(color_array);
        mColorBuffer.position(0);
    }

    private void initIndex() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(index_array.length *2);
        buffer.order(ByteOrder.nativeOrder());
        mIndexBuffer = buffer.asShortBuffer();
        mIndexBuffer.put(index_array);
        mIndexBuffer.position(0);
    }

    private void initShader() {
        String vertexSource = ShaderUtil.loadFromAssetsFile("cube_vertex.glsl" , mContext.getResources());
        String fragmentSource = ShaderUtil.loadFromAssetsFile("cube_fragment.glsl" , mContext.getResources());
        mProgram = ShaderUtil.createProgram(vertexSource, fragmentSource);
        mAttrVertex = GLES20.glGetAttribLocation(mProgram, "aVertex");
        mAttrColor = GLES20.glGetAttribLocation(mProgram, "aColor");
        mUniMVPMatrix = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.5f,0.5f,0.5f,1.0f);
        initVertexData();
        initColorData();
        initIndex();
        initShader();

        // 打开深度检测
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        // 打开背面裁剪
        GLES20.glEnable(GLES20.GL_CULL_FACE);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float)width/height;
        // 设置投影矩阵, 这里的near表示垂直于观察方向的近平面，far表示垂直于观察方向的远平面
        // 这里的near, far不是坐标，就是离摄像机的距离
        // 如果距离越近，投影出来的大小越小
        MatrixState.setProjectFrustum(-ratio, ratio, -1, 1, 3, 20);
        // 设置相机
        MatrixState.setCamera(0f, 0f, 10.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        MatrixState.setInitStack();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        //指定使用某套shader程序
        GLES20.glUseProgram(mProgram);

        //传入总的变化矩阵
        GLES20.glUniformMatrix4fv(mUniMVPMatrix, 1, false, MatrixState.getFinalMatrix() , 0);

        //将顶点位置数据传送进渲染管线
        GLES20.glVertexAttribPointer(
                mAttrVertex, // 顶点坐标引用
                3, // 每个顶点有3个值x, y, z
                GLES20.GL_FLOAT, // 顶点类型
                false, // 是否需要归一化
                vertexStride, // 每个值占4个字节
                mVertexBuffer
        );
        //将顶点颜色数据传送进渲染管线
        GLES20.glVertexAttribPointer(
                mAttrColor, // 顶点颜色引用
                3, // 每个顶点有3个值，  G, B, A
                GLES20.GL_FLOAT,
                false,
                vertexStride, // 每个值占4个字节,  stride指定从一个属性到下一个属性的字节跨度，允许将顶点和属性打包到单个数组中或存储在单独的数组中
                mColorBuffer
        );
        GLES20.glEnableVertexAttribArray(mAttrVertex);//启用顶点位置数据
        GLES20.glEnableVertexAttribArray(mAttrColor);//启用顶点着色数据
        //绘制三角形
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, index_array.length, GLES20.GL_UNSIGNED_SHORT, mIndexBuffer); // 有3个顶点，就是一个普通的三角形

        GLES20.glDisableVertexAttribArray(mAttrVertex);//启用顶点位置数据
        GLES20.glDisableVertexAttribArray(mAttrColor);//启用顶点着色数据
    }

    public void setRotateX(int progress) {
        float angle = 360f * progress / 100;
        MatrixState.rotate(angle, 1.0f, 0f, 0f);
    }

    public void setRotateY(int progress) {
        float angle = 360f * progress / 100;
        MatrixState.rotate(angle, 0f, 1.0f, 0f);
    }

    public void setRotateZ(int progress) {
        float angle = 360f * progress / 100;
        MatrixState.rotate(angle, 0f, 0f, 1.0f);
    }
}
