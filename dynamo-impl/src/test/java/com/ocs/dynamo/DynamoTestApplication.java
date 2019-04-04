package com.ocs.dynamo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(IntegrationTestConfig.class)
public class DynamoTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(DynamoTestApplication.class, args);
    }

}