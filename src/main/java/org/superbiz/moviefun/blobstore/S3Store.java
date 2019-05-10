package org.superbiz.moviefun.blobstore;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.apache.tika.Tika;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

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
                ObjectMetadata objectMetadata = new ObjectMetadata();
                objectMetadata.setContentType(tika.detect(blob.byteArray));
                s3Client.putObject(bucketName, blob.name, new ByteArrayInputStream(blob.byteArray), objectMetadata);
            } catch(AmazonServiceException e){
                System.err.println(e.getErrorMessage());
            }
        }
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        if (s3Client.doesObjectExist(bucketName, name)) {
            try (S3Object s3Object = s3Client.getObject(bucketName, name);
                 ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();) {

                S3ObjectInputStream content = s3Object.getObjectContent();
                int reads = content.read();
                while(reads != -1){
                    byteArrayOutputStream.write(reads);
                    reads = content.read();
                }
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                return Optional.of(new Blob(
                        name,
                        byteArray,
                        tika.detect(byteArray)
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
}