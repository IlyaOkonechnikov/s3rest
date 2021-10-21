package com.jaxel.aws.s3rest.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.jaxel.aws.s3rest.exception.FileAlreadyExistsException;
import com.jaxel.aws.s3rest.exception.FileDoesNotExistException;
import com.jaxel.aws.s3rest.exception.FileUploadException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3FileStorageService implements FileStorageService {

  @Value("${cloud.aws.s3.url}")
  private String url;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  private final AmazonS3 amazonS3;

  public InputStreamResource downloadFile(String filename) {
    if (!existsInBucket(filename)) {
      throw new FileDoesNotExistException(filename, bucket);
    }
    S3Object s3object = amazonS3.getObject(bucket, filename);
    return new InputStreamResource(s3object.getObjectContent());
  }

  public String uploadFile(MultipartFile file) {
    String filename = file.getOriginalFilename();
    validateFilename(filename);
    if (existsInBucket(filename)) {
      throw new FileAlreadyExistsException(filename, bucket);
    }
    return putFileInBucket(file, filename);
  }

  public String updateFile(MultipartFile file) {
    String filename = file.getOriginalFilename();
    validateFilename(filename);
    if (!existsInBucket(filename)) {
      throw new FileDoesNotExistException(filename, bucket);
    }
    return putFileInBucket(file, filename);
  }

  public void deleteFile(String filename) {
    if (!existsInBucket(filename)) {
      throw new FileDoesNotExistException(filename, bucket);
    }
    amazonS3.deleteObject(new DeleteObjectRequest(bucket, filename));
  }

  private String putFileInBucket(MultipartFile file, String filename) {
    try {
      ObjectMetadata metadata = new ObjectMetadata();
      metadata.setContentLength(file.getSize());
      amazonS3.putObject(bucket, filename, file.getInputStream(), metadata);
      return String.format("%s/%s", url, filename);
    } catch (Exception e) {
      log.error("An error occurred while uploading the file.", e);
      throw new FileUploadException(e);
    }
  }

  private boolean existsInBucket(String filename) {
    return amazonS3.doesObjectExist(bucket, filename);
  }

  private void validateFilename(String filename) {
    if (filename == null) {
      throw new IllegalArgumentException("Original filename mustn't be null");
    }
  }
}