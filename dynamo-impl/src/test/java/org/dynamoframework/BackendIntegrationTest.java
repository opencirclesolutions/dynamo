package org.dynamoframework;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.Getter;
import org.dynamoframework.configuration.DynamoConfigurationProperties;
import org.dynamoframework.configuration.DynamoPropertiesHolder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class for backend integration tests (DAO)
 * 
 * @author Bas Rutten
 *
 */
@SpringBootTest
@Transactional
@Import({DynamoPropertiesHolder.class})
@EnableConfigurationProperties(value = DynamoConfigurationProperties.class)
public abstract class BackendIntegrationTest  {

    @PersistenceContext
    @Getter
    protected EntityManager entityManager;
}
