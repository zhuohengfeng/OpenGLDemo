package com.ryan.opengldemo.utils;

import android.content.res.Resources;
import android.opengl.GLES20;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;


/** 加载顶点Shader与片元Shader的工具类 */
public class ShaderUtil {

    /**
     * 加载制定shader的方法： 生成一个shader-->加载脚本soruce-->编译脚本-->返回shader(int)
     * @param shaderType shader的类型  GLES20.GL_VERTEX_SHADER(顶点)   GLES20.GL_FRAGMENT_SHADER(片元)
     * @param source shader的脚本字符串
     * @return
     */
    public static int loadShader(int shaderType, String source) {
        //创建一个新shader
        int shader = GLES20.glCreateShader(shaderType);
        //若创建成功则加载shader
        if (0 != shader) {
            // 加载shader的源代码
            GLES20.glShaderSource(shader, source);
            // 编译shader
            GLES20.glCompileShader(shader);
            // 存放编译成功shader数量的数组
            int[] compiled = new int[1];
            //获取Shader的编译情况
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                //若编译失败则显示错误日志并删除此shader
                Logger.e("Could not compile shader " + shaderType + ":");
                Logger.e(GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    /**
     * 创建shader程序的方法  加载shader并获取到shader(int) --> 创建程序，返回progrm ID --> 向程序中加入(attach)顶点着色器与片元着色器
     *                      --> 链接程序（将两个着色器链接为一个整体的着色器程序） --> 返回程序(int)
     * @param vertexSource
     * @param fragmentSource
     * @return
     */
    public static int createProgram(String vertexSource, String fragmentSource) {
        // 加载顶点着色器
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (0 == vertexShader) {
            Logger.e("load vertex shader error!!! ");
            return 0;
        }

        // 加载片元着色器
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (0 == fragmentShader) {
            Logger.e("load fragment shader error!!! ");
            return 0;
        }

        // 创建程序
        int program = GLES20.glCreateProgram();
        // 若程序创建成功则向程序中加入顶点着色器与片元着色器
        if (0 != program) {
            // 向程序中加入顶点着色器
            GLES20.glAttachShader(program, vertexShader);
            checkGLError("glAttachShader");

            // 向程序中加入片元着色器
            GLES20.glAttachShader(program, fragmentShader);
            checkGLError("glAttachShader");

            // 链接程序, 将两个着色器链接为一个整体的着色器程序
            GLES20.glLinkProgram(program);
            //存放链接成功program数量的数组
            int[] linkStatus = new int[1];
            //获取program的链接情况
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            //若链接失败则报错并删除程序
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Logger.e("Could not link program: ");
                Logger.e(GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }

        return program;
    }

    /**
     * 检查每一步操作是否有错误的方法
     * @param op
     */
    public static void checkGLError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR)
        {
            Logger.e("ES20_ERROR", op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }

    /**
     * 从sh脚本中加载shader内容的方法
     * @param fname
     * @param r
     * @return
     */
    public static String loadFromAssetsFile(String fname, Resources r) {
        String result = null;
        try {
            InputStream in = r.getAssets().open(fname);
            int ch = 0;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ((ch = in.read()) != -1) {
                baos.write(ch);
            }
            byte[] buff = baos.toByteArray();
            baos.close();
            in.close();
            result = new String(buff, "UTF-8");
            result = result.replaceAll("\\r\\n", "\n");
         }
        catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
