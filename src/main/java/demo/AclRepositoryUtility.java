package demo;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.repository.augment.QueryContext.QueryMode;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.core.Authentication;

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
}
