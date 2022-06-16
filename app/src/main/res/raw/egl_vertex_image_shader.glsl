uniform mat4 u_Matrix;
attribute vec4 av_Position;
attribute vec2 af_Position;
varying vec2 v_texPosition;
void main() {
    v_texPosition = af_Position;
    gl_Position = u_Matrix * av_Position;
}
