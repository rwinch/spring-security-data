package demo;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.repository.support.JpaCriteriaQueryContext;
import org.springframework.data.jpa.repository.support.JpaQueryContext;
import org.springframework.data.jpa.repository.support.JpaUpdateContext;
import org.springframework.data.repository.augment.MethodMetadata;
import org.springframework.data.repository.augment.QueryAugmentor;
import org.springframework.data.repository.augment.QueryContext;
import org.springframework.data.repository.core.EntityMetadata;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AclQueryAugmentor
		implements QueryAugmentor<JpaCriteriaQueryContext<?, ?>, JpaQueryContext, JpaUpdateContext<?>> {

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

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null) {
			return context;
		}

		CriteriaQuery<?> criteriaQuery = context.getQuery();
		CriteriaBuilder builder = context.getCriteriaBuilder();

		// Assume "select d from Domain d where …"
		Root<?> root = context.getRoot();

		// Adds "from Permission p"
		Root<Permission> permission = criteriaQuery.from(Permission.class);

		// Adds "p.permission = 'read'"
		Predicate hasReadPermission = builder.equal(permission.get("permission"), "read");

		// Adds "p.domainId = d.id" - TODO: determine identifier property dynamically
		Predicate isDomainPermission = builder.equal(root.get("id"), permission.get("domainId"));

		// Adds "p.username = $authentication.name"
		Predicate isUserPermission = builder.equal(permission.get("username"), authentication.getName());

		// Concatenates atomic predicates
		Predicate predicate = builder.and(hasReadPermission, isDomainPermission, isUserPermission);

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
