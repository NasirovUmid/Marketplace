package com.pm.userservice.service;

import io.minio.*;
import io.minio.errors.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

//@AllArgsConstructor
@Service
public class FileService {

    private final MinioClient minioClient;
    private final Logger logger = LoggerFactory.getLogger(FileService.class);
    private final String bucket;

    public FileService(MinioClient minioClient, @Value("${minio.bucket}") String bucket) {
        this.minioClient = minioClient;
        this.bucket = bucket;
    }

    @Transactional
    public String uploadFileToMinio(UUID userId, MultipartFile multipartFile, String fileUrl) {

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(userId.toString() + "_" + fileUrl)
                            .stream(multipartFile.getInputStream(), multipartFile.getSize(), -1)
                            .contentType(multipartFile.getContentType())
                            .build()

            );
        } catch (ErrorResponseException e) {
            logger.error("error response minio= {}", e.toString());
            return null;
        } catch (InsufficientDataException e) {
            logger.error("error insufficient data mino= {}", e.toString());
            return null;
            //    throw new RuntimeException(e);
        } catch (InternalException e) {
            logger.error("error internal = {}", e.toString());
            return null;
            //    throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            logger.error("error invalid key = {}", e.toString());
            return null;
            //    throw new RuntimeException(e);
        } catch (InvalidResponseException e) {
            logger.error("error invalid response = {}", e.toString());
            return null;
            //    throw new RuntimeException(e);
        } catch (IOException e) {
            logger.error("error io minio = {}", e.toString());
            return null;
            //    throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            logger.error("error no such algorithm = {}", e.toString());
            return null;
            //    throw new RuntimeException(e);
        } catch (ServerException e) {
            logger.error("error server = {}", e.toString());
            return null;
            //     throw new RuntimeException(e);
        } catch (XmlParserException e) {
            logger.error("error xml parser = {}", e.toString());
            return null;
            //       throw new RuntimeException(e);
        }

        return "profileimages/" + userId + "_" + fileUrl;

    }

    public void deleteImageFromBucket(String objectKey) {

        try {

            minioClient.removeObject(RemoveObjectArgs
                    .builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .build());
        } catch (Exception e) {

            logger.error("Failed to delete Image = {}", objectKey, e);

        }

    }

    @PostConstruct
    public void init() {

        try {

            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucket)
                            .build());

            if (!exists) {

                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucket)
                                .build()
                );
            }

        } catch (Exception e) {

            throw new RuntimeException("Failed to init MiniO bucket", e);

        }

    }


}
