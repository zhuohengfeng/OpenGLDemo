package com.ryan.opengldemo.utils;

import android.opengl.Matrix;

import java.util.Stack;

public class MatrixState {

    private static float[] mProjMatrix = new float[16]; // 投影矩阵，从orthoM源码中可以看到这个矩阵要设置16
    private static float[] mVMatrix = new float[16]; // 变化矩阵
    private static float[] mMVPMatrix; // 总的变化矩阵

    private static float[] currMatrix;//当前变换矩阵

    public static Stack<float[]> mStack=new Stack<float[]>();//保护变换矩阵的栈

    public static void setInitStack()//获取不变换初始矩阵
    {
        currMatrix=new float[16];
        Matrix.setRotateM(currMatrix, 0, 0, 1, 0, 0);
    }

    public static void pushMatrix()//保护变换矩阵
    {
        mStack.push(currMatrix.clone());
    }

    public static void popMatrix()//恢复变换矩阵
    {
        currMatrix=mStack.pop();
    }

    public static void translate(float x,float y,float z)//设置沿xyz轴移动
    {
        Matrix.translateM(currMatrix, 0, x, y, z);
    }

    public static void rotate(float angle,float x,float y,float z)//设置绕xyz轴移动
    {
        Matrix.rotateM(currMatrix,0,angle,x,y,z);
    }



    // 设置摄像机
    public static void setCamera(float cx, float cy, float cz, // 摄像机位置XYZ坐标
                                 float tx, float ty, float tz, // 观测目标点XYZ坐标
                                 float upx, float upy, float upz) { // up向量在XYZ上的分量
        Matrix.setLookAtM(mVMatrix, 0, cx, cy, cz, tx, ty, tz, upx, upy, upz);
    }

    // 设置正交投影矩阵
    public static void setProjectOrtho(float left, float right, float bottom, float top, float near, float far) {
        Matrix.orthoM(mProjMatrix, 0, left, right, bottom, top, near, far);
    }

    // 设置透视投影矩阵
    public static void setProjectFrustum(float left, float right, float bottom, float top, float near, float far) {
        Matrix.frustumM(mProjMatrix, 0, left, right, bottom, top, near, far);
    }

    // 生成总的变化矩阵
    public static float[] getFinalMatrix(float[] spec) {
        // 创建用于存放最终变化矩阵的数组
        mMVPMatrix = new float[16];
        // 将摄像机矩阵乘以变化矩阵, spec是变化矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, spec, 0);
        // 将投影矩阵乘以上一步的结果矩阵得到最终变化矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0);
        return mMVPMatrix;
    }

    //获取具体物体的总变换矩阵
    public static float[] getFinalMatrix() {
        float[] mMVPMatrix=new float[16];
        Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, currMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0);
        return mMVPMatrix;
    }

    //获取具体物体的变换矩阵
    public static float[] getMMatrix()
    {
        return currMatrix;
    }

}
