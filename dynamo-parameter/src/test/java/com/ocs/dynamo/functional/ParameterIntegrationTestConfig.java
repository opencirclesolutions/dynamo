package com.ocs.dynamo.functional;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;

import com.ocs.dynamo.IntegrationTestConfig;

/**
 * Configuration for integration tests in functional domain module
 * 
 * @author Bas Rutten
 *
 */
@TestConfiguration
@ComponentScan(basePackages = "com.ocs.dynamo")
@SpringBootApplication
public class ParameterIntegrationTestConfig extends IntegrationTestConfig {

}
