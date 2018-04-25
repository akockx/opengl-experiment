/*
 * Copyright (c) 2018 A.C. Kockx, All Rights Reserved.
 */
package rasterizer.experiments;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.math.Matrix4;
import rasterizer.util.MatrixUtils;
import rasterizer.util.OpenGLUtils;
import rasterizer.util.ResourceLoader;
import rasterizer.util.Utils;

/**
 * Uses OpenGL 3 to draw a multi-colored triangle on the screen.
 *
 * @author A.C. Kockx
 */
public final class MultiColoredTriangle {
    //x, y, z, w model coordinates.
    private static final float[] coordinates = new float[]{-1, -1, 0, 1,
                                                            1, -1, 0, 1,
                                                            0,  1, 0, 1
    };
    //r, g, b, a values.
    private static final float colors[] = new float[]{0, 0, 1, 1,
                                                      0, 1, 0, 1,
                                                      1, 0, 0, 1
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
        GLCanvas canvas = OpenGLUtils.createGLCanvas(800, 600);
        canvas.addGLEventListener(glEventListener);

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
        private int mvpMatrixUniformIndex = -1;
        private int vertexArrayObjectId = -1;

        private Matrix4 modelMatrix = null;
        private Matrix4 viewMatrix = null;
        private Matrix4 projectionMatrix = null;

        @Override
        public void init(GLAutoDrawable drawable) {
            GL3 gl = drawable.getGL().getGL3();
            gl.glClearColor(0, 0, 0, 1);

            //create shaders.
            int positionAttributeIndex = 0;
            int colorAttributeIndex = 1;
            shaderProgramId = OpenGLUtils.createShaderProgram(gl, vertexShaderSource, fragmentShaderSource, positionAttributeIndex, colorAttributeIndex);
            mvpMatrixUniformIndex = gl.glGetUniformLocation(shaderProgramId, OpenGLUtils.MVP_MATRIX);

            //create geometry.
            vertexArrayObjectId = OpenGLUtils.createVertexArray(gl, coordinates, colors, positionAttributeIndex, colorAttributeIndex);
            modelMatrix = MatrixUtils.createModelMatrix(0, 0, 0, 0, 0, 0, 1, 1, 1);

            //create camera.
            viewMatrix = MatrixUtils.createViewMatrix(0, 0, 4, 0, 0, 0);

            int error = gl.glGetError();
            if (error != 0) System.err.println("Error during initialization: " + error);
        }

        @Override
        public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
            //calculate aspect ratio.
            if (width <= 0) width = 1;//to avoid divide by zero.
            if (height <= 0) height = 1;//to avoid divide by zero.
            float aspectRatio = width/((float) height);

            //(re)initialize projection matrix.
            projectionMatrix = MatrixUtils.createPerspectiveProjectionMatrix(45, aspectRatio, 2, 1000);
        }

        @Override
        public void display(GLAutoDrawable drawable) {
            GL3 gl = drawable.getGL().getGL3();
            gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);

            //draw triangle.
            gl.glUseProgram(shaderProgramId);
            Matrix4 mvpMatrix = MatrixUtils.multiply(projectionMatrix, MatrixUtils.multiply(viewMatrix, modelMatrix));
            gl.glUniformMatrix4fv(mvpMatrixUniformIndex, 1, false, mvpMatrix.getMatrix(), 0);
            gl.glBindVertexArray(vertexArrayObjectId);
            gl.glDrawArrays(GL3.GL_TRIANGLES, 0, 3);

            int error = gl.glGetError();
            if (error != 0) System.err.println("Error during rendering: " + error);
        }

        @Override
        public void dispose(GLAutoDrawable drawable) {
        }
    };
}
