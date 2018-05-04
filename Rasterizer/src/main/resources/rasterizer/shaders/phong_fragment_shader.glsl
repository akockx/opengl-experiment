#version 130

uniform vec3 lightPosition;//in camera space.
uniform vec3 lightIntensity;//light source intensity per color component (r, g, b).
uniform vec3 ambientLightIntensity;//ambient lighting intensity per color component (r, g, b).
uniform float specularReflectionCoefficient;
uniform float shininess;

//input variables are automatically interpolated between vertices.
in vec3 fragmentPosition;//in camera space.
in vec3 fragmentNormalVector;//in camera space.
in vec3 fragmentDiffuseColor;//used as reflection coefficient per color component (r, g, b) for ambient and diffuse reflection.

//the color that is used to draw this fragment on the screen.
out vec4 fragmentColor;

/**
 * Implements the Phong reflection model, see https://en.wikipedia.org/wiki/Phong_reflection_model
 */
void main() {
    //calculate directions as seen from fragmentPosition.
    vec3 lightDirection = lightPosition - fragmentPosition;
    float distanceFromFragmentToLight = length(lightDirection);
    lightDirection = normalize(lightDirection);
    vec3 viewDirection = normalize(-fragmentPosition);
    vec3 surfaceNormal = normalize(fragmentNormalVector);
    vec3 reflectionDirection = reflect(-lightDirection, surfaceNormal);

    //incident intensity.
    vec3 incidentIntensity = lightIntensity/pow(distanceFromFragmentToLight, 2);

    //ambient light.
    vec3 ambientIntensity = fragmentDiffuseColor * ambientLightIntensity;

    //diffuse reflection.
    float lambertian = max(dot(surfaceNormal, lightDirection), 0);
    vec3 diffuseIntensity = fragmentDiffuseColor * lambertian * incidentIntensity;

    //specular reflection.
    float specularAngle = max(dot(viewDirection, reflectionDirection), 0);
    vec3 specularIntensity = specularReflectionCoefficient * pow(specularAngle, shininess) * incidentIntensity;

    //outgoing intensity.
    vec3 fragmentIntensity = ambientIntensity + diffuseIntensity + specularIntensity;
    fragmentColor = vec4(clamp(fragmentIntensity, 0, 1), 1);
}
