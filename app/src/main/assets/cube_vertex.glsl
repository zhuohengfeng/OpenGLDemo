uniform mat4 uMVPMatrix; //总变换矩阵
attribute vec4 aVertex;
attribute vec4 aColor;
varying vec4 vColor;
void main() {
    vColor = aColor;
    gl_Position = uMVPMatrix * aVertex;
}