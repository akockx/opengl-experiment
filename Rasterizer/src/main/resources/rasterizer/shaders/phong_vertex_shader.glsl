#version 130

uniform mat4 modelViewProjectionMatrix;
uniform mat4 modelViewMatrix;

in vec3 vertexPosition;//in model space.
in vec3 vertexNormal;//in model space.
in vec3 vertexColor;

//output variables are sent to the fragment shader and are automatically interpolated between vertices.
out vec3 fragmentPosition;//in camera space.
out vec3 fragmentNormalVector;//in camera space.
out vec3 fragmentDiffuseColor;//diffuse reflection coefficient per color component (r, g, b).

/**
 * Implements Phong shading, see https://en.wikipedia.org/wiki/Phong_shading
 */
void main() {
    gl_Position = modelViewProjectionMatrix * vec4(vertexPosition, 1);

    vec4 vertexPositionInCameraSpace = modelViewMatrix * vec4(vertexPosition, 1);
    vec4 vertexNormalInCameraSpace = modelViewMatrix * vec4(vertexNormal, 0);
    fragmentPosition = vertexPositionInCameraSpace.xyz;
    fragmentNormalVector = vertexNormalInCameraSpace.xyz;
    fragmentDiffuseColor = vertexColor;
}
