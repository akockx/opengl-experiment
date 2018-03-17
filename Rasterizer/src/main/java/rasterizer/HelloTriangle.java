/*
 * Copyright (c) 2018 A.C. Kockx, All Rights Reserved.
 */
package rasterizer;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;

import java.nio.FloatBuffer;

/**
 * Uses OpenGL 3.0 to draw a single triangle.
 *
 * @author A.C. Kockx
 */
public final class HelloTriangle {
    //vertices are in 3D, i.e. 3 coordinates together form 1 vertex.
    private final int dimension = 3;
    //x, y, z coordinates for three vertices.
    private final float[] coordinates = new float[]{-0.8f, -0.8f, 0,
                                                     0.8f, -0.8f, 0,
                                                        0,  0.8f, 0
    };

    public static void main(String[] args) throws Exception {
        new HelloTriangle();
    }

    private HelloTriangle() throws Exception {
        //create OpenGL canvas.
        GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities capabilities = new GLCapabilities(profile);
        GLCanvas canvas = new GLCanvas(capabilities);
        canvas.addGLEventListener(glEventListener);
        canvas.setSize(800, 600);

        //init GUI on event-dispatching thread.
        javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                Utils.createAndShowFrame(canvas, "OpenGL canvas", false);
            }
        });
    }

    private final GLEventListener glEventListener = new GLEventListener() {
        private int vertexArrayObjectId = -1;

        @Override
        public void init(GLAutoDrawable drawable) {
            GL2 gl = drawable.getGL().getGL2();

            //set clear color to blue.
            gl.glClearColor(0, 0, 1, 1);

            //store vertex data in graphics card memory.
            //create a new vertex buffer.
            int vertexBufferObjectId = OpenGLUtils.createVertexBufferObject(gl);
            //make the new vertex buffer "active".
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexBufferObjectId);
            //copy vertex data to the active vertex buffer.
            gl.glBufferData(GL.GL_ARRAY_BUFFER, coordinates.length*Float.BYTES, FloatBuffer.wrap(coordinates), GL.GL_STATIC_DRAW);

            //create a new vertex array.
            vertexArrayObjectId = OpenGLUtils.createVertexArrayObject(gl);
            //make the new vertex array "active".
            gl.glBindVertexArray(vertexArrayObjectId);
            //set the first attribute of the active vertex array to point to the data in the active vertex buffer.
            int firstVertexAttributeIndex = 0;
            gl.glVertexAttribPointer(firstVertexAttributeIndex, dimension, GL.GL_FLOAT, false, 0, 0);
            //enable the first attribute of the active vertex array.
            gl.glEnableVertexAttribArray(firstVertexAttributeIndex);

            int error = gl.glGetError();
            if (error != 0) System.err.println("Error during initialization: " + error);
        }

        @Override
        public void display(GLAutoDrawable drawable) {
            GL2 gl = drawable.getGL().getGL2();

            //clear drawing surface.
            gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

            //uncomment this line to draw in wire-frame mode.
            //gl.glPolygonMode(GL.GL_FRONT, GL2.GL_LINE);

            //draw scene.
            //make vertex array "active".
            gl.glBindVertexArray(vertexArrayObjectId);
            //draw triangles using the vertex data in the active vertex array.
            int startIndex = 0;
            int vertexCount = coordinates.length/dimension;
            //note that this uses vertexCount, not triangleCount.
            gl.glDrawArrays(GL.GL_TRIANGLES, startIndex, vertexCount);

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
