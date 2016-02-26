/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package demo;

import javax.persistence.PostPersist;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.augment.UpdateContext.UpdateMode;
import org.springframework.security.acls.model.Permission;

/**
 * A JPA entity listener to make sure permissions are checked for JPA flushes outside a repository invocation. Also
 * registers default Permissions when objects are persisted.
 * 
 * @author Oliver Gierke
 */
public class AclCheckingEntityListener {

	static AclJpaHelper aclJpaHelper;

	@PrePersist
	@PreUpdate
	public void verifyAclBeforeModification(Object entity) {
		verifyAcl(entity, UpdateMode.SAVE);
	}

	@PreRemove
	public void verifyAclBeforeDelete(Object entity) {
		verifyAcl(entity, UpdateMode.DELETE);
	}

	private void verifyAcl(Object entity, UpdateMode mode) {

		try {

			MethodInvocation invocation = ExposeInvocationInterceptor.currentInvocation();

			// In case the call is issued by a call to a repository the verification has already taken place
			if (Repository.class.isInstance(invocation.getThis())) {
				return;
			}

		} catch (IllegalStateException e) {}

		aclJpaHelper.verifyAcl(entity, mode);
	}

	/**
	 * Creates default {@link Permission} instances when an entity is created.
	 *
	 * @param entity
	 */
	@PostPersist
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void createDefaultAcl(Object entity) {

		aclJpaHelper.createDefaultAcl(entity);
	}
}
