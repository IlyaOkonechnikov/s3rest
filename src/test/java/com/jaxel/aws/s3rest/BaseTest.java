package com.jaxel.aws.s3rest;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.jaxel.aws.s3rest.service.S3FileStorageService;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import lombok.SneakyThrows;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@Testcontainers
@SpringBootTest(classes = BaseTest.TestConfig.class)
@ContextConfiguration(initializers = BaseTest.ContextInitializer.class)
public abstract class BaseTest {

  private static final String REGION = "eu-central-1";
  private static final String BUCKET_NAME = "testjaxelbucket";

  @Container
  private static final LocalStackContainer CONTAINER =
      new LocalStackContainer(DockerImageName.parse("localstack/localstack:0.10.0"))
          .withServices(S3)
          .withEnv("DEFAULT_REGION", REGION);

  public static class ContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    @SneakyThrows
    public void initialize(ConfigurableApplicationContext ctx) {
      CONTAINER.execInContainer("awslocal", "s3", "mb", "s3://" + BUCKET_NAME);
    }
  }

  @Configuration
  @Import(S3FileStorageService.class)
  static class TestConfig {

    @Bean
    public AmazonS3 amazonS3() {
      return AmazonS3ClientBuilder
          .standard()
          .withEndpointConfiguration(CONTAINER.getEndpointConfiguration(S3))
          .withCredentials(CONTAINER.getDefaultCredentialsProvider())
          .build();
    }
  }
}
