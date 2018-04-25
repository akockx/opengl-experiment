#version 130

in vec4 vertex_position;
in vec4 vertex_color;

out vec4 color;

void main() {
    gl_Position = vertex_position;

    //color will be sent to the fragment shader.
    color = vertex_color;
}
