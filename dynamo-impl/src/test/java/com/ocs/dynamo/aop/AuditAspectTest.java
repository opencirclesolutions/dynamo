package com.ocs.dynamo.aop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.ocs.dynamo.domain.AbstractAuditableEntity;
import com.ocs.dynamo.service.UserDetailsService;

@ExtendWith(SpringExtension.class)
public class AuditAspectTest {

    @InjectMocks
    private AuditAspect aspect = new AuditAspect();

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @BeforeEach
    public void setUp() {
        when(userDetailsService.getCurrentUserName()).thenReturn("Kevin");
    }

    @Test
    public void testSaveNew() throws Throwable {
        MyAuditableEntity entity = new MyAuditableEntity();
        aspect.auditSave(joinPoint, entity);

        assertEquals("Kevin", entity.getCreatedBy());
        assertEquals("Kevin", entity.getChangedBy());
        assertNotNull(entity.getCreatedBy());
        assertNotNull(entity.getChangedOn());
    }

    @Test
    public void testSaveExisting() throws Throwable {
        MyAuditableEntity entity = new MyAuditableEntity();
        entity.setId(33);
        entity.setCreatedBy("Stuart");
        entity.setChangedBy("Stuart");

        aspect.auditSave(joinPoint, entity);

        assertEquals("Stuart", entity.getCreatedBy());
        assertEquals("Kevin", entity.getChangedBy());
        assertNotNull(entity.getCreatedBy());
        assertNotNull(entity.getChangedOn());
    }

    private class MyAuditableEntity extends AbstractAuditableEntity<Integer> {

        private static final long serialVersionUID = 5076818539497538764L;

        private Integer id;

        @Override
        public Integer getId() {
            return id;
        }

        @Override
        public void setId(Integer id) {
            this.id = id;
        }

    }
}
