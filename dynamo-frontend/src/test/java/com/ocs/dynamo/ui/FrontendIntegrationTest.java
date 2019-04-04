package com.ocs.dynamo.ui;

import org.springframework.boot.test.context.SpringBootTest;

import com.ocs.dynamo.test.BaseIntegrationTest;

/**
 * Abstract base classes for front-end integration tests
 * 
 * @author Bas Rutten
 *
 */
@SpringBootTest(classes = FrontendIntegrationTestConfig.class)
public abstract class FrontendIntegrationTest extends BaseIntegrationTest {

}
