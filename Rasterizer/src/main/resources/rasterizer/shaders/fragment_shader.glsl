#version 130

in vec3 color;

out vec4 outputColor;

void main() {
    //color from vertex shader is automatically interpolated between vertices.
    outputColor = vec4(color, 1);
}
