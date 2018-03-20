/*
 * Copyright (c) 2018 A.C. Kockx, All Rights Reserved.
 */
package rasterizer;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Class for loading resources.
 *
 * @author A.C. Kockx
 */
public final class ResourceLoader {
    private final String resourcePath;

    /**
     * @param resourcePath of the package that contains the resource(s) to load, relative to the root package.
     *        This must contain a preceding '/' and a trailing '/' and must use '/' as separator.
     *        Example: /rasterizer/shaders/
     */
    public ResourceLoader(String resourcePath) {
        if (resourcePath == null) throw new IllegalArgumentException("resourcePath == null");

        //resources in Java always use '/', this has nothing to do with system dependent file separators.
        if (!resourcePath.startsWith("/")) throw new IllegalArgumentException("resourcePath " + resourcePath + " must start with a '/'");
        if (!resourcePath.endsWith("/")) throw new IllegalArgumentException("resourcePath " + resourcePath + " must end with a '/'");

        this.resourcePath = resourcePath;
    }

    /**
     * @param resourceName the name of the resource to load.
     * @return InputStream of the resource.
     */
    public InputStream loadResource(String resourceName) throws FileNotFoundException {
        if (resourceName == null) throw new IllegalArgumentException("resourceName == null");

        String resourcePathName = resourcePath + resourceName;
        InputStream inputStream = getClass().getResourceAsStream(resourcePathName);
        if (inputStream == null) throw new FileNotFoundException("Cannot find resource " + resourcePathName);
        return inputStream;
    }
}
