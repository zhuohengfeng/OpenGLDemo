package com.ryan.opengldemo.triangle;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.ryan.opengldemo.utils.Logger;
import com.ryan.opengldemo.utils.ShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

class TriangleRenderer implements GLSurfaceView.Renderer {

    private Context mContext;

    private int mProgram;
    private int mAttrVertex;
    private int mAttrColor;

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mColorBuffer;

    private float[] vertex_array = new float[] {
        0.0f, 0.5f, 0.0f,
        -0.5f, 0.0f, 0.0f,
        0.5f, 0.0f, 0.0f
    };

    private float[] color_array = new float[] {
        1.0f, 0.0f, 0.0f,
        0.0f, 1.0f, 0.0f,
        0.0f, 0.0f, 1.0f
    };

    // 每个顶点有3个坐标
    private final int COORDS_PER_VERTEX = 3;
    //顶点个数
    private final int vertexCount = vertex_array.length / COORDS_PER_VERTEX; // 3
    //顶点之间的偏移量
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 每个顶点四个字节

    public TriangleRenderer(Context context) {
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

    private void initShader() {
        String vertexSource = ShaderUtil.loadFromAssetsFile("triangle_vertex.glsl" , mContext.getResources());
        String fragmentSource = ShaderUtil.loadFromAssetsFile("triangle_fragment.glsl" , mContext.getResources());
        mProgram = ShaderUtil.createProgram(vertexSource, fragmentSource);
        mAttrVertex = GLES20.glGetAttribLocation(mProgram, "aVertex");
        mAttrColor = GLES20.glGetAttribLocation(mProgram, "aColor");
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Logger.d("zhfzhf", "onSurfaceCreated");
        GLES20.glClearColor(0.5f,0.5f,0.5f,1.0f);
        initVertexData();
        initColorData();
        initShader();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Logger.d("zhfzhf", "onSurfaceChanged width="+width+", height="+height);
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        Logger.d("zhfzhf", "onDrawFrame");
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        //指定使用某套shader程序
        GLES20.glUseProgram(mProgram);
        //将顶点位置数据传送进渲染管线
        GLES20.glVertexAttribPointer(
                mAttrVertex, // 顶点坐标引用
                3, // 每个顶点有3个值x, y, z
                GLES20.GL_FLOAT, // 顶点类型
                false, // 是否需要归一化，不需要，因为我们已经是在-1 ~ 1的范围内了
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
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount); // 有3个顶点，就是一个普通的三角形

        GLES20.glDisableVertexAttribArray(mAttrVertex);//启用顶点位置数据
        GLES20.glDisableVertexAttribArray(mAttrColor);//启用顶点着色数据
    }
}
