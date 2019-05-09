package org.superbiz.moviefun.blobstore;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;

import javax.imageio.ImageIO;
import java.io.*;
import java.util.List;
import java.util.Optional;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;

public class FileStore implements BlobStore {
    private final Tika tika = new Tika();

    @Override
    public void put(Blob blob) throws IOException {
        File file = new File(blob.name);
        file.delete();
        file.getParentFile().mkdirs();
        file.createNewFile();

        try (FileOutputStream fileOutputStream = new FileOutputStream(file)){
            IOUtils.copy(blob.inputStream,fileOutputStream);
        }
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        File file = new File(name);
        if (file.exists()) {
            return Optional.of(new Blob(
                    name,
                    new FileInputStream(file),
                    tika.detect(file)
            ));
        }

        return Optional.empty();
    }

    @Override
    public void deleteAll() { }
}

