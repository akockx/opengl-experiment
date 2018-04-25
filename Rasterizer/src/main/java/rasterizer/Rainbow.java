/*
 * Copyright (c) 2018 A.C. Kockx, All Rights Reserved.
 */
package rasterizer;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;

import static java.lang.Math.*;

/**
 * Uses OpenGL 3 to draw a rainbow that follows a curved path through space.
 *
 * @author A.C. Kockx
 */
public final class Rainbow {
    //r, g, b, a values.
    private static final float RAINBOW_COLORS[][] = new float[][]{{1,    0, 0, 1},
                                                                  {1, 0.5f, 0, 1},
                                                                  {1,    1, 0, 1},
                                                                  {0,    1, 0, 1},
                                                                  {0,    1, 1, 1},
                                                                  {0,    0, 1, 1},
                                                                  {0.5f, 0, 1, 1}};

    private final String vertexShaderSource;
    private final String fragmentShaderSource;

    public static void main(String[] args) throws Exception {
        new Rainbow();
    }

    private Rainbow() throws Exception {
        //load shader source.
        ResourceLoader loader = new ResourceLoader("/rasterizer/shaders/");
        vertexShaderSource = Utils.read(loader.loadResource("vertex_shader.glsl"));
        fragmentShaderSource = Utils.read(loader.loadResource("fragment_shader.glsl"));

        //create OpenGL canvas.
        GLCanvas canvas = OpenGLUtils.createGLCanvas(800, 600);
        canvas.addGLEventListener(glEventListener);

        //init GUI on event-dispatching thread.
        javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                Utils.createAndShowFrame(canvas, Rainbow.class.getSimpleName(), false);
            }
        });
    }

    private final GLEventListener glEventListener = new GLEventListener() {
        private int shaderProgramId = -1;
        private int[] triangleStripIds = null;
        private int[] triangleStripVertexCounts = null;

        @Override
        public void init(GLAutoDrawable drawable) {
            GL3 gl = drawable.getGL().getGL3();
            gl.glClearColor(0, 0, 0, 1);

            //create shaders.
            int positionAttributeIndex = 0;
            int colorAttributeIndex = 1;
            shaderProgramId = OpenGLUtils.createShaderProgram(gl, vertexShaderSource, fragmentShaderSource, positionAttributeIndex, colorAttributeIndex);

            //create rainbow geometry.
            float[][][] allVertices = createRainbowGeometry();
            int triangleStripCount = allVertices.length - 1;
            int stepCount = allVertices[0].length;
            int dimensionCount = allVertices[0][0].length;
            triangleStripIds = new int[triangleStripCount];
            triangleStripVertexCounts = new int[triangleStripCount];
            for (int triangleStripIndex = 0; triangleStripIndex < triangleStripCount; triangleStripIndex++) {
                float[] coordinates = new float[2*stepCount*dimensionCount];
                float[] colors = new float[2*stepCount*dimensionCount];
                int index = 0;
                for (int stepIndex = 0; stepIndex < stepCount; stepIndex++) {
                    for (int dimension = 0; dimension < dimensionCount; dimension++) {
                        coordinates[index] = allVertices[triangleStripIndex + 1][stepIndex][dimension];
                        colors[index] = RAINBOW_COLORS[triangleStripIndex + 1][dimension];
                        index++;
                    }
                    for (int dimension = 0; dimension < dimensionCount; dimension++) {
                        coordinates[index] = allVertices[triangleStripIndex][stepIndex][dimension];
                        colors[index] = RAINBOW_COLORS[triangleStripIndex][dimension];
                        index++;
                    }
                }
                triangleStripIds[triangleStripIndex] = OpenGLUtils.createVertexArray(gl, coordinates, colors, positionAttributeIndex, colorAttributeIndex);
                triangleStripVertexCounts[triangleStripIndex] = index/dimensionCount;
            }

            int error = gl.glGetError();
            if (error != 0) System.err.println("Error during initialization: " + error);
        }

        @Override
        public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        }

        @Override
        public void display(GLAutoDrawable drawable) {
            GL3 gl = drawable.getGL().getGL3();
            gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);

            //draw rainbow.
            gl.glUseProgram(shaderProgramId);
            //draw triangle strips.
            for (int n = 0; n < triangleStripIds.length; n++) {
                gl.glBindVertexArray(triangleStripIds[n]);
                gl.glDrawArrays(GL3.GL_TRIANGLE_STRIP, 0, triangleStripVertexCounts[n]);
            }

            int error = gl.glGetError();
            if (error != 0) System.err.println("Error during rendering: " + error);
        }

        @Override
        public void dispose(GLAutoDrawable drawable) {
        }
    };

    private static float[][][] createRainbowGeometry() {
        int colorCount = RAINBOW_COLORS.length;
        int stepCount = 100;
        int dimensionCount = 4;
        float[][][] allVertices = new float[colorCount][stepCount][dimensionCount];

        //rainbow follows a curved path through 3D space.
        //traverse rainbow path in steps.
        for (int stepIndex = 0; stepIndex < stepCount; stepIndex++) {
            float t = stepIndex/(stepCount - 1f);

            //parameter t ranges from 0 to 1 (both inclusive) along the rainbow path.
            float xStep = -1 + 2*t;
            float yStep = (float) sin(1.3*2*PI*t)/8;
            float zStep = 0;

            //traverse colors perpendicular to rainbow path at current step.
            for (int colorIndex = 0; colorIndex < colorCount; colorIndex++) {
                float u = colorIndex/(colorCount - 1f);

                //parameter u ranges from 0 to 1 (both inclusive) along the rainbow width.
                float xColor = xStep;
                float yColor = yStep - 0.5f + u;
                float zColor = zStep;

                //add vertex for current step and color.
                allVertices[colorIndex][stepIndex] = new float[]{xColor, yColor, zColor, 1};
            }
        }

        return allVertices;
    }
}
