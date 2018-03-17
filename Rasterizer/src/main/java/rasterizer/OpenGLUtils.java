/*
 * Copyright (c) 2018 A.C. Kockx, All Rights Reserved.
 */
package rasterizer;

import com.jogamp.opengl.GL2;

/**
 * @author A.C. Kockx
 */
public final class OpenGLUtils {
    private OpenGLUtils() {
    }

    /**
     * Creates a new vertexArrayObject.
     *
     * @param gl
     * @return id of created vertexArrayObject.
     */
    public static int createVertexArrayObject(GL2 gl) {
        int numberOfVertexArrays = 1;
        int[] vertexArrayIds = new int[numberOfVertexArrays];
        gl.glGenVertexArrays(numberOfVertexArrays, vertexArrayIds, 0);
        return vertexArrayIds[0];
    }

    /**
     * Creates a new vertexBufferObject in the active vertexArrayObject.
     *
     * @param gl
     * @return id of created vertexBufferObject.
     */
    public static int createVertexBufferObject(GL2 gl) {
        int numberOfVertexBuffers = 1;
        int[] vertexBufferIds = new int[numberOfVertexBuffers];
        gl.glGenBuffers(numberOfVertexBuffers, vertexBufferIds, 0);
        return vertexBufferIds[0];
    }
}
