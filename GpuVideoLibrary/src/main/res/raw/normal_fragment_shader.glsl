#extension GL_OES_EGL_image_external : require
varying highp vec2 textureCoordinate;
uniform samplerExternalOES inputTexture;
void main(){
     gl_FragColor = texture2D(inputTexture, textureCoordinate);
}