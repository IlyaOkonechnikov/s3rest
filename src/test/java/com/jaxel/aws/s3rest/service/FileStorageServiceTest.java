package com.jaxel.aws.s3rest.service;

import com.amazonaws.services.s3.AmazonS3;
import com.jaxel.aws.s3rest.BaseTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

class FileStorageServiceTest extends BaseTest {

  @Autowired
  private S3FileStorageService service;

  @Autowired
  private AmazonS3 amazonS3;

  @Test
  void whenFileIsUpdated_thenAllVersionsShouldBeFound() throws IOException {
    File file = ResourceUtils.getFile("classpath:files/1.jpg");
    InputStream stream =  new FileInputStream(file);
    MockMultipartFile multipartFile = new MockMultipartFile("file", file.getName(), MediaType.IMAGE_JPEG_VALUE, stream);
    System.out.println();
//    long total = repository.count();
//    SearchResponse response = service.search(SearchRequest.of(Collections.emptyList(), 0, (int) total));
//    assertThat(total).isEqualTo(response.getBooks().size());
  }
}
