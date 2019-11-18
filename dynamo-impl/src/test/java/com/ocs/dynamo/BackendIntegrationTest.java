package com.ocs.dynamo;

import org.springframework.boot.test.context.SpringBootTest;

import com.ocs.dynamo.test.BaseIntegrationTest;

/**
 * Base class for backend integration tests (DAO)
 * 
 * @author Bas Rutten
 *
 */
@SpringBootTest(classes = IntegrationTestConfig.class)
public abstract class BackendIntegrationTest extends BaseIntegrationTest {

}
