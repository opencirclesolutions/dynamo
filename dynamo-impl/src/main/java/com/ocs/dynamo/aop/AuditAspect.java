/*
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.ocs.dynamo.aop;

import com.ocs.dynamo.domain.AbstractAuditableEntity;
import com.ocs.dynamo.service.UserDetailsService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

/**
 * Aspect for intercepting calls and automatically setting audit information (created by/created on
 * etc)
 * 
 * Can be used in your application simply by adding "com.ocs.dynamo.aop" to the list of packages to
 * scan for components
 * 
 * @author bas.rutten
 *
 */
@Service("auditAspect")
@Aspect
public class AuditAspect {

    @Autowired
    private UserDetailsService userDetailsService;

    /**
     * Intercept all save methods on all classes that inherit from BaseService
     */
    @Pointcut("execution(public * com.ocs.dynamo.service.BaseService+.save(..))")
    public static void anySaveMethod() {
        // pointcut method for intercepting any save method
    }

    /**
     * intercept any method that saves an auditable entity
     * 
     * @param joinPoint
     *            the join point
     * @param entity
     *            the entity that is being saved
     * @return
     * @throws Throwable
     */
    @Around("anySaveMethod() && args(entity)")
    public Object auditSave(ProceedingJoinPoint joinPoint, AbstractAuditableEntity<?> entity)
            throws Throwable {
        setAuditFields(entity);
        return joinPoint.proceed();
    }

    /**
     * Fills in the audit fields (created by, created on etc)
     * 
     * @param entity
     */
    private void setAuditFields(AbstractAuditableEntity<?> entity) {
        String userName = userDetailsService.getCurrentUserName();
        if (entity.getId() == null) {
            entity.setCreatedBy(userName);
            entity.setCreatedOn(ZonedDateTime.now());
        }
        entity.setChangedBy(userName);
        entity.setChangedOn(ZonedDateTime.now());
    }
}
