package com.jaxel.aws.s3rest.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.jaxel.aws.s3rest.exception.FileAlreadyExistsException;
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
public class FileStorageService {

  @Value("${cloud.aws.s3.url}")
  private String url;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  private final AmazonS3 amazonS3;

  public InputStreamResource downloadFile(String filename) {
    S3Object s3object = amazonS3.getObject(new GetObjectRequest(bucket, filename));
    return new InputStreamResource(s3object.getObjectContent());
  }

  public String uploadFile(MultipartFile file) {
    String filename = file.getOriginalFilename();
    if (filename == null) {
      throw new IllegalArgumentException("Original filename mustn't be null");
    }
    if (amazonS3.doesObjectExist(bucket, filename)) {
      throw new FileAlreadyExistsException(filename, bucket);
    }
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

  public void deleteFile(String url) {
    String fileName = url.substring(url.lastIndexOf("/") + 1);
    amazonS3.deleteObject(new DeleteObjectRequest(bucket, fileName));
  }
}