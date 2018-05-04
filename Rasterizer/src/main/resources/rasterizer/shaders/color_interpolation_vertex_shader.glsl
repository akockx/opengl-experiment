#version 130

uniform mat4 modelViewProjectionMatrix;

in vec4 vertexPosition;
in vec4 vertexColor;

//output variables are sent to the fragment shader and are automatically interpolated between vertices.
out vec4 interpolatedColor;

void main() {
    gl_Position = modelViewProjectionMatrix * vertexPosition;

    interpolatedColor = vertexColor;
}
