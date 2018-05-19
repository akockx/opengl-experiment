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
    public static final String MODEL_VIEW_PROJECTION_MATRIX = "modelViewProjectionMatrix";
    public static final String MODEL_VIEW_MATRIX = "modelViewMatrix";
    public static final String VERTEX_POSITION = "vertexPosition";
    public static final String VERTEX_NORMAL = "vertexNormal";
    public static final String VERTEX_COLOR = "vertexColor";
    public static final String VERTEX_UV_COORDINATES = "vertexUVCoordinates";
    public static final String TEXTURE_MATRIX = "textureMatrix";
    public static final String LIGHT_POSITION = "lightPosition";
    public static final String LIGHT_INTENSITY = "lightIntensity";
    public static final String AMBIENT_LIGHT_INTENSITY = "ambientLightIntensity";
    public static final String SPECULAR_REFLECTION_COEFFICIENT = "specularReflectionCoefficient";
    public static final String SHININESS = "shininess";
    private static final String FRAGMENT_COLOR = "fragmentColor";

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

    /**
     * @param attributeDimensions dimension for each attribute, in the order of increasing attribute index (0, 1, 2, etc.).
     * @param attributeDataArrays one data array for each attribute, in the order of increasing attribute index (0, 1, 2, etc.).
     * @return id of created vertex array object.
     */
    public static int createVertexArray(GL3 gl, int[] attributeDimensions, float[][] attributeDataArrays) {
        if (attributeDimensions.length != attributeDataArrays.length) throw new IllegalArgumentException("attributeDimensions.length != attributeDataArrays.length");

        int vertexArrayObjectId = OpenGLUtils.createVertexArrayObject(gl);
        gl.glBindVertexArray(vertexArrayObjectId);

        for (int attributeIndex = 0; attributeIndex < attributeDimensions.length; attributeIndex++) {
            int dimension = attributeDimensions[attributeIndex];
            float[] data = attributeDataArrays[attributeIndex];

            int vertexBufferObjectId = OpenGLUtils.createVertexBufferObject(gl);
            gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vertexBufferObjectId);
            gl.glBufferData(GL3.GL_ARRAY_BUFFER, data.length*Float.BYTES, FloatBuffer.wrap(data), GL3.GL_STATIC_DRAW);
            gl.glVertexAttribPointer(attributeIndex, dimension, GL3.GL_FLOAT, false, 0, 0);
            gl.glEnableVertexAttribArray(attributeIndex);
        }

        return vertexArrayObjectId;
    }

    /**
     * @param vertexAttributeNames names of the attributes. These will be linked to attribute indices 0, 1, 2, etc. in the order in which they are given.
     * @return id of created shader program.
     */
    public static int createShaderProgram(GL3 gl, int[] shaderTypes, String[] shaderSources, String[] vertexAttributeNames) {
        if (shaderTypes.length != shaderSources.length) throw new IllegalArgumentException("shaderTypes.length != shaderSources.length");

        //create and compile shaders.
        int[] shaderIds = new int[shaderTypes.length];
        for (int n = 0; n < shaderTypes.length; n++) {
            shaderIds[n] = createShader(gl, shaderTypes[n], shaderSources[n]);
            compileShader(gl, shaderIds[n]);
        }

        //link shaders into a shader program.
        int programId = gl.glCreateProgram();
        for (int shaderId : shaderIds) {
            gl.glAttachShader(programId, shaderId);
        }
        //link vertex shader input variables to attribute indices.
        for (int attributeIndex = 0; attributeIndex < vertexAttributeNames.length; attributeIndex++) {
            gl.glBindAttribLocation(programId, attributeIndex, vertexAttributeNames[attributeIndex]);
        }
        //link fragment shader output variable to color index 0.
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

        gl.glValidateProgram(shaderProgramId);
        int validateStatus = getShaderProgramParameter(gl, shaderProgramId, GL3.GL_VALIDATE_STATUS);
        if (validateStatus != 1) System.err.println("Shader program " + shaderProgramId + " validate status: " + validateStatus);

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
