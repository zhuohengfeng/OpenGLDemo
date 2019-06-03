#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 vTexture;
uniform samplerExternalOES uTextureId;
void main() {
    gl_FragColor = texture2D(uTextureId, vTexture);
}