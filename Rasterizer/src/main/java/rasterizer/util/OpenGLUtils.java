/*
 * Copyright (c) 2018 A.C. Kockx, All Rights Reserved.
 */
package rasterizer.util;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;

import java.nio.FloatBuffer;

/**
 * @author A.C. Kockx
 */
public final class OpenGLUtils {
    private static final String VERTEX_POSITION = "vertex_position";
    private static final String VERTEX_COLOR = "vertex_color";
    private static final String FRAGMENT_COLOR = "fragment_color";
    public static final String MVP_MATRIX = "mvp_matrix";

    private OpenGLUtils() {
    }

    public static GLCanvas createGLCanvas(int width, int height) {
        GLProfile profile = GLProfile.get(GLProfile.GL3);
        GLCapabilities capabilities = new GLCapabilities(profile);
        GLCanvas canvas = new GLCanvas(capabilities);
        canvas.setSize(width, height);
        return canvas;
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

    public static int createVertexArray(GL3 gl, float[] coordinates, float[] colors, int positionAttributeIndex, int colorAttributeIndex) {
        int vertexArrayObjectId = OpenGLUtils.createVertexArrayObject(gl);
        gl.glBindVertexArray(vertexArrayObjectId);

        int positionVboId = OpenGLUtils.createVertexBufferObject(gl);
        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, positionVboId);
        gl.glBufferData(GL3.GL_ARRAY_BUFFER, coordinates.length*Float.BYTES, FloatBuffer.wrap(coordinates), GL3.GL_STATIC_DRAW);
        gl.glVertexAttribPointer(positionAttributeIndex, 4, GL3.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(positionAttributeIndex);

        int colorVboId = OpenGLUtils.createVertexBufferObject(gl);
        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, colorVboId);
        gl.glBufferData(GL3.GL_ARRAY_BUFFER, colors.length*Float.BYTES, FloatBuffer.wrap(colors), GL3.GL_STATIC_DRAW);
        gl.glVertexAttribPointer(colorAttributeIndex, 4, GL3.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(colorAttributeIndex);

        return vertexArrayObjectId;
    }

    /**
     * @return id of created shader program.
     */
    public static int createShaderProgram(GL3 gl, String vertexShaderSource, String fragmentShaderSource, int positionAttributeIndex, int colorAttributeIndex) {
        //create shaders.
        int vertexShaderId = createShader(gl, GL3.GL_VERTEX_SHADER, vertexShaderSource);
        int fragmentShaderId = createShader(gl, GL3.GL_FRAGMENT_SHADER, fragmentShaderSource);

        //compile shaders.
        compileShader(gl, vertexShaderId);
        compileShader(gl, fragmentShaderId);

        //link shaders into a shader program.
        int programId = gl.glCreateProgram();
        gl.glAttachShader(programId, vertexShaderId);
        gl.glAttachShader(programId, fragmentShaderId);
        //link vertex shader input variables to attribute indices.
        gl.glBindAttribLocation(programId, positionAttributeIndex, VERTEX_POSITION);
        gl.glBindAttribLocation(programId, colorAttributeIndex, VERTEX_COLOR);
        //link fragment shader output variable to attribute index 0.
        gl.glBindFragDataLocation(programId, 0, FRAGMENT_COLOR);
        linkShaders(gl, programId);
        return programId;
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
