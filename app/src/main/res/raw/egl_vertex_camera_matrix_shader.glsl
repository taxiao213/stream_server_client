attribute vec4 av_Position;
attribute vec2 af_Position;
varying vec2 v_texPosition;
uniform mat4 u_Matrix;
void main() {
    v_texPosition = af_Position;
    gl_Position = vec4(av_Position.x,av_Position.y,av_Position.z,1.0f) * u_Matrix;
//    gl_Position = av_Position * u_Matrix;
}
