package demo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.data.jpa.repository.support.QueryExecutor;
import org.springframework.data.repository.augment.QueryContext.QueryMode;
import org.springframework.data.repository.augment.UpdateContext.UpdateMode;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * A utility for ACL security with Repositories.
 * 
 * @author gazal
 */
public class AclRepositoryUtility {

	/**
	 * Gets the required permission.
	 *
	 * @param mode the mode
	 * @return the required permission
	 */
	public static int getRequiredPermission(QueryMode mode) {

		switch (mode) {
			case FOR_DELETE:
			case FOR_UPDATE:
				return BasePermission.WRITE.getMask();
			default:
				return BasePermission.READ.getMask();
		}
	}

	/**
	 * Gets the permission predicate.
	 *
	 * @param criteriaQuery the criteria query
	 * @param builder the builder
	 * @param mode the mode
	 * @param authentication the authentication
	 * @param domainType the domain type
	 * @param idPath the id path
	 * @return the permission predicate
	 */
	public static Predicate getPermissionPredicate(CriteriaQuery<?> criteriaQuery, CriteriaBuilder builder,
			QueryMode mode, Authentication authentication, Class<?> domainType, Path<?> idPath) {
		Root<AclEntry> aclEntry = criteriaQuery.from(AclEntry.class);

		List<Predicate> predicates = new ArrayList<>();

		predicates.add(builder
				.equal(builder.mod(builder.toInteger(builder.quot(aclEntry.get("mask"), getRequiredPermission(mode))), 2), 1));

		predicates.add(builder.equal(aclEntry.get("sid").get("sid"), authentication.getName()));

		predicates.add(builder.equal(aclEntry.get("objectIdentity").get("aclClass").get("class_"), domainType.getName()));

		if (null != idPath) {
			predicates.add(builder.equal(idPath, aclEntry.get("objectIdentity").get("objectIdIdentity")));
		}

		// Concatenates atomic predicates
		return builder.and(predicates.toArray(new Predicate[] {}));
	}

	/**
	 * Verify acl.
	 *
	 * @param entity the entity
	 * @param mode the mode
	 * @param entityManager the entity manager
	 */
	public static void verifyAcl(Object entity, UpdateMode mode, EntityManager entityManager) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null) {
			return;
		}

		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Boolean> criteriaQuery = builder.createQuery(Boolean.class);
		Root<AclEntry> aclEntry = criteriaQuery.from(AclEntry.class);
		criteriaQuery.select(builder.gt(builder.count(aclEntry), 0));

		JpaEntityInformation entityInformation = JpaEntityInformationSupport.getEntityInformation(entity.getClass(),
				entityManager);
		Path idPath = null;
		if (!entityInformation.isNew(entity)) {
			Root<?> domainEntity = criteriaQuery.from(entityInformation.getJavaType());
			idPath = domainEntity.get(entityInformation.getIdAttribute());
			criteriaQuery.where(builder.equal(idPath, entityInformation.getId(entity)));
		}

		Predicate predicate = AclRepositoryUtility.getPermissionPredicate(criteriaQuery, builder, QueryMode.FOR_UPDATE,
				authentication, entityInformation.getJavaType(), idPath);

		Predicate restriction = criteriaQuery.getRestriction();
		criteriaQuery.where(restriction == null ? predicate : builder.and(restriction, predicate));

		QueryExecutor<?, ?> executor = new QueryExecutor<>(entityInformation, entityManager, null, null);
		Boolean result = executor.<Boolean> doWithFlushDisabled(new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				return entityManager.createQuery(criteriaQuery).getSingleResult();
			}

		});

		if (!result) {
			throw new AccessDeniedException(String.format("Insufficient permissions %s entity %s", mode, entity));
		}
	}
}
