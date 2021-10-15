package com.jaxel.aws.s3rest.controller;

import com.jaxel.aws.s3rest.service.FileStorageService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/file-storage")
public class FileStorageController {

  private final FileStorageService service;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public String upload(@RequestBody MultipartFile multipartFile) {
    return service.uploadFile(multipartFile);
  }

  @DeleteMapping("/{url}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteBook(@PathVariable String url) {
    service.deleteFile(url);
  }
}
