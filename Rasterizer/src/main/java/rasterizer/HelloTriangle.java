/*
 * Copyright (c) 2018 A.C. Kockx, All Rights Reserved.
 */
package rasterizer;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;

import java.nio.FloatBuffer;

/**
 * This class uses the minimum amount of code required to draw a single triangle on the screen
 * using OpenGL 3 and the JOGL (Java OpenGL) framework.
 * Comments have been added to explain every step.
 * To keep this class as simple as possible, all coordinates are normalized device coordinates,
 * therefore no model, view or projection matrices are needed in this class.
 * Furthermore the shader source code is hard-coded so that all the important parts are contained in a single file.
 *
 * @author A.C. Kockx
 */
public final class HelloTriangle {
    //vertices are in 3D, i.e. 3 coordinates together form 1 vertex.
    private static final int dimension = 3;
    //x, y, z coordinates for three vertices.
    private static final float[] coordinates = new float[]{-0.8f, -0.8f, 0,
                                                            0.8f, -0.8f, 0,
                                                               0,  0.8f, 0
    };
    //hard-coded source code for a pixel shader that colors everything red.
    private static final String fragmentShaderSourceCode = "#version 130\n"
                                                           + "out vec4 outputColor;\n"
                                                           + "void main() {"
                                                           + "    outputColor = vec4(1, 0, 0, 1);"
                                                           + "}\n";

    public static void main(String[] args) throws Exception {
        new HelloTriangle();
    }

    private HelloTriangle() throws Exception {
        //create OpenGL canvas.
        GLCanvas canvas = OpenGLUtils.createGLCanvas(800, 600);
        //canvas can be thought of as an ordinary java.awt.Component. The glEventListener contains all OpenGL-related code.
        canvas.addGLEventListener(glEventListener);

        //init GUI on event-dispatching thread.
        javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                //create a JFrame and add canvas to it.
                Utils.createAndShowFrame(canvas, HelloTriangle.class.getSimpleName(), false);
            }
        });
    }

    private final GLEventListener glEventListener = new GLEventListener() {
        private int shaderProgramId = -1;
        private int vertexArrayObjectId = -1;

        @Override
        public void init(GLAutoDrawable drawable) {
            GL3 gl = drawable.getGL().getGL3();

            //set clear color to dark blue.
            gl.glClearColor(0, 0, 0.5f, 1);

            //create and compile fragment shader (pixel shader).
            int fragmentShaderId = OpenGLUtils.createShader(gl, GL3.GL_FRAGMENT_SHADER, fragmentShaderSourceCode);
            gl.glCompileShader(fragmentShaderId);
            //create and link shader program.
            shaderProgramId = gl.glCreateProgram();
            gl.glAttachShader(shaderProgramId, fragmentShaderId);
            gl.glLinkProgram(shaderProgramId);

            //store vertex data in graphics card memory.
            //create a new vertex buffer.
            int vertexBufferObjectId = OpenGLUtils.createVertexBufferObject(gl);
            //make the new vertex buffer "active".
            gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vertexBufferObjectId);
            //copy vertex data to the active vertex buffer.
            gl.glBufferData(GL3.GL_ARRAY_BUFFER, coordinates.length*Float.BYTES, FloatBuffer.wrap(coordinates), GL3.GL_STATIC_DRAW);

            //create a new vertex array.
            vertexArrayObjectId = OpenGLUtils.createVertexArrayObject(gl);
            //make the new vertex array "active".
            gl.glBindVertexArray(vertexArrayObjectId);
            //set the first attribute of the active vertex array to point to the data in the active vertex buffer.
            int firstVertexAttributeIndex = 0;
            gl.glVertexAttribPointer(firstVertexAttributeIndex, dimension, GL3.GL_FLOAT, false, 0, 0);
            //enable the first attribute of the active vertex array.
            gl.glEnableVertexAttribArray(firstVertexAttributeIndex);

            int error = gl.glGetError();
            if (error != 0) System.err.println("Error during initialization: " + error);
        }

        @Override
        public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        }

        @Override
        public void display(GLAutoDrawable drawable) {
            GL3 gl = drawable.getGL().getGL3();

            //clear drawing surface.
            gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);

            //draw scene.
            //make shader program "active".
            gl.glUseProgram(shaderProgramId);
            //make vertex array "active".
            gl.glBindVertexArray(vertexArrayObjectId);
            //draw triangles using the vertex data in the active vertex array.
            int startIndex = 0;
            int vertexCount = coordinates.length/dimension;
            //note that this uses vertexCount, not triangleCount.
            gl.glDrawArrays(GL3.GL_TRIANGLES, startIndex, vertexCount);

            int error = gl.glGetError();
            if (error != 0) System.err.println("Error during rendering: " + error);
        }

        @Override
        public void dispose(GLAutoDrawable drawable) {
        }
    };
}
