package com.jaxel.aws.s3rest.service;

import com.amazonaws.services.s3.AmazonS3;
import com.jaxel.aws.s3rest.BaseTest;
import com.jaxel.aws.s3rest.exception.FileAlreadyExistsException;
import com.jaxel.aws.s3rest.exception.FileDoesNotExistException;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.ResourceUtils;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileStorageServiceTest extends BaseTest {

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  @Autowired
  private S3FileStorageService service;

  @Autowired
  private AmazonS3 amazonS3;

  @Test
  void whenDownloadFile_thenFileShouldBeEqualToUploadedOne() throws IOException {
    File file = ResourceUtils.getFile("classpath:files/1.jpg");
    service.uploadFile(getMultipartFile(file, file.getName()));
    try (InputStream stream = service.downloadFile(file.getName()).getInputStream()) {
      File downloadedFile = Files.createTempFile("downloadedFile", ".jpg").toFile();
      FileUtils.copyInputStreamToFile(stream, downloadedFile);
      assertTrue(compareImageFiles(file, downloadedFile));
    }
  }

  @Test
  void whenUploadFile_thenFileShouldBeInBucket() throws IOException {
    String filename = "2.jpg";
    File file = ResourceUtils.getFile("classpath:files/" + filename);
    service.uploadFile(getMultipartFile(file, file.getName()));
    assertTrue(amazonS3.doesObjectExist(bucket, filename));
  }

  @Test
  void whenUploadExistingFile_thenExceptionShouldBeThrown() throws IOException {
    File file = ResourceUtils.getFile("classpath:files/3.jpg");
    service.uploadFile(getMultipartFile(file, file.getName()));
    assertThrows(FileAlreadyExistsException.class, () -> service.uploadFile(getMultipartFile(file, file.getName())));
  }

  @Test
  void whenUpdateFile_thenFileShouldBeChanged() throws IOException {
    File initialFile = ResourceUtils.getFile("classpath:files/4.jpg");
    service.uploadFile(getMultipartFile(initialFile, initialFile.getName()));

    File replacementFile = ResourceUtils.getFile("classpath:files/5.jpg");
    service.updateFile(getMultipartFile(replacementFile, initialFile.getName()));

    try (InputStream stream = service.downloadFile(initialFile.getName()).getInputStream()) {
      File downloadedFile = Files.createTempFile("downloadedFile", ".jpg").toFile();
      FileUtils.copyInputStreamToFile(stream, downloadedFile);
      assertFalse(compareImageFiles(initialFile, downloadedFile));
    }
  }

  @Test
  void whenDeleteFile_thenFileShouldNotBeInBucket() throws IOException {
    String filename = "6.jpg";
    File file = ResourceUtils.getFile("classpath:files/" + filename);
    service.uploadFile(getMultipartFile(file, file.getName()));
    service.deleteFile(filename);
    assertFalse(amazonS3.doesObjectExist(bucket, filename));
  }

  @Test
  void whenNonExistentFile_thenExceptionShouldBeThrown() throws IOException {
    String filename = "7.jpg";
    assertThrows(FileDoesNotExistException.class, () -> service.downloadFile(filename));

    File file = ResourceUtils.getFile("classpath:files/" + filename);
    MockMultipartFile multipartFile = getMultipartFile(file, file.getName());
    assertThrows(FileDoesNotExistException.class, () -> service.updateFile(multipartFile));

    assertThrows(FileDoesNotExistException.class, () -> service.deleteFile(filename));
  }

  private MockMultipartFile getMultipartFile(File file, String filename) throws IOException {
    try (InputStream stream = new FileInputStream(file)) {
      return new MockMultipartFile(filename, filename, MediaType.IMAGE_JPEG_VALUE, stream);
    }
  }

  public static boolean compareImageFiles(File firstFile, File secondFile) {
    try {
      BufferedImage firstBufferedImage = ImageIO.read(firstFile);
      DataBuffer firstDataBuffer = firstBufferedImage.getData().getDataBuffer();
      int firstSize = firstDataBuffer.getSize();
      BufferedImage secondBufferedImage = ImageIO.read(secondFile);
      DataBuffer secondDataBuffer = secondBufferedImage.getData().getDataBuffer();
      int secondSize = secondDataBuffer.getSize();
      if (firstSize == secondSize) {
        for (int i = 0; i < firstSize; i++) {
          if (firstDataBuffer.getElem(i) != secondDataBuffer.getElem(i)) {
            return false;
          }
        }
        return true;
      } else {
        return false;
      }
    } catch (Exception e) {
      System.out.println("Failed to compare image files...");
      return false;
    }
  }
}
