package com.ryan.opengldemo.fbo;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;

import com.ryan.opengldemo.utils.Logger;
import com.ryan.opengldemo.utils.ShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class FBORenderer implements GLSurfaceView.Renderer {

    private Context mContext;

    private Bitmap mBitmap;
    private ByteBuffer mBuffer;
    private Callback mCallback;

    private int[] fFrame = new int[1];
    private int[] fRender = new int[1];
    private int[] fTexture = new int[2];

    private int mProgram;
    private int mAttrVertex;
    private int mAttrTexurePos;
    private int mUniTexureId;

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureCoordsBuffer;

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

    public FBORenderer(Context context) {
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

    private void initFBOEnv() {
        GLES20.glGenFramebuffers(1, fFrame, 0);
        // 生成RBO
        GLES20.glGenRenderbuffers(1, fRender, 0);
        // 绑定RBO
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, fRender[0]);
        // 设置RBO，颜色格式，宽度，高度
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16,
                mBitmap.getWidth(), mBitmap.getHeight());
        // 连接RBO到FBO, FBO有多个不同绑定点，这里是要绑定在FBO的深度缓冲绑定点上。
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT,
                GLES20.GL_RENDERBUFFER, fRender[0]); // 挂接一个Renderbuffer图像到FBO
        // 解绑RBO
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);

        // 生成2个纹理
        // 一个是作为数据源的texture，
        // 另外一个是用来作为输出图像的texture，这时候这个texture相当于是一块还没画东西的画布。
        GLES20.glGenTextures(2, fTexture, 0);
        for (int i = 0; i < 2; i++) {
            // 绑定纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fTexture[i]);
            if (i == 0) {
                // 第一个纹理，用来显示图片
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mBitmap, 0);
            } else {
                // 第二个纹理，渲染处理后的图片，内容是空的
                GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mBitmap.getWidth(), mBitmap.getHeight(),
                        0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
            }
            // 设置纹理的属性
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        }
        Logger.d("initFBOEnv mBitmap.getWidth()="+mBitmap.getWidth()+", mBitmap.getHeight()="+mBitmap.getHeight());
        mBuffer = ByteBuffer.allocate(mBitmap.getWidth() * mBitmap.getHeight() * 4);
    }

    private void deleteFBOEnv() {
        GLES20.glDeleteTextures(2, fTexture, 0);
        GLES20.glDeleteRenderbuffers(1, fRender, 0);
        GLES20.glDeleteFramebuffers(1, fFrame, 0);
    }

    private void initShader() {
        String vertexSource = ShaderUtil.loadFromAssetsFile("fbo_vertex.glsl" , mContext.getResources());
        String fragmentSource = ShaderUtil.loadFromAssetsFile("fbo_fragment.glsl" , mContext.getResources());
        mProgram = ShaderUtil.createProgram(vertexSource, fragmentSource);
        mAttrVertex = GLES20.glGetAttribLocation(mProgram, "aVertex");
        mAttrTexurePos = GLES20.glGetAttribLocation(mProgram, "aTexture");
        mUniTexureId = GLES20.glGetUniformLocation(mProgram, "uTextureId");
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.5f,0.5f,0.5f,1.0f);
        initVertexData();
        initShader();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (mBitmap!=null&&!mBitmap.isRecycled()) {
            Logger.d("onDrawFrame");
            initFBOEnv();

            //当一个FBO绑定以后，所有的OpenGL操作将会作用在这个绑定的帧缓冲区对象上。
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fFrame[0]);

            //把一幅空的纹理图像关联到一个FBO 一个FBO在同一个时间内可以绑定多个颜色缓冲区，每个对应FBO的一个绑定点
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                    GLES20.GL_TEXTURE_2D, fTexture[1], 0);

            // OpenGL的绘制内容会绘制到framebuffer里，
            GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, //
                    GLES20.GL_RENDERBUFFER, fRender[0]);

            GLES20.glViewport(0, 0, mBitmap.getWidth(), mBitmap.getHeight());

            GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            GLES20.glUseProgram(mProgram);

            // 把图片绘制到这个texture上
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fTexture[0]); // 我们采样器对于的是texture0，也就是保存图片的texture
            GLES20.glUniform1i(mUniTexureId, 0);

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

            // 读取渲染的像素值
            GLES20.glReadPixels(0, 0, mBitmap.getWidth(), mBitmap.getHeight(), GLES20.GL_RGBA,
                    GLES20.GL_UNSIGNED_BYTE, mBuffer);
            if(mCallback!=null){
                mCallback.onCall(mBuffer);
            }
            deleteFBOEnv();
            mBitmap.recycle();
        }
    }

    public void setBitmap(Bitmap bitmap){
        this.mBitmap=bitmap;
    }

    public void setCallback(Callback callback){
        this.mCallback=callback;
    }

    public interface Callback{
        void onCall(ByteBuffer data);
    }
}
