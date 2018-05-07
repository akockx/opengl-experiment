/*
 * Copyright (c) 2018 A.C. Kockx, All Rights Reserved.
 */
package rasterizer.experiments;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.math.Matrix4;
import rasterizer.util.OpenGLUtils;
import rasterizer.util.ResourceLoader;
import rasterizer.util.Utils;

import javax.swing.JFrame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Draws the Mandelbrot set on the screen using OpenGL 3.
 * All calculations are performed by the GPU using a fragment shader.
 *
 * @author A.C. Kockx
 */
public final class GpuMandelbrot {
    private static final float FRAME_RATE = 30;//frames/second.
    private static final float ZOOM_SPEED = 4;//ratio/second.
    private static final float PAN_SPEED = 3;//units/second.

    //normalized device coordinates (x, y, z) of quad corners.
    private final float[] vertexCoordinates = new float[]{-1, -1, 0,
                                                           1, -1, 0,
                                                          -1,  1, 0,
                                                           1,  1, 0};
    //initial u,v-coordinates of quad corners.
    private final float[] vertexUVCoordinates = new float[]{-2, -2,
                                                             2, -2,
                                                            -2,  2,
                                                             2,  2};

    private final float deltaT = 1/FRAME_RATE;//in seconds.
    private final GLCanvas glCanvas;

    //coordinates of current view in fractal space.
    private float u = -0.5f;
    private float v = 0;
    private float magnification = 1;
    private float aspectRatio = 1;

    //at any given moment this stores the keyCodes of the keys that are currently being pressed down.
    private final Set<Integer> pressedKeys = Collections.synchronizedSet(new HashSet<>());

    public static void main(String[] args) throws Exception {
        new GpuMandelbrot();
    }

    private GpuMandelbrot() throws Exception {
        //create OpenGL canvas.
        glCanvas = OpenGLUtils.createGLCanvas(800, 600);
        glCanvas.addGLEventListener(glEventListener);

        //init GUI on event-dispatching thread.
        javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                JFrame frame = Utils.createAndShowFrame(glCanvas, GpuMandelbrot.class.getSimpleName(), false);
                frame.addKeyListener(keyListener);
            }
        });

        //start interaction loop.
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(interactionLoop, 0, (long) (1000*deltaT), TimeUnit.MILLISECONDS);
    }

    private final KeyListener keyListener = new KeyListener() {
        @Override
        public void keyPressed(KeyEvent e) {
            pressedKeys.add(e.getKeyCode());
        }

        @Override
        public void keyReleased(KeyEvent e) {
            pressedKeys.remove(e.getKeyCode());
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }
    };

    private final Runnable interactionLoop = new Runnable() {
        @Override
        public void run() {
            boolean viewDirty = false;
            if (pressedKeys.contains(KeyEvent.VK_PAGE_UP)) {//zoom in.
                magnification *= Math.pow(ZOOM_SPEED, deltaT);
                viewDirty = true;
            } else if (pressedKeys.contains(KeyEvent.VK_PAGE_DOWN)) {//zoom out.
                magnification /= Math.pow(ZOOM_SPEED, deltaT);
                viewDirty = true;
            }

            if (pressedKeys.contains(KeyEvent.VK_RIGHT)) {//pan right.
                u += PAN_SPEED*deltaT/magnification;
                viewDirty = true;
            } else if (pressedKeys.contains(KeyEvent.VK_LEFT)) {//pan left.
                u -= PAN_SPEED*deltaT/magnification;
                viewDirty = true;
            }

            if (pressedKeys.contains(KeyEvent.VK_UP)) {//pan up.
                v += PAN_SPEED*deltaT/magnification;
                viewDirty = true;
            } else if (pressedKeys.contains(KeyEvent.VK_DOWN)) {//pan down.
                v -= PAN_SPEED*deltaT/magnification;
                viewDirty = true;
            }

            if (viewDirty) glCanvas.repaint();
        }
    };

    private final GLEventListener glEventListener = new GLEventListener() {
        private int shaderId = -1;
        private int mvpMatrixUniformIndex = -1;
        private int textureMatrixUniformIndex = -1;
        private int quadId = -1;

        @Override
        public void init(GLAutoDrawable drawable) {
            GL3 gl = drawable.getGL().getGL3();
            gl.glClearColor(0, 0, 0, 1);

            //create shaders.
            ResourceLoader loader = new ResourceLoader("/rasterizer/shaders/");
            String vertexShaderSource;
            String fragmentShaderSource;
            try {
                vertexShaderSource = Utils.read(loader.loadResource("uv_vertex_shader.glsl"));
                fragmentShaderSource = Utils.read(loader.loadResource("mandelbrot_fragment_shader.glsl"));
            } catch (Exception e) {
                throw new RuntimeException("Error while loading shader source: " + e.getMessage(), e);
            }
            shaderId = OpenGLUtils.createShaderProgram(gl, vertexShaderSource, fragmentShaderSource,
                    new String[]{OpenGLUtils.VERTEX_POSITION, OpenGLUtils.VERTEX_UV_COORDINATES});
            mvpMatrixUniformIndex = gl.glGetUniformLocation(shaderId, OpenGLUtils.MODEL_VIEW_PROJECTION_MATRIX);
            textureMatrixUniformIndex = gl.glGetUniformLocation(shaderId, OpenGLUtils.TEXTURE_MATRIX);

            //create geometry (a quad that fills the entire screen).
            quadId = OpenGLUtils.createVertexArray(gl, new int[]{3, 2}, new float[][]{vertexCoordinates, vertexUVCoordinates});

            int error = gl.glGetError();
            if (error != 0) System.err.println("Error during initialization: " + error);
        }

        @Override
        public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
            //calculate aspect ratio.
            if (width <= 0) width = 1;//to avoid divide by zero.
            if (height <= 0) height = 1;//to avoid divide by zero.
            aspectRatio = width/((float) height);
        }

        @Override
        public void display(GLAutoDrawable drawable) {
            GL3 gl = drawable.getGL().getGL3();
            gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);

            //draw quad.
            gl.glUseProgram(shaderId);
            //vertex coordinates are normalized device coordinates, so model-view-projection matrix is equal to the identity matrix.
            Matrix4 mvpMatrix = new Matrix4();
            gl.glUniformMatrix4fv(mvpMatrixUniformIndex, 1, false, mvpMatrix.getMatrix(), 0);
            //transformation matrix for u,v-coordinates in column-major order.
            float[] textureMatrix = {aspectRatio/magnification,               0, 0,
                                                             0, 1/magnification, 0,
                                                             u,               v, 1};
            gl.glUniformMatrix3fv(textureMatrixUniformIndex, 1, false, textureMatrix, 0);
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
