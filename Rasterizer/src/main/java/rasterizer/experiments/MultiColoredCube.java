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

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Uses OpenGL 3 to draw a multi-colored cube on the screen.
 * The cube is lit by a single point light and is shaded using an implementation
 * of Phong shading, see https://en.wikipedia.org/wiki/Phong_shading
 *
 * @author A.C. Kockx
 */
public final class MultiColoredCube {
    private static final float ROTATION_SPEED = (float) (Math.PI/2);//radians/second.
    private static final float FRAME_RATE = 30;//frames/second.
    private static final float DELTA_T = 1/FRAME_RATE;//in seconds.

    private final int dimensionCount = 3;
    //vertex coordinates (x, y, z) in model space.
    private final float[] coordinates = new float[]{ 0.5f, -0.5f, -0.5f,//front face.
                                                           -0.5f, -0.5f, -0.5f,
                                                            0.5f,  0.5f, -0.5f,
                                                           -0.5f,  0.5f, -0.5f,
                                                           -0.5f, -0.5f,  0.5f,//back face.
                                                            0.5f, -0.5f,  0.5f,
                                                           -0.5f,  0.5f,  0.5f,
                                                            0.5f,  0.5f,  0.5f,
                                                            0.5f, -0.5f,  0.5f,//right face.
                                                            0.5f, -0.5f, -0.5f,
                                                            0.5f,  0.5f,  0.5f,
                                                            0.5f,  0.5f, -0.5f,
                                                           -0.5f, -0.5f, -0.5f,//left face.
                                                           -0.5f, -0.5f,  0.5f,
                                                           -0.5f,  0.5f, -0.5f,
                                                           -0.5f,  0.5f,  0.5f,
                                                           -0.5f,  0.5f,  0.5f,//top face.
                                                            0.5f,  0.5f,  0.5f,
                                                           -0.5f,  0.5f, -0.5f,
                                                            0.5f,  0.5f, -0.5f,
                                                           -0.5f, -0.5f, -0.5f,//bottom face.
                                                            0.5f, -0.5f, -0.5f,
                                                           -0.5f, -0.5f,  0.5f,
                                                            0.5f, -0.5f,  0.5f
    };
    //vertex normal vectors (x, y, z) in model space.
    private final float normalVectors[] = new float[]{ 0,  0, -1,//front face.
                                                              0,  0, -1,
                                                              0,  0, -1,
                                                              0,  0, -1,
                                                              0,  0,  1,//back face.
                                                              0,  0,  1,
                                                              0,  0,  1,
                                                              0,  0,  1,
                                                              1,  0,  0,//right face.
                                                              1,  0,  0,
                                                              1,  0,  0,
                                                              1,  0,  0,
                                                             -1,  0,  0,//left face.
                                                             -1,  0,  0,
                                                             -1,  0,  0,
                                                             -1,  0,  0,
                                                              0,  1,  0,//top face.
                                                              0,  1,  0,
                                                              0,  1,  0,
                                                              0,  1,  0,
                                                              0, -1,  0,//bottom face.
                                                              0, -1,  0,
                                                              0, -1,  0,
                                                              0, -1,  0
    };
    //vertex colors (r, g, b).
    private final float colors[] = new float[]{0, 0, 1,//front face.
                                                      0, 0, 1,
                                                      0, 0, 1,
                                                      0, 0, 1,
                                                      1, 1, 0,//back face.
                                                      1, 1, 0,
                                                      1, 1, 0,
                                                      1, 1, 0,
                                                      1, 0, 1,//right face.
                                                      1, 0, 1,
                                                      1, 0, 1,
                                                      1, 0, 1,
                                                      0, 1, 0,//left face.
                                                      0, 1, 0,
                                                      0, 1, 0,
                                                      0, 1, 0,
                                                      1, 0, 0,//top face.
                                                      1, 0, 0,
                                                      1, 0, 0,
                                                      1, 0, 0,
                                                      0, 1, 1,//bottom face.
                                                      0, 1, 1,
                                                      0, 1, 1,
                                                      0, 1, 1
    };
    private final float specularReflectionCoefficient = 0.9f;
    private final float shininess = 15;

    //light source intensity per color component (r, g, b).
    private final float[] lightIntensity = new float[]{25, 25, 25};
    //ambient light intensity per color component (r, g, b).
    private final float[] ambientLightIntensity = new float[]{0.1f, 0.15f, 0.2f};

    //at any given moment this stores the keyCodes of the keys that are currently being pressed down.
    private final Set<Integer> pressedKeys = Collections.synchronizedSet(new HashSet<>());
    private final GLCanvas glCanvas;

    //current polar coordinates of light source in world space.
    private float radius = 5;
    private float yaw = -1.1f;//in radians.
    private float pitch = 0.1f;//in radians.

    public static void main(String[] args) throws Exception {
        new MultiColoredCube();
    }

    private MultiColoredCube() throws Exception {
        //create OpenGL canvas.
        glCanvas = OpenGLUtils.createGLCanvas(800, 600);
        glCanvas.addGLEventListener(glEventListener);

        //init GUI on event-dispatching thread.
        javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                JLabel label = new JLabel("ARROW KEYS = rotate light position around cube")  ;
                label.setBorder(new EmptyBorder(5, 5, 5, 5));
                label.setForeground(Color.GREEN);
                label.setBackground(Color.BLACK);
                label.setOpaque(true);

                JPanel panel = new JPanel(new BorderLayout());
                panel.add(label, BorderLayout.NORTH);
                panel.add(glCanvas, BorderLayout.CENTER);

                Utils.createAndShowFrame(panel, MultiColoredCube.class.getSimpleName(), false);
                glCanvas.addKeyListener(keyListener);
                glCanvas.requestFocus();
            }
        });

        //start interaction loop.
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(interactionLoop, 0, (long) (1000*DELTA_T), TimeUnit.MILLISECONDS);
    }

    private final KeyListener keyListener = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            pressedKeys.add(e.getExtendedKeyCode());
        }

        @Override
        public void keyReleased(KeyEvent e) {
            pressedKeys.remove(e.getExtendedKeyCode());
        }
    };

    private final Runnable interactionLoop = new Runnable() {
        @Override
        public void run() {
            boolean viewDirty = false;
            if (pressedKeys.contains(KeyEvent.VK_RIGHT)) {//rotate right.
                yaw += ROTATION_SPEED*DELTA_T;
                viewDirty = true;
            } else if (pressedKeys.contains(KeyEvent.VK_LEFT)) {//rotate left.
                yaw -= ROTATION_SPEED*DELTA_T;
                viewDirty = true;
            }

            if (pressedKeys.contains(KeyEvent.VK_UP)) {//rotate up.
                pitch += ROTATION_SPEED*DELTA_T;
                pitch = (float) Math.min(pitch, Math.PI/4);
                viewDirty = true;
            } else if (pressedKeys.contains(KeyEvent.VK_DOWN)) {//rotate down.
                pitch -= ROTATION_SPEED*DELTA_T;
                pitch = (float) Math.max(pitch, -Math.PI/4);
                viewDirty = true;
            }

            if (viewDirty) glCanvas.repaint();
        }
    };

    private final GLEventListener glEventListener = new GLEventListener() {
        private int shaderProgramId = -1;
        private int modelViewProjectionMatrixUniformIndex = -1;
        private int modelViewMatrixUniformIndex = -1;
        private int lightPositionUniformIndex = -1;
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
            gl.glEnable(GL3.GL_CULL_FACE);
            gl.glCullFace(GL3.GL_BACK);

            //create shaders.
            ResourceLoader loader = new ResourceLoader("/rasterizer/shaders/");
            String vertexShaderSource;
            String fragmentShaderSource;
            try {
                vertexShaderSource = Utils.read(loader.loadResource("phong_vertex_shader.glsl"));
                fragmentShaderSource = Utils.read(loader.loadResource("phong_fragment_shader.glsl"));
            } catch (Exception e) {
                throw new RuntimeException("Error while loading shader source: " + e.getMessage(), e);
            }
            shaderProgramId = OpenGLUtils.createShaderProgram(gl,
                    new int[]{GL3.GL_VERTEX_SHADER, GL3.GL_FRAGMENT_SHADER}, new String[]{vertexShaderSource, fragmentShaderSource},
                    new String[]{OpenGLUtils.VERTEX_POSITION, OpenGLUtils.VERTEX_NORMAL, OpenGLUtils.VERTEX_COLOR});
            modelViewProjectionMatrixUniformIndex = gl.glGetUniformLocation(shaderProgramId, OpenGLUtils.MODEL_VIEW_PROJECTION_MATRIX);
            modelViewMatrixUniformIndex = gl.glGetUniformLocation(shaderProgramId, OpenGLUtils.MODEL_VIEW_MATRIX);
            gl.glUseProgram(shaderProgramId);
            gl.glUniform1f(gl.glGetUniformLocation(shaderProgramId, OpenGLUtils.SPECULAR_REFLECTION_COEFFICIENT), specularReflectionCoefficient);
            gl.glUniform1f(gl.glGetUniformLocation(shaderProgramId, OpenGLUtils.SHININESS), shininess);
            lightPositionUniformIndex = gl.glGetUniformLocation(shaderProgramId, OpenGLUtils.LIGHT_POSITION);

            //create geometry.
            vertexArrayObjectId = OpenGLUtils.createVertexArray(gl,
                    new int[]{dimensionCount, dimensionCount, dimensionCount}, new float[][]{coordinates, normalVectors, colors});
            modelMatrix = MatrixUtils.createModelMatrix(0, 0, 0, 160, 0, 0, 1, 1, 1);

            //create camera.
            viewMatrix = MatrixUtils.createViewMatrix(0, 1, 4, 0, -11, 0);

            //create light.
            gl.glUniform3fv(gl.glGetUniformLocation(shaderProgramId, OpenGLUtils.LIGHT_INTENSITY), 1, lightIntensity, 0);
            gl.glUniform3fv(gl.glGetUniformLocation(shaderProgramId, OpenGLUtils.AMBIENT_LIGHT_INTENSITY), 1, ambientLightIntensity, 0);

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
            //calculate matrices.
            Matrix4 modelViewMatrix = MatrixUtils.multiply(viewMatrix, modelMatrix);
            gl.glUniformMatrix4fv(modelViewMatrixUniformIndex, 1, false, modelViewMatrix.getMatrix(), 0);
            Matrix4 modelViewProjectionMatrix = MatrixUtils.multiply(projectionMatrix, modelViewMatrix);
            gl.glUniformMatrix4fv(modelViewProjectionMatrixUniformIndex, 1, false, modelViewProjectionMatrix.getMatrix(), 0);
            float[] lightPositionInWorldSpace = new float[]{(float) (radius*Math.cos(pitch)*Math.cos(yaw)),
                                                            (float) (radius*Math.sin(pitch)), (float) (radius*Math.cos(pitch)*-Math.sin(yaw)), 1};
            float[] lightPositionInCameraSpace = MatrixUtils.multiply(viewMatrix, lightPositionInWorldSpace);
            gl.glUniform3fv(lightPositionUniformIndex, 1, lightPositionInCameraSpace, 0);
            //draw vertices.
            gl.glBindVertexArray(vertexArrayObjectId);
            for (int face = 0; face < 6; face++) {
                gl.glDrawArrays(GL3.GL_TRIANGLE_STRIP, face*4, 4);
            }

            int error = gl.glGetError();
            if (error != 0) System.err.println("Error during rendering: " + error);
        }

        @Override
        public void dispose(GLAutoDrawable drawable) {
        }
    };
}
