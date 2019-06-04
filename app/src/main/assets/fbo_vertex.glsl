attribute vec4 aVertex;
attribute vec2 aTexture;
varying vec2 vTexture;
void main() {
    vTexture = aTexture; // 传递纹理坐标
    gl_Position = aVertex;
}