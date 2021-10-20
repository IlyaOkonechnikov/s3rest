package com.jaxel.aws.s3rest.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3VersionSummary;
import com.jaxel.aws.s3rest.exception.FileAlreadyExistsException;
import com.jaxel.aws.s3rest.exception.FileDoesNotExistException;
import com.jaxel.aws.s3rest.exception.FileUploadException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

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

  public List<String> listFileVersions(String filename) {
    return amazonS3.listVersions(bucket, filename).getVersionSummaries()
        .stream()
        .map(S3VersionSummary::getVersionId)
        .collect(Collectors.toList());
  }

  public InputStreamResource downloadFile(String filename, String version) {
    S3Object s3object = amazonS3.getObject(new GetObjectRequest(bucket, filename, version));
    return new InputStreamResource(s3object.getObjectContent());
  }

  public String uploadFile(MultipartFile file) {
    String filename = file.getOriginalFilename();
    validateFilename(filename);
    validateFileExistence(filename);
    return putFileInBucket(file, filename);
  }

  public String updateFile(MultipartFile file) {
    String filename = file.getOriginalFilename();
    validateFilename(filename);
    if (!amazonS3.doesObjectExist(bucket, filename)) {
      throw new FileDoesNotExistException(filename, bucket);
    }
    return putFileInBucket(file, filename);
  }

  public void deleteFile(String filename) {
    validateFileExistence(filename);
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

  private void validateFileExistence(String filename) {
    if (amazonS3.doesObjectExist(bucket, filename)) {
      throw new FileAlreadyExistsException(filename, bucket);
    }
  }

  private void validateFilename(String filename) {
    if (filename == null) {
      throw new IllegalArgumentException("Original filename mustn't be null");
    }
  }
}