precision mediump float;
varying vec2 vTexture;
varying vec4 vPosition;
uniform sampler2D uTextureId;
uniform int uDrawType;

void main() {
    vec4 nColor = texture2D(uTextureId,vTexture);
    if (uDrawType == 1) { // 黑白
        float c = nColor.r*0.299 + nColor.g*0.587 + nColor.b*0.114;
        gl_FragColor = vec4(c, c, c, nColor.a);
    }
    else if (uDrawType == 2)  { // 模糊
        nColor+=texture2D(uTextureId, vec2(vTexture.x-0.006, vTexture.y-0.006));
        nColor+=texture2D(uTextureId, vec2(vTexture.x-0.006, vTexture.y+0.006));
        nColor+=texture2D(uTextureId, vec2(vTexture.x+0.006, vTexture.y-0.006));
        nColor+=texture2D(uTextureId, vec2(vTexture.x+0.006, vTexture.y+0.006));
        nColor+=texture2D(uTextureId, vec2(vTexture.x-0.004, vTexture.y-0.004));
        nColor+=texture2D(uTextureId, vec2(vTexture.x-0.004, vTexture.y+0.004));
        nColor+=texture2D(uTextureId, vec2(vTexture.x+0.004, vTexture.y-0.004));
        nColor+=texture2D(uTextureId, vec2(vTexture.x+0.004, vTexture.y+0.004));
        nColor+=texture2D(uTextureId, vec2(vTexture.x-0.002, vTexture.y-0.002));
        nColor+=texture2D(uTextureId, vec2(vTexture.x-0.002, vTexture.y+0.002));
        nColor+=texture2D(uTextureId, vec2(vTexture.x+0.002, vTexture.y-0.002));
        nColor+=texture2D(uTextureId, vec2(vTexture.x+0.002, vTexture.y+0.002));
        nColor/=13.0;
        gl_FragColor=nColor;
    }
    else if (uDrawType == 3)  { // 放大
        float dis = distance(vec2(vPosition.x,vPosition.y), vec2(0.0, 0.0));
        if(dis<0.4){
            nColor=texture2D(uTextureId, vec2(vTexture.x/2.0+0.25, vTexture.y/2.0+0.25));
        }
        gl_FragColor=nColor;
    }
    else {
        gl_FragColor = nColor;
    }
}
