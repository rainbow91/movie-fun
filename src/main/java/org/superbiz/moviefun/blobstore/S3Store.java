package org.superbiz.moviefun.blobstore;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;

import java.io.*;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;

public class S3Store implements BlobStore {
    private final Tika tika = new Tika();
    private final AmazonS3Client s3Client;
    private final String bucketName;

    public S3Store(AmazonS3Client s3Client, String bucketName) {
        this.s3Client = s3Client;
        this.bucketName= bucketName;
    }

    @Override
    public void put(Blob blob) throws IOException {
        if (!s3Client.doesObjectExist(bucketName, blob.name)) {
            try {
                s3Client.putObject(
                        bucketName, blob.name, blob.inputStream), new ObjectMetadata());
            } catch(AmazonServiceException e){
                System.err.println(e.getErrorMessage());
            }
        }
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        if (s3Client.doesObjectExist(bucketName, name)) {
            try (S3Object s3Object = s3Client.getObject(bucketName, name)) {
                S3ObjectInputStream s3ObjectInputStream = s3Object.getObjectContent();
                byte[] bytes = IOUtils.toByteArray(s3ObjectInputStream);
                return Optional.of(new Blob(
                        name,
                        bytes,
                        tika.detect(bytes)
                ));
            }
        }

        return Optional.empty();
    }

    @Override
    public void deleteAll() {
        try {
            DeleteObjectsRequest deleteRequest = new DeleteObjectsRequest(bucketName);
            s3Client.deleteObjects(deleteRequest);
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
        }
    }

    public static byte[] toByteArray(InputStream is)
            throws IOException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int reads = is.read();

        while(reads != -1){
            baos.write(reads);
            reads = is.read();
        }

        return baos.toByteArray();

    }
    public static byte[] toInputStream(byte[] content) {
        int size = content.length;
        InputStream is = null;
        byte[] b = new byte[size];
        try {
            is = new ByteArrayInputStream(content);
            is.read(b);
            System.out.println("Data Recovered: " + new String(b));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) is.close();
            } catch (Exception ex) {

            }
        }
    }
}