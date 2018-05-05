#version 130

const int maxIterationCount = 100;

//input variables are automatically interpolated between vertices.
in vec2 fragmentUVCoordinates;

//the color that is used to draw this fragment on the screen.
out vec4 fragmentColor;

/**
 * The fragment is colored depending on whether it is in the Mandelbrot set.
 * This way the Mandelbrot is procedurally generated (not using textures).
 */
void main() {
    vec2 c = fragmentUVCoordinates;

    vec2 z = vec2(0, 0);
    bool diverged = false;
    int iteration = 0;
    while (!diverged && iteration < maxIterationCount) {
        //z_new = z_old^2 + c
        z = vec2(z.x*z.x - z.y*z.y, 2*z.x*z.y) + c;

        if (z.x*z.x + z.y*z.y > 2*2) {//if abs(z) > 2
            diverged = true;
        }

        iteration++;
    }

    if (!diverged) {//if in Mandelbrot set.
        fragmentColor = vec4(0, 0, 0, 1);//black.
    } else {//outside coloring.
        //use 15 shades of grey.
        float f = (iteration%15 + 1)/15.0;
        fragmentColor = vec4(f, f, f, 1);
    }
}
