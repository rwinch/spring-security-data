package demo;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;

import org.springframework.data.jpa.repository.support.JpaCriteriaQueryContext;
import org.springframework.data.jpa.repository.support.JpaQueryContext;
import org.springframework.data.jpa.repository.support.JpaUpdateContext;
import org.springframework.data.repository.augment.MethodMetadata;
import org.springframework.data.repository.augment.QueryAugmentor;
import org.springframework.data.repository.augment.QueryContext;
import org.springframework.data.repository.core.EntityMetadata;

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
	public JpaCriteriaQueryContext<?, ?> augmentQuery(JpaCriteriaQueryContext<?, ?> context, MethodMetadata methodMetadata) {
		CriteriaQuery<?> criteriaQuery = context.getQuery();
		CriteriaBuilder builder = context.getCriteriaBuilder();

		// just add anything I can to the criteria for now
		// need to add a new context root eventually
		Predicate predicate = builder.equal(context.getRoot().get("id"), "1");
		Predicate restriction = criteriaQuery.getRestriction();
		

		criteriaQuery.where(restriction == null ? predicate : builder.and(restriction, predicate));

		return context;
	}

	@Override
	public JpaUpdateContext<?> augmentUpdate(JpaUpdateContext<?> update, MethodMetadata methodMetadata) {
		System.out.println("augmentUpdate");
		return update;
	}
}
