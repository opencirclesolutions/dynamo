package com.ocs.dynamo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.Getter;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class for backend integration tests (DAO)
 * 
 * @author Bas Rutten
 *
 */
@SpringBootTest
@Transactional
public abstract class BackendIntegrationTest  {

    @PersistenceContext
    @Getter
    protected EntityManager entityManager;
}
