#version 130

in vec4 color;

out vec4 fragment_color;

void main() {
    //color from vertex shader is automatically interpolated between vertices.
    fragment_color = color;
}
