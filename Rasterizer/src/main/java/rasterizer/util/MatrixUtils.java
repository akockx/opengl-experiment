/*
 * Copyright (c) 2018 A.C. Kockx, All Rights Reserved.
 */
package rasterizer.util;

import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.math.Matrix4;
import com.jogamp.opengl.math.Quaternion;

import java.util.Arrays;

/**
 * @author A.C. Kockx
 */
public final class MatrixUtils {
    private MatrixUtils() {
    }

    /**
     * Creates and returns a model matrix in column-major order, using the given position, orientation and scale of the model.
     *
     * The rotations are applied in the order:
     * 1. yaw about y-axis.
     * 2. roll about z'-axis.
     * 3. pitch about x''-axis.
     */
    public static Matrix4 createModelMatrix(float x, float y, float z, float yawInDegrees, float rollInDegrees, float pitchInDegrees, float xScale, float yScale, float zScale) {
        //create scaling matrix.
        Matrix4 scalingMatrix = new Matrix4();
        scalingMatrix.scale(xScale, yScale, zScale);

        //create rotation.
        Quaternion rotation = new Quaternion();
        rotation.setFromEuler((float) Math.toRadians(pitchInDegrees), (float) Math.toRadians(yawInDegrees), (float) Math.toRadians(rollInDegrees));

        //create translation matrix.
        Matrix4 translationMatrix = new Matrix4();
        translationMatrix.translate(x, y, z);

        Matrix4 modelMatrix = new Matrix4();
        //scale.
        modelMatrix.multMatrix(scalingMatrix);
        //rotate.
        modelMatrix.rotate(rotation);
        //translate.
        modelMatrix.multMatrix(translationMatrix);
        return modelMatrix;
    }

    /**
     * Creates and returns a view matrix in column-major order, using the given position and orientation of the camera.
     *
     * The rotations are applied in the order:
     * 1. yaw about y-axis.
     * 2. roll about z'-axis.
     * 3. pitch about x''-axis.
     */
    public static Matrix4 createViewMatrix(float x, float y, float z, float yawInDegrees, float rollInDegrees, float pitchInDegrees) {
        //create translation matrix.
        Matrix4 translationMatrix = new Matrix4();
        translationMatrix.translate(-x, -y, -z);

        //create rotation.
        Quaternion rotation = new Quaternion();
        rotation.setFromEuler((float) Math.toRadians(-pitchInDegrees), (float) Math.toRadians(-yawInDegrees), (float) Math.toRadians(-rollInDegrees));

        Matrix4 modelMatrix = new Matrix4();
        //translate.
        modelMatrix.multMatrix(translationMatrix);
        //rotate.
        modelMatrix.rotate(rotation);
        //Note: the scaling that would be expected here (analogous to the model matrix) is implicitly contained in the projection matrix.
        return modelMatrix;
    }

    /**
     * The camera looks in the direction of the negative z-axis in camera space.
     * The image plane of the camera is at z = -zNear in camera space.
     *
     * @param cameraHeight in camera space.
     * @param aspectRatio = cameraWidth/cameraHeight.
     * @param zNear the negative value of the z coordinate of the near clipping plane in camera space.
     * @param zFar the negative value of the z coordinate of the far clipping plane in camera space.
     */
    public static Matrix4 createOrthographicProjectionMatrix(float cameraHeight, float aspectRatio, float zNear, float zFar) {
        if (cameraHeight <= 0) throw new IllegalArgumentException("cameraHeight <= 0");
        if (aspectRatio <= 0) throw new IllegalArgumentException("aspectRatio <= 0");
        if (zFar <= zNear) throw new IllegalArgumentException("zFar <= zNear");

        //xLeft, xRight, yBottom and yTop are the coordinates of the edges of the camera in camera space.
        float cameraWidth = aspectRatio*cameraHeight;
        float xLeft = -cameraWidth/2;
        float xRight = cameraWidth/2;
        float yBottom = -cameraHeight/2;
        float yTop = cameraHeight/2;

        Matrix4 projectionMatrix = new Matrix4();
        projectionMatrix.makeOrtho(xLeft, xRight, yBottom, yTop, zNear, zFar);
        return projectionMatrix;
    }

    /**
     * The camera looks in the direction of the negative z-axis in camera space.
     * The center of projection is at the origin in camera space.
     * The image plane of the camera is at z = -zNear in camera space.
     *
     * Here the perspective projection matrix is created by combining two transformations in this order:
     * 1. an orthographic projection, which transforms from camera space to clipping space.
     * 2. preparation for perspective division (takes place in clipping space).
     * The standard approach seems to do this the other way around, in which case preparation for perspective division
     * takes place in camera space. However this approach gives the same results.
     *
     * @param fieldOfViewInDegrees in the y direction in camera space.
     * @param aspectRatio = cameraWidth/cameraHeight.
     * @param zNear the negative value of the z coordinate of the near clipping plane in camera space.
     * @param zFar the negative value of the z coordinate of the far clipping plane in camera space.
     */
    public static Matrix4 createPerspectiveProjectionMatrix(float fieldOfViewInDegrees, float aspectRatio, float zNear, float zFar) {
        if (fieldOfViewInDegrees <= 0) throw new IllegalArgumentException("fieldOfViewInDegrees <= 0");
        if (fieldOfViewInDegrees >= 180) throw new IllegalArgumentException("fieldOfViewInDegrees >= 180");
        if (aspectRatio <= 0) throw new IllegalArgumentException("aspectRatio <= 0");
        if (zNear <= 0) throw new IllegalArgumentException("zNear <= 0");
        if (zFar <= zNear) throw new IllegalArgumentException("zFar <= zNear");

        //create matrix to transform from camera space to clipping space.
        float cameraHeight = 2*zNear*(float) Math.tan(Math.toRadians(fieldOfViewInDegrees/2));
        Matrix4 orthographicProjectionMatrix = MatrixUtils.createOrthographicProjectionMatrix(cameraHeight, aspectRatio, zNear, zFar);

        //the center of projection is at the origin in camera space.
        float[] centerOfProjection = new float[]{0, 0, 0, 1};
        //transform center of projection to clipping space.
        float[] temp = new float[4];
        orthographicProjectionMatrix.multVec(centerOfProjection, temp);
        centerOfProjection = temp;
        float zCenterOfProjection = centerOfProjection[2];
        //image plane is z = -1 in clipping space.
        float zImagePlane = -1;

        //create matrix that takes care of preparation for perspective division.
        //OpenGL will divide all x, y, and z coordinates by their corresponding w values. This division is done by OpenGL in a later stage.
        //Here only need to create a matrix that calculates the w values that will be used later by OpenGL.
        float[] matrix = new float[16];//in column-major order.
        Arrays.fill(matrix, 0);
        matrix[0] = 1;
        matrix[5] = 1;
        matrix[10] = 1;
        //using ratio of similar triangles in clipping space, we get:
        //xImage/(zImagePlane - zCenterOfProjection) = x/(z - zCenterOfProjection)
        //=> xImage = x * (zImagePlane - zCenterOfProjection)/(z - zCenterOfProjection) = x / w
        //=> w = (z - zCenterOfProjection)/(zImagePlane - zCenterOfProjection) = z/(zImagePlane - zCenterOfProjection) - zCenterOfProjection/(zImagePlane - zCenterOfProjection)
        matrix[11] = 1/(zImagePlane - zCenterOfProjection);
        matrix[15] = -zCenterOfProjection/(zImagePlane - zCenterOfProjection);
        Matrix4 preparationMatrix = new Matrix4();
        preparationMatrix.multMatrix(matrix);

        return multiply(preparationMatrix, orthographicProjectionMatrix);
    }

    /**
     * Matrix multiplication.
     *
     * This method is needed because the method com.jogamp.opengl.math.Matrix4.multMatrix() puts the result of the multiplication into its first input matrix.
     * Therefore the method com.jogamp.opengl.math.Matrix4.multMatrix() changes its input, which is something we don't want.
     * This method does not change the input.
     *
     * @param a matrix A in column-major order.
     * @param b matrix B in column-major order.
     * @return A x B in column-major order.
     */
    public static Matrix4 multiply(Matrix4 a, Matrix4 b) {
        Matrix4 result = new Matrix4();
        FloatUtil.multMatrix(a.getMatrix(), b.getMatrix(), result.getMatrix());
        return result;
    }
}
