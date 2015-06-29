package demo;

import org.springframework.data.jpa.repository.support.JpaCriteriaQueryContext;
import org.springframework.data.jpa.repository.support.JpaQueryContext;
import org.springframework.data.jpa.repository.support.JpaUpdateContext;
import org.springframework.data.repository.augment.MethodMetadata;
import org.springframework.data.repository.augment.QueryAugmentor;
import org.springframework.data.repository.augment.QueryContext;
import org.springframework.data.repository.core.EntityMetadata;

/**
 * Demo bug in Spring Data
 * 
 * @author rwinch
 */
public class NoOpAugmentor
		implements QueryAugmentor<JpaCriteriaQueryContext<?, ?>, JpaQueryContext, JpaUpdateContext<?, ?>> {

	@Override
	public boolean supports(MethodMetadata method, QueryContext.QueryMode queryMode, EntityMetadata<?> entityMetadata) {
		return true;
	}

	@Override
	public JpaQueryContext augmentNativeQuery(JpaQueryContext query, MethodMetadata methodMetadata) {
		return query;
	}

	@Override
	public JpaCriteriaQueryContext<?, ?> augmentQuery(JpaCriteriaQueryContext<?, ?> context,
			MethodMetadata methodMetadata) {
		return context;
	}

	@Override
	public JpaUpdateContext<?, ?> augmentUpdate(JpaUpdateContext<?, ?> update, MethodMetadata methodMetadata) {
		return update;
	}
}
