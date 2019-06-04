precision mediump float;
varying vec2 vTexture;
varying vec4 vPosition;
uniform sampler2D uTextureId;

void main() {
    vec4 nColor = texture2D(uTextureId,vTexture);
    float c = nColor.r*0.299 + nColor.g*0.587 + nColor.b*0.114;
    gl_FragColor = vec4(c, c, c, nColor.a);
}
