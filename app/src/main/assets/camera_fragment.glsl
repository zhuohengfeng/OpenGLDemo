#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 vTexture;
uniform samplerExternalOES uTextureId;
uniform int uBeauty;
void main() {
    if (uBeauty == 1) {

    }
    else {
        gl_FragColor = texture2D(uTextureId, vTexture);
    }
}