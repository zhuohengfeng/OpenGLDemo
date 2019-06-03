attribute vec4 aVertex;
attribute vec2 aTexture;
varying vec2 vTexture;
varying vec4 vPosition;
void main() {
    vTexture = aTexture; // 传递纹理坐标
    vPosition = aVertex; // 传递当前的顶点
    gl_Position = aVertex;
}