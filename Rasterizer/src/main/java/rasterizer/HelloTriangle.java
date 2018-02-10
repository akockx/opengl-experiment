/*
 * Copyright (c) 2018 A.C. Kockx, All Rights Reserved.
 */
package rasterizer;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;

import java.nio.FloatBuffer;

/**
 * @author A.C. Kockx
 */
public final class HelloTriangle {

    public static void main(String[] args) throws Exception {
        //create OpenGL canvas.
        GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities capabilities = new GLCapabilities(profile);
        GLCanvas canvas = new GLCanvas(capabilities);
        canvas.addGLEventListener(new GLEventListener() {
            @Override
            public void init(GLAutoDrawable drawable) {
                GL4 gl = drawable.getGL().getGL4();

                //create vao.
                int numberOfArrays = 1;
                int[] arrayIds = new int[numberOfArrays];
                gl.glGenVertexArrays(numberOfArrays, arrayIds, 0);
                int vertexArrayObjectId = arrayIds[0];
                gl.glBindVertexArray(vertexArrayObjectId);
            }

            @Override
            public void display(GLAutoDrawable drawable) {
                GL4 gl = drawable.getGL().getGL4();

                int dimension = 3;//vertices are in 3D, i.e. every 3 coordinates form 1 vertex.
                float[] vertices = new float[]{-1f, -1f, 0f,
                                                1f, -1f, 0f,
                                                0f,  1f, 0f
                };

                //create buffer.
                int numberOfBuffers = 1;
                int[] bufferIds = new int[numberOfBuffers];
                gl.glGenBuffers(numberOfBuffers, bufferIds, 0);
                int vertexBufferObjectId = bufferIds[0];
                gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexBufferObjectId);
                gl.glBufferData(GL.GL_ARRAY_BUFFER, vertices.length*Float.BYTES, FloatBuffer.wrap(vertices), GL.GL_STATIC_DRAW);

                int attributeIndex = 0;
                gl.glEnableVertexAttribArray(attributeIndex);
                gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexBufferObjectId);
                gl.glVertexAttribPointer(attributeIndex, dimension, GL.GL_FLOAT, false, 0, 0);
                int startIndex = 0;
                int vertexCount = 3;
                gl.glDrawArrays(GL.GL_TRIANGLES, startIndex, vertexCount);//this uses vertexCount, not triangleCount.
                gl.glDisableVertexAttribArray(attributeIndex);
            }

            @Override
            public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
            }

            @Override
            public void dispose(GLAutoDrawable drawable) {
            }
        });
        canvas.setSize(800, 600);

        //init GUI on event-dispatching thread.
        javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                Utils.createAndShowFrame(canvas, "OpenGL canvas", false);
            }
        });
    }
}
