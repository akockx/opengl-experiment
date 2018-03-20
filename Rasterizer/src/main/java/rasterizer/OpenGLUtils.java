/*
 * Copyright (c) 2018 A.C. Kockx, All Rights Reserved.
 */
package rasterizer;

import com.jogamp.opengl.GL3;

import java.nio.FloatBuffer;

/**
 * @author A.C. Kockx
 */
public final class OpenGLUtils {
    private OpenGLUtils() {
    }

    /**
     * @return id of created vertexArrayObject.
     */
    public static int createVertexArrayObject(GL3 gl) {
        int numberOfVertexArrays = 1;
        int[] vertexArrayIds = new int[numberOfVertexArrays];
        gl.glGenVertexArrays(numberOfVertexArrays, vertexArrayIds, 0);
        return vertexArrayIds[0];
    }

    /**
     * @return id of created vertexBufferObject.
     */
    public static int createVertexBufferObject(GL3 gl) {
        int numberOfVertexBuffers = 1;
        int[] vertexBufferIds = new int[numberOfVertexBuffers];
        gl.glGenBuffers(numberOfVertexBuffers, vertexBufferIds, 0);
        return vertexBufferIds[0];
    }

    /**
     * @return id of created shader.
     */
    public static int createShader(GL3 gl, int shaderType, String shaderSource) {
        //create shader.
        int shaderId = gl.glCreateShader(shaderType);
        int count = 1;
        gl.glShaderSource(shaderId, count, new String[]{shaderSource}, null, 0);
        return shaderId;
    }

    public static void compileShader(GL3 gl, int shaderId) {
        gl.glCompileShader(shaderId);

        int compileStatus = getShaderParameter(gl, shaderId, GL3.GL_COMPILE_STATUS);
        if (compileStatus != 1) System.err.println("Shader " + shaderId + " compile status: " + compileStatus);

        String error = getShaderInfoLog(gl, shaderId);
        if (error != null) System.err.println("Shader " + shaderId + " error: " + error);
    }

    public static void linkShaders(GL3 gl, int shaderProgramId) {
        gl.glLinkProgram(shaderProgramId);

        int linkStatus = getShaderProgramParameter(gl, shaderProgramId, GL3.GL_LINK_STATUS);
        if (linkStatus != 1) System.err.println("Shader program " + shaderProgramId + " link status: " + linkStatus);

        String error = getShaderProgramInfoLog(gl, shaderProgramId);
        if (error != null) System.err.println("Shader program " + shaderProgramId + " error: " + error);
    }

    private static String getShaderInfoLog(GL3 gl, int shaderId) {
        int infoLogLength = getShaderParameter(gl, shaderId, GL3.GL_INFO_LOG_LENGTH);
        if (infoLogLength <= 0) return null;

        byte[] returnedBytes = new byte[infoLogLength + 1];
        gl.glGetShaderInfoLog(shaderId, infoLogLength, null, 0, returnedBytes, 0);
        return new String(returnedBytes);
    }

    private static String getShaderProgramInfoLog(GL3 gl, int shaderProgramId) {
        int infoLogLength = getShaderProgramParameter(gl, shaderProgramId, GL3.GL_INFO_LOG_LENGTH);
        if (infoLogLength <= 0) return null;

        byte[] returnedBytes = new byte[infoLogLength + 1];
        gl.glGetProgramInfoLog(shaderProgramId, infoLogLength, null, 0, returnedBytes, 0);
        return new String(returnedBytes);
    }

    private static int getShaderParameter(GL3 gl, int shaderId, int parameterName) {
        int parameters[] = new int[1];
        gl.glGetShaderiv(shaderId, parameterName, parameters, 0);
        return parameters[0];
    }

    private static int getShaderProgramParameter(GL3 gl, int shaderProgramId, int parameterName) {
        int parameters[] = new int[1];
        gl.glGetProgramiv(shaderProgramId, parameterName, parameters, 0);
        return parameters[0];
    }
}
