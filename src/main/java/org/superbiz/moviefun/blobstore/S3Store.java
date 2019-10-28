package org.superbiz.moviefun.blobstore;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class S3Store implements BlobStore {

    private AmazonS3Client s3client;
    private String photoStorageBucket;

    public S3Store(AmazonS3Client s3Client, String photoStorageBucket) {
        this.s3client = s3Client;
        this.photoStorageBucket = photoStorageBucket;
    }

    @Override
    public void put(Blob blob) throws IOException {
        ObjectMetadata omd = new ObjectMetadata();
        omd.setContentType(blob.contentType);
        s3client.putObject(photoStorageBucket, blob.name , blob.inputStream, omd);
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        S3Object s3object = s3client.getObject(photoStorageBucket, name);
        if (s3object == null){
            return Optional.empty();
        }

        InputStream inputStream = s3object.getObjectContent();
        String contentType = s3object.getObjectMetadata().getContentType();

        return Optional.of(new Blob(name, inputStream, contentType));
    }

    @Override
    public void deleteAll() {

    }
}
