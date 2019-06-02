attribute vec4 aVertex;
attribute vec2 aTexture;
varying vec2 vTexture;
void main() {
    vTexture = aTexture;
    gl_Position = aVertex;
}