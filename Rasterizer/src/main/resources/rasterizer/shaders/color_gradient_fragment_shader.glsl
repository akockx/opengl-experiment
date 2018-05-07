#version 130

const int maxColorCount = 10;//a maximum of 10 colors can be used.

uniform vec3[maxColorCount] gradientColors;
uniform float[maxColorCount] locations;//u coordinates corresponding to gradientColors.
uniform int colorCount;//actual color count.

//input variables are automatically interpolated between vertices.
in vec2 fragmentUVCoordinates;

//the color that is used to draw this fragment on the screen.
out vec4 fragmentColor;

/**
 * If x is equal to a location, then the color for that location is used.
 * If x is between two locations, then the color is linearly interpolated between the colors for the two locations.
 * If x is smaller than the first location, then the first color is used.
 * If x is greater than the last location, then the last color is used.
 */
vec3 getGradientColor(in float x, in vec3[maxColorCount] colors, in float[maxColorCount] locations, in int colorCount) {
    if (x <= locations[0]) return colors[0];
    if (x >= locations[colorCount - 1]) return colors[colorCount - 1];

    int index1 = -1;
    int index2 = -1;
    //Note: this could be improved by using a binary search.
    for (int index = 1; index < colorCount; index++) {
        if (x <= locations[index]) {
            index1 = index - 1;
            index2 = index;
            break;
        }
    }

    float distance = x - locations[index1];
    float totalDistance = locations[index2] - locations[index1];
    return mix(colors[index1], colors[index2], distance/totalDistance);
}

/**
 * The u coordinate of this fragment is checked against the given locations
 * and gradientColors to get the color for this fragment.
 * The v coordinate is ignored.
 */
void main() {
    fragmentColor = vec4(getGradientColor(fragmentUVCoordinates.x, gradientColors, locations, colorCount), 1);
}
