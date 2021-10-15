package com.jaxel.aws.s3rest.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.jaxel.aws.s3rest.exception.FileUploadException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("ConstantConditions")
public class FileStorageService {

  @Value("${cloud.aws.s3.url}")
  private String url;

  @Value("${cloud.aws.s3.bucket-name}")
  private String bucketName;

  private final AmazonS3 amazonS3;

  public String uploadFile(MultipartFile multipartFile) {
    if (multipartFile.getOriginalFilename() == null) {
      throw new IllegalArgumentException("Original filename mustn't be null");
    }
    return uploadMultipartFile(multipartFile);
  }

  public void deleteFile(String url) {
    String fileName = url.substring(url.lastIndexOf("/") + 1);
    amazonS3.deleteObject(new DeleteObjectRequest(bucketName, fileName));
  }

  private String uploadMultipartFile(MultipartFile multipartFile) {
    String fileUrl;
    try {
      File file = convertMultipartToFile(multipartFile);
      String fileName = multipartFile.getOriginalFilename();
      amazonS3.putObject(new PutObjectRequest(bucketName, fileName, file)
          .withCannedAcl(CannedAccessControlList.PublicRead));
      Files.delete(file.toPath());
      fileUrl = url.concat(fileName);
    } catch (IOException e) {
      throw new FileUploadException(e);
    }
    return fileUrl;
  }

  private File convertMultipartToFile(MultipartFile multipartFile) throws IOException {
    File file = new File(multipartFile.getOriginalFilename());
    try (FileOutputStream stream = new FileOutputStream(file)) {
      stream.write(multipartFile.getBytes());
    }
    return file;
  }
}