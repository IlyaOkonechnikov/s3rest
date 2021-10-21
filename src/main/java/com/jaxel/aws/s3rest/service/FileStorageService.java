package com.jaxel.aws.s3rest.service;

import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {


  InputStreamResource downloadFile(String filename);

  String uploadFile(MultipartFile file);

  String updateFile(MultipartFile file);

  void deleteFile(String filename);
}
