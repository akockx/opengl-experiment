/*
 * Copyright (c) 2018 A.C. Kockx, All Rights Reserved.
 */
package rasterizer.experiments;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.math.Matrix4;
import com.jogamp.opengl.math.VectorUtil;
import rasterizer.util.MatrixUtils;
import rasterizer.util.OpenGLUtils;
import rasterizer.util.ResourceLoader;
import rasterizer.util.Utils;

import java.util.Arrays;

import static java.lang.Math.*;

/**
 * Uses OpenGL 3 to draw a rainbow that follows a curve through 3D space.
 *
 * @author A.C. Kockx
 */
public final class Rainbow {
    //colors of the rainbow (r, g, b).
    private final float rainbowColors[] = new float[]{1,    0, 0,
                                                      1, 0.5f, 0,
                                                      1,    1, 0,
                                                      0,    1, 0,
                                                      0,    1, 1,
                                                      0,    0, 1,
                                                      0.5f, 0, 1};
    private final float colorLocations[] = new float[]{0, 1/6f, 2/6f, 3/6f, 4/6f, 5/6f, 1};

    private final int dimensionCount = 3;
    private final String vertexShaderSource;
    private final String fragmentShaderSource;

    public static void main(String[] args) throws Exception {
        new Rainbow();
    }

    private Rainbow() throws Exception {
        //load shader source.
        ResourceLoader loader = new ResourceLoader("/rasterizer/shaders/");
        vertexShaderSource = Utils.read(loader.loadResource("uv_vertex_shader.glsl"));
        fragmentShaderSource = Utils.read(loader.loadResource("color_gradient_fragment_shader.glsl"));

        //create OpenGL canvas.
        GLCanvas canvas = OpenGLUtils.createGLCanvas(800, 600);
        canvas.addGLEventListener(glEventListener);

        //init GUI on event-dispatching thread.
        javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                Utils.createAndShowFrame(canvas, Rainbow.class.getSimpleName(), false);
            }
        });
    }

    private final GLEventListener glEventListener = new GLEventListener() {
        private int shaderProgramId = -1;
        private int mvpMatrixUniformIndex = -1;
        private int[] triangleStripIds = null;
        private int vertexCountPerTriangleStrip = -1;

        private Matrix4 modelMatrix = null;
        private Matrix4 viewMatrix = null;
        private Matrix4 projectionMatrix = null;

        @Override
        public void init(GLAutoDrawable drawable) {
            GL3 gl = drawable.getGL().getGL3();
            gl.glClearColor(0, 0, 0, 1);
            gl.glEnable(GL3.GL_DEPTH_TEST);

            //create shaders.
            shaderProgramId = OpenGLUtils.createShaderProgram(gl,
                    new int[]{GL3.GL_VERTEX_SHADER, GL3.GL_FRAGMENT_SHADER}, new String[]{vertexShaderSource, fragmentShaderSource},
                    new String[]{OpenGLUtils.VERTEX_POSITION, OpenGLUtils.VERTEX_UV_COORDINATES});
            mvpMatrixUniformIndex = gl.glGetUniformLocation(shaderProgramId, OpenGLUtils.MODEL_VIEW_PROJECTION_MATRIX);
            int gradientColorsUniformIndex = gl.glGetUniformLocation(shaderProgramId, "gradientColors");
            int locationsUniformIndex = gl.glGetUniformLocation(shaderProgramId, "locations");
            int colorCountUniformIndex = gl.glGetUniformLocation(shaderProgramId, "colorCount");
            gl.glUseProgram(shaderProgramId);
            int colorCount = colorLocations.length;
            gl.glUniform3fv(gradientColorsUniformIndex, colorCount, rainbowColors, 0);
            gl.glUniform1fv(locationsUniformIndex, colorCount, colorLocations, 0);
            gl.glUniform1i(colorCountUniformIndex, colorCount);

            //create rainbow curve.
            //The rainbow starts horizontal (i.e. in the xz-plane) at the origin in model space, going in the negative z direction.
            int pointCount = 100;//to accomodate level of detail in the direction of the length of the rainbow (can be 2 if rainbow is completely straight and flat).
            float[][] points = new float[pointCount][dimensionCount];
            float[] firstSegmentDirectionUnitVector = new float[]{1, 0, 0};
            float[] bankingAnglesInDegrees = new float[pointCount];
            for (int t = 0; t < points.length; t++) {
                float x = (float) -sin(2*PI*t/(pointCount - 1f))/10;
                float y = 0;
                float z = -2*t/(pointCount - 1f);
                points[t] = new float[]{x, y, z};
                bankingAnglesInDegrees[t] = 90*t/(pointCount - 1f);
            }
            //create rainbow geometry.
            float[][][] allVertices = createRainbowGeometry(points, 1, firstSegmentDirectionUnitVector, bankingAnglesInDegrees);
            int segmentCount = allVertices.length;
            int vertexCountPerSegment = allVertices[0].length;
            float[] uCoordinates = new float[vertexCountPerSegment];
            int triangleStripCount = vertexCountPerSegment - 1;
            triangleStripIds = new int[triangleStripCount];
            vertexCountPerTriangleStrip = segmentCount*2;
            //calculate u coordinates.
            for (int vertexIndex = 0; vertexIndex < vertexCountPerSegment; vertexIndex++) {
                uCoordinates[vertexIndex] = vertexIndex/(vertexCountPerSegment - 1f);
            }
            for (int triangleStripIndex = 0; triangleStripIndex < triangleStripCount; triangleStripIndex++) {
                float[] coordinates = new float[vertexCountPerTriangleStrip*dimensionCount];
                float[] uvCoordinates = new float[vertexCountPerTriangleStrip*2];
                //set all u and v coordinates to 0.
                Arrays.fill(uvCoordinates, 0);
                int index = 0;
                for (int segmentIndex = 0; segmentIndex < segmentCount; segmentIndex++) {
                    //uneven vertex.
                    for (int d = 0; d < dimensionCount; d++) {
                        coordinates[index] = allVertices[segmentIndex][triangleStripIndex][d];
                        index++;
                    }
                    uvCoordinates[4*segmentIndex] = uCoordinates[triangleStripIndex];//u.

                    //even vertex.
                    for (int d = 0; d < dimensionCount; d++) {
                        coordinates[index] = allVertices[segmentIndex][triangleStripIndex + 1][d];
                        index++;
                    }
                    uvCoordinates[4*segmentIndex + 2] = uCoordinates[triangleStripIndex + 1];//u.
                }
                triangleStripIds[triangleStripIndex] = OpenGLUtils.createVertexArray(gl, new int[]{dimensionCount, 2}, new float[][]{coordinates, uvCoordinates});
            }
            modelMatrix = MatrixUtils.createModelMatrix(-1, 0, 0, -90, 0, 90, 1, 1, 1);

            //create camera.
            viewMatrix = MatrixUtils.createViewMatrix(0, 0, 4, 0, 0, 0);

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
            projectionMatrix = MatrixUtils.createOrthographicProjectionMatrix(2, aspectRatio, 1, 100);
        }

        @Override
        public void display(GLAutoDrawable drawable) {
            GL3 gl = drawable.getGL().getGL3();
            gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);

            //draw rainbow.
            gl.glUseProgram(shaderProgramId);
            //calculate model-view-projection matrix for rainbow.
            Matrix4 mvpMatrix = MatrixUtils.multiply(projectionMatrix, MatrixUtils.multiply(viewMatrix, modelMatrix));
            //set model-view-projection matrix in the "active" shader program.
            gl.glUniformMatrix4fv(mvpMatrixUniformIndex, 1, false, mvpMatrix.getMatrix(), 0);
            //draw triangle strips.
            for (int triangleStripId : triangleStripIds) {
                gl.glBindVertexArray(triangleStripId);
                gl.glDrawArrays(GL3.GL_TRIANGLE_STRIP, 0, vertexCountPerTriangleStrip);
            }

            int error = gl.glGetError();
            if (error != 0) System.err.println("Error during rendering: " + error);
        }

        @Override
        public void dispose(GLAutoDrawable drawable) {
        }
    };

    /**
     * Creates a rainbow along the given curve.
     *
     * The rainbow follows a curve in 3D space that is described by the given list of points.
     * For each point, the rainbow has one segment. A segment consists of a set of vertices,
     * one for each color of the rainbow, that are positioned equidistantly along a line piece
     * that is centered on the point corresponding to the segment. The line piece has a length
     * equal to the given width.
     * The line piece of the first segment is oriented along the given firstSegmentUAxisUnitVector,
     * which must be perpendicular to the curve.
     * The orientation of each next segment is such that it is perpendicular to the curve
     * and as close as possible to the orientation of the previous segment.
     * In other words if there is a bend in the curve, then the segments are rotated to follow the bend.
     *
     * Afterwards the orientation of each segment is rotated by the corresponding banking angle
     * about an axis tangential to the curve at the segment position.
     */
    private static float[][][] createRainbowGeometry(float[][] points, float width, float[] firstSegmentUAxisUnitVector, float[] bankingAnglesInDegrees) {
        int segmentCount = points.length;
        int vertexCountPerSegment = 20;//to accomodate level of detail in direction of width of rainbow (can be 2 if rainbow is completely flat).
        int dimensionCount = points[0].length;
        float[][][] allVertices = new float[segmentCount][vertexCountPerSegment][dimensionCount];

        //create segments.
        float[] previousPosition = points[0];
        float[] previousUAxisUnitVector = firstSegmentUAxisUnitVector;
        for (int segmentIndex = 0; segmentIndex < segmentCount; segmentIndex++) {
            float[] currentPosition = points[segmentIndex];
            float[] nextPosition;
            if (segmentIndex < segmentCount - 1) {
                nextPosition = points[segmentIndex + 1];
            } else {//if last segment.
                nextPosition = currentPosition;
            }

            //project previous u-axis on a plane perpendicular to the curve to get new u-axis.
            float[] tangentUnitVector = new float[dimensionCount];
            for (int d = 0; d < dimensionCount; d++) {
                tangentUnitVector[d] = nextPosition[d] - previousPosition[d];
            }
            VectorUtil.normalizeVec3(tangentUnitVector);
            //this code assumes that two consecutive pieces of the curve never make a 90 degree angle.
            float[] vAxisUnitVector = VectorUtil.crossVec3(new float[dimensionCount], previousUAxisUnitVector, tangentUnitVector);
            VectorUtil.normalizeVec3(vAxisUnitVector);
            float[] uAxisUnitVector = VectorUtil.crossVec3(new float[dimensionCount], tangentUnitVector, vAxisUnitVector);
            VectorUtil.normalizeVec3(uAxisUnitVector);

            //rotate segment by bankingAngle about an axis parallel to tangentUnitVector in the direction from u-axis to v-axis.
            Matrix4 rotationMatrix = new Matrix4();
            rotationMatrix.rotate((float) Math.toRadians(bankingAnglesInDegrees[segmentIndex]), -tangentUnitVector[0], -tangentUnitVector[1], -tangentUnitVector[2]);
            float[] segmentDirectionUnitVector = MatrixUtils.multiply(rotationMatrix, new float[]{uAxisUnitVector[0], uAxisUnitVector[1], uAxisUnitVector[2], 0});

            //create segment.
            allVertices[segmentIndex] = createRainbowSegment(currentPosition,
                    new float[]{segmentDirectionUnitVector[0], segmentDirectionUnitVector[1], segmentDirectionUnitVector[2]}, width, vertexCountPerSegment);

            previousPosition = currentPosition;
            previousUAxisUnitVector = uAxisUnitVector;
        }

        return allVertices;
    }

    /**
     * Creates vertexCount vertices positioned equidistantly along a line piece with the given length.
     * The line piece is oriented along the direction of the given directionUnitVector and centered on the given centerCoordinates.
     */
    private static float[][] createRainbowSegment(float[] centerCoordinates, float[] directionUnitVector, float length, int vertexCount) {
        int dimensionCount = centerCoordinates.length;
        float[][] segmentVertexCoordinates = new float[vertexCount][dimensionCount];

        for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
            //parameter u ranges from -length/2 to length/2 (both inclusive).
            float u = length*vertexIndex/(vertexCount - 1f) - length/2;

            float[] vertexPosition = new float[dimensionCount];
            for (int d = 0; d < dimensionCount; d++) {
                segmentVertexCoordinates[vertexIndex][d] = centerCoordinates[d] + u*directionUnitVector[d];
            }
        }

        return segmentVertexCoordinates;
    }
}
