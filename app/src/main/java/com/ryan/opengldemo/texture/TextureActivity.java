package com.ryan.opengldemo.texture;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.ryan.opengldemo.R;

public class TextureActivity extends Activity {

    private GLSurfaceView mTextureSurfaceView;

    private TextureRenderer mTextureRenderer;

    private RadioGroup mRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texture);

        mTextureRenderer = new TextureRenderer(this);

        mTextureSurfaceView = findViewById(R.id.gl_texture);
        mTextureSurfaceView.setEGLContextClientVersion(2);
        mTextureSurfaceView.setRenderer(mTextureRenderer);
        mTextureSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        mRadioGroup = findViewById(R.id.rb_group);
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int id= group.getCheckedRadioButtonId();
                switch (id) {
                    case R.id.rb_gray:
                        mTextureRenderer.setDrawType(1);
                        break;
                    case R.id.rb_blur:
                        mTextureRenderer.setDrawType(2);
                        break;
                    case R.id.rb_magn:
                        mTextureRenderer.setDrawType(3);
                        break;
                    case R.id.rb_normal:
                    default:
                        mTextureRenderer.setDrawType(0);
                        break;
                }
                mTextureSurfaceView.requestRender();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTextureSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mTextureSurfaceView.onPause();
    }

}
