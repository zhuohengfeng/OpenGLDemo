attribute vec4 aVertex;
attribute vec4 aTexture;
varying vec2 vTexture;
uniform mat4 uTextureMatrix;
void main() {
    vTexture = (uTextureMatrix * aTexture).xy;
    gl_Position = aVertex;
}