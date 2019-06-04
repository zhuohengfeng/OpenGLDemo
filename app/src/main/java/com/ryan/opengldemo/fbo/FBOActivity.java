package com.ryan.opengldemo.fbo;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.ryan.opengldemo.R;
import com.ryan.opengldemo.utils.Logger;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class FBOActivity extends Activity implements FBORenderer.Callback {

    private GLSurfaceView mFBOSurfaceView;

    private FBORenderer mFBORenderer;

    private ImageView mImage;
    private int mBmpWidth,mBmpHeight;
    private String mImgPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fbo);

        mFBORenderer = new FBORenderer(this);
        mFBORenderer.setCallback(this);

        mFBOSurfaceView = findViewById(R.id.gl_fbo);
        mFBOSurfaceView.setEGLContextClientVersion(2);
        mFBOSurfaceView.setRenderer(mFBORenderer);
        mFBOSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        mImage= findViewById(R.id.mImage);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFBOSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFBOSurfaceView.onPause();
    }

    public void onClick(View view){
        //调用相册
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    // 选择一张图片并渲染贴图
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(selectedImage, filePathColumns, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePathColumns[0]);
            mImgPath = c.getString(columnIndex);
            Logger.d("FBO get img->"+mImgPath);
            // 解码图片，并进行渲染
            Bitmap bmp= BitmapFactory.decodeFile(mImgPath);
            mBmpWidth=bmp.getWidth();
            mBmpHeight=bmp.getHeight();
            mFBORenderer.setBitmap(bmp);
            mFBOSurfaceView.requestRender();
            c.close();
        }
    }

    @Override
    public void onCall(final ByteBuffer data) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Logger.d("FBO callback success");
                Bitmap bitmap = Bitmap.createBitmap(mBmpWidth,mBmpHeight, Bitmap.Config.ARGB_8888);
                bitmap.copyPixelsFromBuffer(data);
                saveBitmap(bitmap);
                data.clear();
            }
        }).start();
    }

    //图片保存
    public void saveBitmap(final Bitmap b){
        String path = mImgPath.substring(0,mImgPath.lastIndexOf("/")+1);
        File folder=new File(path);
        if(!folder.exists()&&!folder.mkdirs()){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(FBOActivity.this, "无法保存照片", Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }
        long dataTake = System.currentTimeMillis();
        final String jpegName=path+ dataTake +".jpg";
        try {
            FileOutputStream fout = new FileOutputStream(jpegName);
            BufferedOutputStream bos = new BufferedOutputStream(fout);
            b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(FBOActivity.this, "保存成功->"+jpegName, Toast.LENGTH_SHORT).show();
                Logger.d("保存处理后的图片到 jpegName="+jpegName);
                mImage.setImageBitmap(b);
            }
        });
    }

}
