package com.ocs.dynamo.ui;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.ocs.dynamo.test.BaseIntegrationTest;

/**
 * Abstract base classes for front-end integration tests
 * 
 * @author Bas Rutten
 *
 */
@SpringBootTest(classes = FrontendIntegrationTestConfig.class)
@TestPropertySource(value = "classpath:application-it.properties")
public abstract class FrontendIntegrationTest extends BaseIntegrationTest {

}
