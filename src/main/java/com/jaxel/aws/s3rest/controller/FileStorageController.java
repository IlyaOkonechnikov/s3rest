package com.jaxel.aws.s3rest.controller;

import com.jaxel.aws.s3rest.service.FileStorageService;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/file-storage")
public class FileStorageController {

  private final FileStorageService service;

  @GetMapping(value = "/{filename}")
  public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", filename))
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(service.downloadFile(filename));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public String upload(@RequestParam("file") MultipartFile multipartFile) {
    return service.uploadFile(multipartFile);
  }

  @DeleteMapping("/{url}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable String url) {
    service.deleteFile(url);
  }
}
