#version 130

//input variables are automatically interpolated between vertices.
in vec2 fragmentUVCoordinates;

//the color that is used to draw this fragment on the screen.
out vec4 fragmentColor;

/**
 * The fragment is colored depending on whether it is contained in the Mandelbrot set.
 * This way the Mandelbrot is procedurally generated (not using textures).
 */
void main() {
    vec2 c = fragmentUVCoordinates;

    vec2 z = vec2(0, 0);
    bool escaped = false;
    int iteration = 0;
    while (!escaped && iteration < 100) {
        float real = z[0];
        float imaginary = z[1];
        z = vec2(real*real - imaginary*imaginary, 2*real*imaginary) + c;
        if (length(z) > 2) {
            escaped = true;
        }

        iteration++;
    }

    if (escaped) {
        //use 15 shades of grey.
        float f = (iteration%15 + 1)/15.0;
        fragmentColor = vec4(f, f, f, 1);
    } else {
        fragmentColor = vec4(0, 0, 0, 1);//black.
    }
}
