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
 * Uses OpenGL 3 to draw a multi-colored cube on the screen.
 *
 * @author A.C. Kockx
 */
public final class MultiColoredCube {
    //x, y, z, w model coordinates.
    private static final float[] coordinates = new float[]{ 0.5f, -0.5f, -0.5f, 1,//front face.
                                                           -0.5f, -0.5f, -0.5f, 1,
                                                           -0.5f,  0.5f, -0.5f, 1,
                                                            0.5f, -0.5f, -0.5f, 1,
                                                           -0.5f,  0.5f, -0.5f, 1,
                                                            0.5f,  0.5f, -0.5f, 1,
                                                           -0.5f, -0.5f,  0.5f, 1,//back face.
                                                            0.5f, -0.5f,  0.5f, 1,
                                                            0.5f,  0.5f,  0.5f, 1,
                                                           -0.5f, -0.5f,  0.5f, 1,
                                                            0.5f,  0.5f,  0.5f, 1,
                                                           -0.5f,  0.5f,  0.5f, 1,
                                                            0.5f, -0.5f,  0.5f, 1,//right face.
                                                            0.5f, -0.5f, -0.5f, 1,
                                                            0.5f,  0.5f, -0.5f, 1,
                                                            0.5f, -0.5f,  0.5f, 1,
                                                            0.5f,  0.5f, -0.5f, 1,
                                                            0.5f,  0.5f,  0.5f, 1,
                                                           -0.5f, -0.5f, -0.5f, 1,//left face.
                                                           -0.5f, -0.5f,  0.5f, 1,
                                                           -0.5f,  0.5f,  0.5f, 1,
                                                           -0.5f, -0.5f, -0.5f, 1,
                                                           -0.5f,  0.5f,  0.5f, 1,
                                                           -0.5f,  0.5f, -0.5f, 1,
                                                           -0.5f,  0.5f,  0.5f, 1,//top face.
                                                            0.5f,  0.5f,  0.5f, 1,
                                                            0.5f,  0.5f, -0.5f, 1,
                                                           -0.5f,  0.5f,  0.5f, 1,
                                                            0.5f,  0.5f, -0.5f, 1,
                                                           -0.5f,  0.5f, -0.5f, 1,
                                                           -0.5f, -0.5f, -0.5f, 1,//bottom face.
                                                            0.5f, -0.5f, -0.5f, 1,
                                                            0.5f, -0.5f,  0.5f, 1,
                                                           -0.5f, -0.5f, -0.5f, 1,
                                                            0.5f, -0.5f,  0.5f, 1,
                                                           -0.5f, -0.5f,  0.5f, 1
    };
    //r, g, b, a values.
    private static final float colors[] = new float[]{0, 0, 1, 1,//front face.
                                                      0, 0, 1, 1,
                                                      0, 0, 1, 1,
                                                      0, 0, 1, 1,
                                                      0, 0, 1, 1,
                                                      0, 0, 1, 1,
                                                      1, 1, 0, 1,//back face.
                                                      1, 1, 0, 1,
                                                      1, 1, 0, 1,
                                                      1, 1, 0, 1,
                                                      1, 1, 0, 1,
                                                      1, 1, 0, 1,
                                                      1, 0, 1, 1,//right face.
                                                      1, 0, 1, 1,
                                                      1, 0, 1, 1,
                                                      1, 0, 1, 1,
                                                      1, 0, 1, 1,
                                                      1, 0, 1, 1,
                                                      0, 1, 0, 1,//left face.
                                                      0, 1, 0, 1,
                                                      0, 1, 0, 1,
                                                      0, 1, 0, 1,
                                                      0, 1, 0, 1,
                                                      0, 1, 0, 1,
                                                      1, 0, 0, 1,//top face.
                                                      1, 0, 0, 1,
                                                      1, 0, 0, 1,
                                                      1, 0, 0, 1,
                                                      1, 0, 0, 1,
                                                      1, 0, 0, 1,
                                                      0, 1, 1, 1,//bottom face.
                                                      0, 1, 1, 1,
                                                      0, 1, 1, 1,
                                                      0, 1, 1, 1,
                                                      0, 1, 1, 1,
                                                      0, 1, 1, 1
    };

    private final String vertexShaderSource;
    private final String fragmentShaderSource;

    public static void main(String[] args) throws Exception {
        new MultiColoredCube();
    }

    private MultiColoredCube() throws Exception {
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
                Utils.createAndShowFrame(canvas, MultiColoredCube.class.getSimpleName(), false);
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
            gl.glEnable(GL3.GL_DEPTH_TEST);
            //enable back-face culling.
            gl.glCullFace(GL3.GL_BACK);
            gl.glEnable(GL3.GL_CULL_FACE);

            //create shaders.
            int positionAttributeIndex = 0;
            int colorAttributeIndex = 1;
            shaderProgramId = OpenGLUtils.createShaderProgram(gl, vertexShaderSource, fragmentShaderSource, positionAttributeIndex, colorAttributeIndex);
            mvpMatrixUniformIndex = gl.glGetUniformLocation(shaderProgramId, OpenGLUtils.MVP_MATRIX);

            //create geometry.
            vertexArrayObjectId = OpenGLUtils.createVertexArray(gl, coordinates, colors, positionAttributeIndex, colorAttributeIndex);
            modelMatrix = MatrixUtils.createModelMatrix(0, 0, 0, 180, 0, 0, 1, 1, 1);

            //create camera.
            viewMatrix = MatrixUtils.createViewMatrix(2, 1, 4, 27, -11, 0);

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
            projectionMatrix = MatrixUtils.createPerspectiveProjectionMatrix(45, aspectRatio, 1, 100);
        }

        @Override
        public void display(GLAutoDrawable drawable) {
            GL3 gl = drawable.getGL().getGL3();
            gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);

            //draw cube.
            gl.glUseProgram(shaderProgramId);
            Matrix4 mvpMatrix = MatrixUtils.multiply(projectionMatrix, MatrixUtils.multiply(viewMatrix, modelMatrix));
            gl.glUniformMatrix4fv(mvpMatrixUniformIndex, 1, false, mvpMatrix.getMatrix(), 0);
            gl.glBindVertexArray(vertexArrayObjectId);
            int vertexCount = coordinates.length/4;
            gl.glDrawArrays(GL3.GL_TRIANGLES, 0, vertexCount);

            int error = gl.glGetError();
            if (error != 0) System.err.println("Error during rendering: " + error);
        }

        @Override
        public void dispose(GLAutoDrawable drawable) {
        }
    };
}
