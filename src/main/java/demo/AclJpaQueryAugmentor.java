package demo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.repository.support.JpaCriteriaQueryContext;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaQueryContext;
import org.springframework.data.jpa.repository.support.JpaUpdateContext;
import org.springframework.data.jpa.repository.support.QueryExecutor;
import org.springframework.data.repository.augment.AnnotationBasedQueryAugmentor;
import org.springframework.data.repository.augment.QueryContext.QueryMode;
import org.springframework.data.repository.augment.UpdateContext.UpdateMode;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

public class AclJpaQueryAugmentor<T> extends
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

		JpaEntityInformation<?, ?> entityInformation = context.getEntityInformation();

		WhereClause clause = getIdGuard(entityInformation.getIdAttribute().getName())
				.and(getPermissionGuard(entityInformation.getJavaType(), context.getMode(), authentication));

		return context.augment("AclEntry acl", clause.toString(), clause.getParameters());
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.augment.AnnotationBasedQueryAugmentor#prepareQuery(org.springframework.data.repository.augment.QueryContext, java.lang.annotation.Annotation)
	 */
	@Override
	protected JpaCriteriaQueryContext<?, ?> prepareQuery(JpaCriteriaQueryContext<?, ?> context, Acled annotation) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null) {
			return context;
		}

		CriteriaQuery<?> criteriaQuery = context.getQuery();
		CriteriaBuilder builder = context.getCriteriaBuilder();
		JpaEntityInformation<?, ?> entityInformation = context.getEntityInformation();

		Predicate predicate = AclRepositoryUtility.getPermissionPredicate(criteriaQuery, builder, context.getMode(),
				authentication, entityInformation.getJavaType(),
				context.getRoot().get(entityInformation.getIdAttribute().getName()));

		Predicate restriction = criteriaQuery.getRestriction();
		criteriaQuery.where(restriction == null ? predicate : builder.and(restriction, predicate));

		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.augment.AnnotationBasedQueryAugmentor#prepareUpdate(org.springframework.data.repository.augment.UpdateContext, java.lang.annotation.Annotation)
	 */
	@Override
	protected JpaUpdateContext<T, ?> prepareUpdate(JpaUpdateContext<T, ?> context, Acled annotation) {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null) {
			return context;
		}

		QueryExecutor<T, ?> queryExecutor = context.getQueryExecutor();

		return queryExecutor.doWithFlushDisabled(new Callable<JpaUpdateContext<T, ?>>() {

			@Override
			public JpaUpdateContext<T, ?> call() throws Exception {

				if (context.isNewEntity()) {

					CriteriaBuilder builder = context.getEntityManager().getCriteriaBuilder();
					CriteriaQuery<Boolean> criteriaQuery = builder.createQuery(Boolean.class);
					Root<AclEntry> aclEntry = criteriaQuery.from(AclEntry.class);
					criteriaQuery.select(builder.gt(builder.count(aclEntry), 0));

					Predicate predicate = AclRepositoryUtility.getPermissionPredicate(criteriaQuery, builder,
							QueryMode.FOR_UPDATE, authentication, context.getEntity().getClass(), null);

					criteriaQuery.where(predicate);

					Boolean result = context.getEntityManager().createQuery(criteriaQuery).getSingleResult();

					if (!result) {
						throw new AccessDeniedException(
								String.format("Insufficient permissions %s entity %s", context.getMode(), context.getEntity()));
					}

					return context;
				}

				QueryMode mode = context.getMode().equals(UpdateMode.DELETE) ? QueryMode.FOR_DELETE : QueryMode.FOR_UPDATE;

				if (queryExecutor.executeCountByIdFor(context.getEntity(), mode) == 0L) {
					throw new AccessDeniedException(
							String.format("Insufficient permissions %s entity %s", mode, context.getEntity()));
				}

				return context;
			}
		});
	}

	private WhereClause getIdGuard(String identifierProperty) {
		return new WhereClause(String.format("{alias}.%s = acl.objectIdentity.objectIdIdentity", identifierProperty));
	}

	private WhereClause getPermissionGuard(Class<?> domainType, QueryMode mode, Authentication authentication) {

		WhereClause where = new WhereClause("MOD((acl.mask / :required_permission), 2) = 1",
				AclRepositoryUtility.getRequiredPermission(mode));
		where = where.and("acl.objectIdentity.aclClass.class_ = :domainType", domainType.getName());
		return where.and("acl.sid.sid = :username", authentication.getName());
	}

	private static class WhereClause {

		private static final Pattern PARAMETER = Pattern.compile(":(\\w+)");

		private final List<String> clause;
		private final Map<String, Object> parameters;

		public WhereClause(String clause) {
			this(clause, null);
		}

		public WhereClause(String clause, Object value) {

			this.clause = Arrays.asList(clause);
			this.parameters = value == null ? Collections.emptyMap()
					: Collections.singletonMap(getParameterName(clause), value);
		}

		/**
		 * @param clause
		 * @param parameters
		 */
		private WhereClause(List<String> clause, Map<String, Object> parameters) {
			this.clause = clause;
			this.parameters = parameters;
		}

		public WhereClause and(WhereClause clause) {

			List<String> clauses = new ArrayList<String>(this.clause);
			clauses.addAll(clause.clause);

			Map<String, Object> parameters = new HashMap<String, Object>(this.parameters);
			parameters.putAll(clause.parameters);

			return new WhereClause(clauses, parameters);
		}

		public WhereClause and(String clause) {
			return and(clause, null);
		}

		public WhereClause and(String clause, Object parameter) {
			return and(new WhereClause(clause, parameter));
		}

		public Map<String, Object> getParameters() {
			return Collections.unmodifiableMap(parameters);
		}

		public String toWhereString() {
			return "where ".concat(toString());
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return StringUtils.collectionToDelimitedString(clause, " and ");
		}

		private static String getParameterName(String clause) {

			Matcher matcher = PARAMETER.matcher(clause);
			return matcher.find() ? matcher.group(1) : null;
		}
	}

}
