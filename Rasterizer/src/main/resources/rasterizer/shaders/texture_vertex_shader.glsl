#version 130

uniform mat4 modelViewProjectionMatrix;
uniform mat3 textureMatrix;

in vec3 vertexPosition;
in vec2 vertexUVCoordinates;

//output variables are sent to the fragment shader and are automatically interpolated between vertices.
out vec2 fragmentUVCoordinates;

void main() {
    gl_Position = modelViewProjectionMatrix * vec4(vertexPosition, 1);

    fragmentUVCoordinates = (textureMatrix * vec3(vertexUVCoordinates, 1)).xy;
}
