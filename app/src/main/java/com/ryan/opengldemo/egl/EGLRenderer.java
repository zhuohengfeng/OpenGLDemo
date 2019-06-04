package com.ryan.opengldemo.egl;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;

import com.ryan.opengldemo.fbo.FBORenderer;
import com.ryan.opengldemo.utils.Logger;
import com.ryan.opengldemo.utils.ShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class EGLRenderer {

    private Context mContext;

    private EGLHelper mEGLHelper;

    private Bitmap mBitmap;
    private int mWidth;
    private int mHeight;

    private int mProgram;
    private int mAttrVertex;
    private int mAttrTexurePos;
    private int mUniTexureId;

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureCoordsBuffer;

    private int mTextureId;

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
            1.0f, 1.0f,
    };

    // 每个顶点有2个坐标
    private final int COORDS_PER_VERTEX = 2;
    //顶点个数
    private final int vertexCount = vertex_coords_array.length / COORDS_PER_VERTEX;
    //顶点之间的偏移量
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 每个顶点四个字节

    // 初始化EGL
    public EGLRenderer(Context context, int width, int height) {
        mContext = context;
        mWidth = width;
        mHeight = height;
        mEGLHelper = new EGLHelper();
        mEGLHelper.eglInit(width,height);
    }

    public void setBitmap(Bitmap bitmap){
        this.mBitmap=bitmap;
        initVertexData();
        initShader();
        mTextureId = createTexture(mBitmap);
    }

    public Bitmap getBitmap() {
        drawFrame();
        return convertToBitmap();
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


    private void initShader() {
        String vertexSource = ShaderUtil.loadFromAssetsFile("fbo_vertex.glsl" , mContext.getResources());
        String fragmentSource = ShaderUtil.loadFromAssetsFile("fbo_fragment.glsl" , mContext.getResources());
        mProgram = ShaderUtil.createProgram(vertexSource, fragmentSource);
        mAttrVertex = GLES20.glGetAttribLocation(mProgram, "aVertex");
        mAttrTexurePos = GLES20.glGetAttribLocation(mProgram, "aTexture");
        mUniTexureId = GLES20.glGetUniformLocation(mProgram, "uTextureId");
    }

    private int createTexture(Bitmap bmp){
        int[] texture=new int[1];
        if(bmp!=null && !bmp.isRecycled()){
            //生成纹理
            GLES20.glGenTextures(1,texture,0);
            //生成纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,texture[0]);
            //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_NEAREST);
            //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);
            //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE);
            //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE);
            //根据以上指定的参数，生成一个2D纹理
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
            return texture[0];
        }
        return 0;
    }

    private void drawFrame() {
        if (mBitmap!=null&&!mBitmap.isRecycled()) {

            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
            //指定使用某套shader程序
            GLES20.glUseProgram(mProgram);

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
            GLES20.glUniform1i(mUniTexureId, 0);

            //将顶点位置数据传送进渲染管线
            GLES20.glVertexAttribPointer(
                    mAttrVertex, // 顶点坐标引用
                    2, // 每个顶点有3个值x, y
                    GLES20.GL_FLOAT, // 顶点类型
                    false, // 是否需要归一化，不需要，因为我们已经是在-1 ~ 1的范围内了
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
    }

    private Bitmap convertToBitmap() {
        int[] iat = new int[mWidth * mHeight];
        IntBuffer ib = IntBuffer.allocate(mWidth * mHeight);
        // 读取GL的像素，然后转换成图片
        mEGLHelper.mGL.glReadPixels(0, 0, mWidth, mHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, ib);
        int[] ia = ib.array();

        // Convert upside down mirror-reversed image to right-side up normal
        // image.
        for (int i = 0; i < mHeight; i++) {
            System.arraycopy(ia, i * mWidth, iat, (mHeight - i - 1) * mWidth, mWidth);
        }
        Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(IntBuffer.wrap(iat));
        return bitmap;
    }



}
