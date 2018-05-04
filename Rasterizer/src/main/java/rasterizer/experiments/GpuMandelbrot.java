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
 * Draws the Mandelbrot set on the screen.
 * All calculations are performed by the GPU using a fragment shader.
 *
 * @author A.C. Kockx
 */
public final class GpuMandelbrot {

    public static void main(String[] args) throws Exception {
        new GpuMandelbrot();
    }

    private GpuMandelbrot() throws Exception {
        //create OpenGL canvas.
        GLCanvas canvas = OpenGLUtils.createGLCanvas(800, 600);
        canvas.addGLEventListener(glEventListener);

        //init GUI on event-dispatching thread.
        javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                Utils.createAndShowFrame(canvas, GpuMandelbrot.class.getSimpleName(), false);
            }
        });
    }

    private final GLEventListener glEventListener = new GLEventListener() {
        private int shaderId = -1;
        private int mvpMatrixUniformIndex = -1;
        private int quadId = -1;

        private Matrix4 modelMatrix = null;
        private Matrix4 viewMatrix = null;
        private Matrix4 projectionMatrix = null;

        @Override
        public void init(GLAutoDrawable drawable) {
            GL3 gl = drawable.getGL().getGL3();
            gl.glClearColor(0, 0, 0, 1);
            gl.glEnable(GL3.GL_DEPTH_TEST);

            //create shaders.
            ResourceLoader loader = new ResourceLoader("/rasterizer/shaders/");
            String vertexShaderSource;
            String fragmentShaderSource;
            try {
                vertexShaderSource = Utils.read(loader.loadResource("texture_vertex_shader.glsl"));
                fragmentShaderSource = Utils.read(loader.loadResource("mandelbrot_fragment_shader.glsl"));
            } catch (Exception e) {
                throw new RuntimeException("Error while loading shader source: " + e.getMessage(), e);
            }
            shaderId = OpenGLUtils.createShaderProgram(gl, vertexShaderSource, fragmentShaderSource,
                    new String[]{OpenGLUtils.VERTEX_POSITION, OpenGLUtils.VERTEX_UV_COORDINATES});
            mvpMatrixUniformIndex = gl.glGetUniformLocation(shaderId, OpenGLUtils.MODEL_VIEW_PROJECTION_MATRIX);

            //create quad at the origin that fills the entire screen.
            float[] quadCoordinates = new float[]{-1, -1, 0,
                                                   1, -1, 0,
                                                  -1,  1, 0,
                                                   1,  1, 0};
            float[] quadUVCoordinates = new float[]{-2, -2,
                                                     2, -2,
                                                    -2,  2,
                                                     2,  2};
            quadId = OpenGLUtils.createVertexArray(gl, new int[]{3, 2}, new float[][]{quadCoordinates, quadUVCoordinates});
            modelMatrix = MatrixUtils.createModelMatrix(0, 0, 0, 0, 0, 0, 1, 1, 1);

            //create camera.
            viewMatrix = MatrixUtils.createViewMatrix(0, 0, 0, 0, 0, 0);

            int error = gl.glGetError();
            if (error != 0) System.err.println("Error during initialization: " + error);
        }

        @Override
        public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
            //this method is called at least once before method display is called for the first time.

            //calculate aspect ratio.
            if (width <= 0) width = 1;//to avoid divide by zero.
            if (height <= 0) height = 1;//to avoid divide by zero.
            float aspectRatio = width/((float) height);

            //(re)initialize projection matrix.
            projectionMatrix = MatrixUtils.createOrthographicProjectionMatrix(2, aspectRatio, -1, 1);
        }

        @Override
        public void display(GLAutoDrawable drawable) {
            GL3 gl = drawable.getGL().getGL3();
            gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);

            //draw quad.
            gl.glUseProgram(shaderId);
            Matrix4 mvpMatrix = MatrixUtils.multiply(projectionMatrix, MatrixUtils.multiply(viewMatrix, modelMatrix));
            gl.glUniformMatrix4fv(mvpMatrixUniformIndex, 1, false, mvpMatrix.getMatrix(), 0);
            gl.glBindVertexArray(quadId);
            gl.glDrawArrays(GL3.GL_TRIANGLE_STRIP, 0, 4);

            int error = gl.glGetError();
            if (error != 0) System.err.println("Error during rendering: " + error);
        }

        @Override
        public void dispose(GLAutoDrawable drawable) {
        }
    };
}
