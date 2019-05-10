package org.superbiz.moviefun.blobstore;

public class Blob {
    public final String name;
    public final byte[] byteArray;
    public final String contentType;

    public Blob(String name, byte[] byteArray, String contentType) {
        this.name = name;
        this.byteArray = byteArray;
        this.contentType = contentType;
    }
}
