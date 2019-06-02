precision mediump float;
varying vec2 vTexture;
uniform sampler2D uTextureId;
void main() {
    gl_FragColor = texture2D(uTextureId, vTexture);
}
