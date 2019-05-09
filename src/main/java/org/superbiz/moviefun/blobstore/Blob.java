package org.superbiz.moviefun.blobstore;

import java.io.InputStream;

public class Blob {
    public final String name;
    public final byte[] inputByteArray;
    public final String contentType;

    public Blob(String name, byte[] inputByteArray, String contentType) {
        this.name = name;
        this.inputByteArray = inputByteArray;
        this.contentType = contentType;
    }
}
