package com.example.demo.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@CommonsLog
@Component
public class S3Uploader {
    @Value("${s3.access.key}")
    private String accessKey;

    @Value("${s3.secret.key}")
    private String secretKey;

    @Value("${s3.bucket.name}")
    private String bucketName;

    private static File convertMultipartToFile(MultipartFile multipartFile) throws IOException {
        File file = new File(multipartFile.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(multipartFile.getBytes());
        fos.close();
        return file;
    }

    private AmazonS3 getS3Client() {
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

        // Create an S3 client
        return AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.US_EAST_1)
                .build();
    }

    public void uploadFile(String saveKeyName, File file) {
        AmazonS3 s3 = getS3Client();
        try {
            s3.putObject(bucketName, saveKeyName, file);
        } catch (AmazonServiceException e) {
            log.error("upload " + saveKeyName + " failed: " + e.getMessage());
        }
    }


    public void saveMultipartFileToFile(MultipartFile file, String filePath) throws IOException {
        File convertedFile = convertMultipartToFile(file);
        uploadFile(filePath, convertedFile);

        // Delete file which will be uploaded to EC2
        convertedFile.delete();
    }
}
