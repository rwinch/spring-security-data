package demo;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import org.springframework.data.jpa.repository.support.JpaCriteriaQueryContext;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaQueryContext;
import org.springframework.data.jpa.repository.support.JpaUpdateContext;
import org.springframework.data.jpa.repository.support.QueryExecutor;
import org.springframework.data.repository.augment.AnnotationBasedQueryAugmentor;
import org.springframework.data.repository.augment.QueryContext.QueryMode;
import org.springframework.data.repository.augment.UpdateContext.UpdateMode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AclQueryAugmentor<T> extends
		AnnotationBasedQueryAugmentor<Acled, JpaCriteriaQueryContext<?, ?>, JpaQueryContext, JpaUpdateContext<T, ?>> {

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.augment.AnnotationBasedQueryAugmentor#prepareNativeQuery(org.springframework.data.repository.augment.QueryContext, java.lang.annotation.Annotation)
	 */
	@Override
	protected JpaQueryContext prepareNativeQuery(JpaQueryContext context, Acled annotation) {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null) {
			return context;
		}

		return context.augment("Permission p",
				String.format("{alias}.id = p.domainId and p.permission = '%s' and p.domainType = '%s' and p.username = '%s'",
						getRequiredPermission(context.getMode()), MyDomain.class.getName(), authentication.getName()));
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.augment.AnnotationBasedQueryAugmentor#prepareQuery(org.springframework.data.repository.augment.QueryContext, java.lang.annotation.Annotation)
	 */
	@Override
	protected JpaCriteriaQueryContext<?, ?> prepareQuery(JpaCriteriaQueryContext<?, ?> context, Acled annotation) {
		return augmentPermission(context);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.augment.AnnotationBasedQueryAugmentor#prepareUpdate(org.springframework.data.repository.augment.UpdateContext, java.lang.annotation.Annotation)
	 */
	@Override
	protected JpaUpdateContext<T, ?> prepareUpdate(JpaUpdateContext<T, ?> context, Acled annotation) {

		QueryMode mode = context.getMode().equals(UpdateMode.DELETE) ? QueryMode.FOR_DELETE : QueryMode.FOR_UPDATE;
		QueryExecutor<T, ?> queryExecutor = context.getQueryExecutor();

		return queryExecutor.executeCountByIdFor(context.getEntity(), mode) == 0L ? null : context;
	}

	private static JpaCriteriaQueryContext<?, ?> augmentPermission(JpaCriteriaQueryContext<?, ?> context) {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null) {
			return context;
		}

		CriteriaQuery<?> criteriaQuery = context.getQuery();
		CriteriaBuilder builder = context.getCriteriaBuilder();
		JpaEntityInformation<?, ?> entityInformation = context.getEntityInformation();

		// Assume "select d from Domain d where …"
		Root<?> root = context.getRoot();

		// Adds "from Permission p"
		Root<Permission> permission = criteriaQuery.from(Permission.class);

		// Adds "p.permission = 'read'"
		Predicate hasReadPermission = builder.equal(permission.get("permission"), getRequiredPermission(context.getMode()));

		SingularAttribute<?, ?> idAttribute = entityInformation.getIdAttribute();
		Predicate isDomainPermission = builder.equal(root.get(idAttribute.getName()), permission.get("domainId"));

		Predicate isDomainType = builder.equal(permission.get("domainType"), entityInformation.getJavaType().getName());

		// Adds "p.username = $authentication.name"
		Predicate isUserPermission = builder.equal(permission.get("username"), authentication.getName());

		// Concatenates atomic predicates
		Predicate predicate = builder.and(hasReadPermission, isDomainPermission, isUserPermission, isDomainType);

		Predicate restriction = criteriaQuery.getRestriction();
		criteriaQuery.where(restriction == null ? predicate : builder.and(restriction, predicate));

		return context;
	}

	private static String getRequiredPermission(QueryMode mode) {

		switch (mode) {
			case FOR_DELETE:
				return "write";
			default:
				return "read";
		}
	}
}
