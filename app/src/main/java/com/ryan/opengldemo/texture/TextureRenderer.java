package com.ryan.opengldemo.texture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

class TextureRenderer implements GLSurfaceView.Renderer {

    private Context mContext;

    private int mDrawType = 0;

    private int mProgram;
    private int mAttrVertex;
    private int mAttrTexurePos;
    private int mUniTexureId;
    private int mUniDrawType;

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureCoordsBuffer;

    private float[] vertex_coords_array = new float[] {
        -1.0f, 1.0f,
        -1.0f, -1.0f,
        1.0f, 1.0f,
        1.0f, -1.0f,
    };

    private float[] texture_coords_array = new float[] {
        0.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 0.0f,
        1.0f, 1.0f
    };

    // 每个顶点有2个坐标
    private final int COORDS_PER_VERTEX = 2;
    //顶点个数
    private final int vertexCount = vertex_coords_array.length / COORDS_PER_VERTEX;
    //顶点之间的偏移量
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 每个顶点四个字节

    public TextureRenderer(Context context) {
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

    private void initTextureData() {
        int[] textureId = new int[1];
        GLES20.glGenTextures(1, textureId, 0);
        // 绑定纹理texture
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0]);

        //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_NEAREST);
        //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);
        //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE);
        //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE);

        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.girl, null);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();

        // 解绑texture --- 不能解绑，一解绑就不显示了
        //GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    private void initShader() {
        String vertexSource = ShaderUtil.loadFromAssetsFile("texture_vertex.glsl" , mContext.getResources());
        String fragmentSource = ShaderUtil.loadFromAssetsFile("texture_fragment.glsl" , mContext.getResources());
        mProgram = ShaderUtil.createProgram(vertexSource, fragmentSource);
        mAttrVertex = GLES20.glGetAttribLocation(mProgram, "aVertex");
        mAttrTexurePos = GLES20.glGetAttribLocation(mProgram, "aTexture");
        mUniTexureId = GLES20.glGetUniformLocation(mProgram, "uTextureId");
        mUniDrawType = GLES20.glGetUniformLocation(mProgram, "uDrawType");
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.5f,0.5f,0.5f,1.0f);
        initVertexData();
        initTextureData();
        initShader();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        //指定使用某套shader程序
        GLES20.glUseProgram(mProgram);

        GLES20.glUniform1i(mUniTexureId, 0);
        GLES20.glUniform1i(mUniDrawType, mDrawType);

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

    public void setDrawType(int i) {
        mDrawType = i;
    }
}
