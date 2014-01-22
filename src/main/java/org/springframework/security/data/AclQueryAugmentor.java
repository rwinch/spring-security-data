/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.security.data;

import org.springframework.data.jpa.repository.support.JpaCriteriaQueryContext;
import org.springframework.data.jpa.repository.support.JpaQueryContext;
import org.springframework.data.jpa.repository.support.JpaUpdateContext;
import org.springframework.data.repository.SoftDelete;
import org.springframework.data.repository.augment.*;
import org.springframework.data.repository.core.EntityMetadata;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;

/**
 * @author Rob Winch
 */
public class AclQueryAugmentor implements QueryAugmentor<JpaCriteriaQueryContext<?, ?>, JpaQueryContext, JpaUpdateContext<?>> {

	@Override
	public boolean supports(MethodMetadata method, QueryContext.QueryMode queryMode, EntityMetadata<?> entityMetadata) {
		return true;
	}

	@Override
	public JpaQueryContext augmentNativeQuery(JpaQueryContext query, MethodMetadata methodMetadata) {
		return query;
	}

	@Override
	public JpaCriteriaQueryContext<?, ?> augmentQuery(JpaCriteriaQueryContext<?, ?> query, MethodMetadata methodMetadata) {
		return query;
	}

	@Override
	public JpaUpdateContext<?> augmentUpdate(JpaUpdateContext<?> update, MethodMetadata methodMetadata) {
		System.out.println("augmentUpdate");
		return update;
	}
}