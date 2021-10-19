package com.jaxel.aws.s3rest.service;

import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileStorageService {

  List<String> listFileVersions(String filename);

  InputStreamResource downloadFile(String filename, String version);

  String uploadFile(MultipartFile file);

  String updateFile(MultipartFile file);

  void deleteFile(String filename);
}
