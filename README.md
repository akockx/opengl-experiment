OpenGL Experiment
=================

An experiment to learn how to use OpenGL 3 in Java.

Author: A.C. Kockx



Requirements
------------

Java SDK 8 (or higher) and Gradle need to be installed on your system. The code depends on the Java OpenGL (JOGL) third party library (not included). The dependencies are defined in the build.gradle file and will be downloaded automatically by Gradle when needed. In order to run, your system must have a GPU that supports OpenGL 3.



Build
-----

In order to build and run the MultiColoredCube experiment, clone the repository and run the following command on the command line in the folder that contains the file "settings.gradle":

```
gradle run
```



Experiments
-----------

* MultiColoredCube: Uses OpenGL 3 to draw a multi-colored cube on the screen. The cube is lit by a single point light and is shaded using an implementation of Phong shading.
* GpuMandelbrot: Draws the Mandelbrot set on the screen using OpenGL 3. All calculations are performed by the GPU using a fragment shader.
* Rainbow: Uses OpenGL 3 to draw a rainbow that follows a curve through 3D space.
* HelloTriangle: This class uses a minimal amount of code to draw a single triangle on the screen using OpenGL 3. Comments have been added to explain every step.
