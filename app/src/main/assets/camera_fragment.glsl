#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 vTexture;
uniform samplerExternalOES uTextureId;
uniform int uBeauty;

void modifyColor(vec4 color){
    color.r=max(min(color.r,1.0),0.0);
    color.g=max(min(color.g,1.0),0.0);
    color.b=max(min(color.b,1.0),0.0);
    color.a=max(min(color.a,1.0),0.0);
}

void main() {
    vec4 nColor = texture2D(uTextureId,vTexture);
    if (uBeauty == 1) {
        // 暖色调
        vec4 deltaColor=nColor+vec4(0.1,0.1,0.0,0.0);
        modifyColor(deltaColor);
        gl_FragColor=deltaColor;
    }
    else {
        gl_FragColor = nColor;
    }
}