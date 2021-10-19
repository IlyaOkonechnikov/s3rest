package com.jaxel.aws.s3rest.exception;

public class FileDoesNotExistException extends RuntimeException {

  public FileDoesNotExistException(String filename, String bucket) {
    super(String.format("A file with name %s doesn't exist in the bucket %s", filename, bucket));
  }
}
