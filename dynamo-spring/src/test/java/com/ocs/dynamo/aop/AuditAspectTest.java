package com.ocs.dynamo.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.ocs.dynamo.domain.AbstractAuditableEntity;
import com.ocs.dynamo.service.UserDetailsService;
import com.ocs.dynamo.test.BaseMockitoTest;

public class AuditAspectTest extends BaseMockitoTest {

    private AuditAspect aspect = new AuditAspect();

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        Mockito.when(userDetailsService.getCurrentUserName()).thenReturn("Kevin");

        wireTestSubject(aspect);
    }

    @Test
    public void testSaveNew() throws Throwable {
        MyAuditableEntity entity = new MyAuditableEntity();
        aspect.auditSave(joinPoint, entity);

        Assert.assertEquals("Kevin", entity.getCreatedBy());
        Assert.assertEquals("Kevin", entity.getChangedBy());
        Assert.assertNotNull(entity.getCreatedBy());
        Assert.assertNotNull(entity.getChangedOn());
    }

    @Test
    public void testSaveExisting() throws Throwable {
        MyAuditableEntity entity = new MyAuditableEntity();
        entity.setId(33);
        entity.setCreatedBy("Stuart");
        entity.setChangedBy("Stuart");

        aspect.auditSave(joinPoint, entity);

        Assert.assertEquals("Stuart", entity.getCreatedBy());
        Assert.assertEquals("Kevin", entity.getChangedBy());
        Assert.assertNotNull(entity.getCreatedBy());
        Assert.assertNotNull(entity.getChangedOn());
    }

    private class MyAuditableEntity extends AbstractAuditableEntity<Integer> {

        private static final long serialVersionUID = 89014990402090601L;

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
