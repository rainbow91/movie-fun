package org.superbiz.moviefun.blobstore;

import org.apache.tika.Tika;

import java.io.*;
import java.util.Optional;

public class FileStore implements BlobStore {
    private final Tika tika = new Tika();

    @Override
    public void put(Blob blob) throws IOException {
        File targetFile = new File(blob.name);
        targetFile.delete();
        targetFile.getParentFile().mkdirs();
        targetFile.createNewFile();

        try (OutputStream outputStream = new FileOutputStream(targetFile)) {
            outputStream.write(blob.byteArray);
        }
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        File file = new File(name);
        if (!file.exists()) {
            return Optional.empty();
        }

        byte[] byteArray = new byte[(int) file.length()];
        try( InputStream fis = new FileInputStream(file);){
            fis.read(byteArray);
        }

        return Optional.of(new Blob(
                name,
                byteArray,
                tika.detect(file)
        ));
    }

    @Override
    public void deleteAll() { }
}