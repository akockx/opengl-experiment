/*
 * Copyright (c) 2018 A.C. Kockx, All Rights Reserved.
 */
package rasterizer;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;

/**
 * Uses OpenGL 3 to draw a multi-colored triangle.
 *
 * @author A.C. Kockx
 */
public final class MultiColoredTriangle {
    //x, y, z.
    private static final float[] coordinates = new float[]{-0.8f, -0.8f, 0,
                                                            0.8f, -0.8f, 0,
                                                               0,  0.8f, 0
    };
    //r, g, b.
    private static final float colors[] = new float[]{0, 0, 1,
                                                      0, 1, 0,
                                                      1, 0, 0
    };

    private final String vertexShaderSource;
    private final String fragmentShaderSource;

    public static void main(String[] args) throws Exception {
        new MultiColoredTriangle();
    }

    private MultiColoredTriangle() throws Exception {
        //load shader source.
        ResourceLoader loader = new ResourceLoader("/rasterizer/shaders/");
        vertexShaderSource = Utils.read(loader.loadResource("vertex_shader.glsl"));
        fragmentShaderSource = Utils.read(loader.loadResource("fragment_shader.glsl"));

        //create OpenGL canvas.
        GLProfile profile = GLProfile.get(GLProfile.GL3);
        GLCapabilities capabilities = new GLCapabilities(profile);
        GLCanvas canvas = new GLCanvas(capabilities);
        canvas.addGLEventListener(glEventListener);
        canvas.setSize(800, 600);

        //init GUI on event-dispatching thread.
        javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                Utils.createAndShowFrame(canvas, MultiColoredTriangle.class.getSimpleName(), false);
            }
        });
    }

    private final GLEventListener glEventListener = new GLEventListener() {
        private int shaderProgramId = -1;
        private int vertexArrayObjectId = -1;

        @Override
        public void init(GLAutoDrawable drawable) {
            GL3 gl = drawable.getGL().getGL3();
            gl.glClearColor(0, 0, 0, 1);

            //create shaders.
            int positionAttributeIndex = 0;
            int colorAttributeIndex = 1;
            shaderProgramId = OpenGLUtils.createShaderProgram(gl, vertexShaderSource, fragmentShaderSource, positionAttributeIndex, colorAttributeIndex);

            //store vertex data in graphics card memory.
            vertexArrayObjectId = OpenGLUtils.createVertexArray(gl, coordinates, colors, positionAttributeIndex, colorAttributeIndex);

            int error = gl.glGetError();
            if (error != 0) System.err.println("Error during initialization: " + error);
        }

        @Override
        public void display(GLAutoDrawable drawable) {
            GL3 gl = drawable.getGL().getGL3();
            gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);

            //draw scene.
            gl.glUseProgram(shaderProgramId);
            gl.glBindVertexArray(vertexArrayObjectId);
            gl.glDrawArrays(GL3.GL_TRIANGLES, 0, 3);

            int error = gl.glGetError();
            if (error != 0) System.err.println("Error during rendering: " + error);
        }

        @Override
        public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        }

        @Override
        public void dispose(GLAutoDrawable drawable) {
        }
    };
}
