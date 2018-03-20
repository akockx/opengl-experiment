#version 130

in vec3 vertex_position;
in vec3 vertex_color;

out vec3 color;

void main() {
    gl_Position = vec4(vertex_position, 1);

    //send vertex color to fragment shader.
    color = vertex_color;
}
