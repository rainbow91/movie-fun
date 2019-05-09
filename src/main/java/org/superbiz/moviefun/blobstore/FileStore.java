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

    private AmazonS3Client s3Client;
    private Bucket bucket = null;
    private String bucketName;
    private int index = -1;

    public FileStore(AmazonS3Client s3Client, String bucketName) {
        this.s3Client = s3Client;
        this.bucketName= bucketName;
 //       this.createBucket();

    }

    public void createBucket() {
//        final AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
         if (s3Client.doesBucketExist(bucketName)) {
            System.out.format("Bucket %s already exists.\n", bucketName);
             this.bucket = getBucket(bucketName);
        } else {
            try {
                bucket = s3Client.createBucket(bucketName);
            } catch (AmazonS3Exception e) {
                System.err.println(e.getErrorMessage());
            }
        }
    }

    public static Bucket getBucket(String bucket_name) {
        final AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
        Bucket named_bucket = null;
        List<Bucket> buckets = s3.listBuckets();
        for (Bucket b : buckets) {
            if (b.getName().equals(bucket_name)) {
                named_bucket = b;
            }
        }
        return named_bucket;
    }

    @Override
    public void put(Blob blob) throws IOException {
        if (!s3Client.doesObjectExist(blob.name, blob.contentType)) {
            byte[] imageBytes = IOUtils.toByteArray(blob.inputStream);
            File file = new File(blob.name);
            file.delete();
            file.getParentFile().mkdir();
            file.createNewFile();
            try (FileOutputStream fOutput = new FileOutputStream(file)){
                fOutput.write(imageBytes);
                PutObjectRequest request = new PutObjectRequest(blob.name, blob.contentType, file);
                s3Client.putObject(request);
            } catch(AmazonServiceException e){
                System.err.println(e.getErrorMessage());
            }
        }
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
//        S3ObjectInputStream s3is = o.getObjectContent();
//        FileOutputStream fos = new FileOutputStream(new File(name));
//        byte[] read_buf = new byte[1024];
//        int read_len = 0;
//        while ((read_len = s3is.read(read_buf)) > 0) {
//            fos.write(read_buf, 0, read_len);
//        }
//        s3is.close();
//        fos.close();

        GetObjectRequest request = new GetObjectRequest(name, IMAGE_JPEG_VALUE);
        S3Object s3Object = s3Client.getObject(request);
        if (s3Object != null) {
            S3ObjectInputStream s3is = s3Object.getObjectContent();
//            FileOutputStream fos = new FileOutputStream(new File(name));
//            byte[] read_buf = new byte[1024];
//            int read_len = 0;
//            while ((read_len = s3is.read(read_buf)) > 0) {
//                fos.write(read_buf, 0, read_len);
//            }
//            s3is.close();
//            fos.close();
//
//            ByteArrayInputStream bis = null;
//            ByteArrayOutputStream bos = new ByteArrayOutputStream();
//            ImageIO.write((InputStream)s3Object.getObjectContent()., "png", bos);
//            byte[] bImageData = bos.toByteArray();
//            bis = new ByteArrayInputStream(bImageData);
            Blob blob = new Blob(name, (InputStream) s3is, IMAGE_JPEG_VALUE);
            return Optional.of(blob);
        }
        return Optional.empty();
    }

    @Override
    public void deleteAll() {
        try {
            DeleteObjectsRequest dor = new DeleteObjectsRequest(bucketName);
            s3Client.deleteObjects(dor);
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
        }
    }
}
