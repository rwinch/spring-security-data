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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;

import javax.persistence.EntityManager;
import javax.persistence.PostPersist;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.data.jpa.repository.support.JpaUpdateContext;
import org.springframework.data.jpa.repository.support.QueryExecutor;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.augment.QueryAugmentationEngine;
import org.springframework.data.repository.augment.UpdateContext.UpdateMode;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * A JPA entity listener to make sure permissions are checked for JPA flushes outside a repository invocation. Also
 * registers default Permissions when objects are persisted.
 * 
 * @author Oliver Gierke
 */
public class AclCheckingEntityListener {

	// TODO: Use proper dependency injection
	private AclQueryAugmentor<Object> augmentor = new AclQueryAugmentor<Object>();

	// TODO: Use proper dependency injection
	public static JpaContext context;

	@PrePersist
	@PreUpdate
	public void verifyAclBeforeModification(Object entity) {
		verifyAcl(entity, UpdateMode.SAVE);
	}

	@PreRemove
	public void verifyAclBeforeDelete(Object entity) {
		verifyAcl(entity, UpdateMode.DELETE);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void verifyAcl(Object entity, UpdateMode mode) {

		try {

			MethodInvocation invocation = ExposeInvocationInterceptor.currentInvocation();

			// In case the call is issued by a call to a repository the verification has already taken place
			if (Repository.class.isInstance(invocation.getThis())) {
				return;
			}

		} catch (IllegalStateException e) {}

		Class<? extends Object> domainType = entity.getClass();
		EntityManager entityManager = context.getEntityManagerByManagedType(domainType);
		JpaEntityInformation entityInformation = JpaEntityInformationSupport.getEntityInformation(domainType,
				entityManager);

		QueryAugmentationEngine engine = new QueryAugmentationEngine(Collections.singleton(augmentor));
		QueryExecutor<Object, Serializable> executor = new QueryExecutor<Object, Serializable>(entityInformation,
				entityManager, engine, null);

		JpaUpdateContext<Object, Serializable> updateContext = new JpaUpdateContext<Object, Serializable>(entity, mode,
				entityManager, executor, entityInformation);

		augmentor.prepareUpdate(updateContext, null);
	}

	/**
	 * Creates default {@link Permission} instances when an entity is created. TODO: Make sure the default permissions can
	 * be configured somewhere.
	 * 
	 * @param entity
	 */
	@PostPersist
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void createDefaultAcl(Object entity) {

		if (Permission.class.isInstance(entity)) {
			return;
		}

		Class<? extends Object> domainType = entity.getClass();
		EntityManager entityManager = context.getEntityManagerByManagedType(domainType);
		JpaEntityInformation entityInformation = JpaEntityInformationSupport.getEntityInformation(domainType,
				entityManager);

		for (String value : Arrays.asList("read", "write")) {

			Permission permission = new Permission();
			permission.setDomainId(entityInformation.getId(entity).toString());
			permission.setDomainType(domainType.getName());
			permission.setUsername(SecurityContextHolder.getContext().getAuthentication().getName());
			permission.setPermission(value);

			entityManager.persist(permission);
		}
	}
}
