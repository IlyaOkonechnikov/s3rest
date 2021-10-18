package com.jaxel.aws.s3rest.exception;

public class FileAlreadyExistsException extends RuntimeException {

  public FileAlreadyExistsException(String filename, String bucket) {
    super(String.format("A file with name %s already exists in the bucket %s", filename, bucket));
  }
}
