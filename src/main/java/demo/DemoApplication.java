package demo;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.AclPermissionEvaluator;
import org.springframework.security.acls.domain.AclAuthorizationStrategy;
import org.springframework.security.acls.domain.AclAuthorizationStrategyImpl;
import org.springframework.security.acls.domain.ConsoleAuditLogger;
import org.springframework.security.acls.domain.DefaultPermissionFactory;
import org.springframework.security.acls.domain.DefaultPermissionGrantingStrategy;
import org.springframework.security.acls.domain.EhCacheBasedAclCache;
import org.springframework.security.acls.domain.PermissionFactory;
import org.springframework.security.acls.jdbc.BasicLookupStrategy;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.AclCache;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.PermissionGrantingStrategy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@SpringBootApplication
public class DemoApplication {

	@Autowired private DataSource dataSource;

	/**
	 * Permission factory.
	 *
	 * @return the permission factory
	 */
	@Bean
	public PermissionFactory permissionFactory() {
		return new DefaultPermissionFactory();
	}

	/**
	 * Permission evaluator.
	 *
	 * @return the permission evaluator
	 */
	@Bean
	public PermissionEvaluator permissionEvaluator() {
		final PermissionEvaluator permissionEvaluator = new AclPermissionEvaluator(aclService());
		return permissionEvaluator;
	}

	/**
	 * Acl service.
	 *
	 * @return the mutable acl service
	 */
	@Bean
	public MutableAclService aclService() {
		return new JdbcMutableAclService(dataSource, lookupStrategy(), aclCache());
	}

	/**
	 * Lookup strategy.
	 *
	 * @return the lookup strategy
	 */
	@Bean
	public LookupStrategy lookupStrategy() {
		return new BasicLookupStrategy(dataSource, aclCache(), aclAuthorizationStrategy(), new ConsoleAuditLogger());
	}

	/**
	 * Acl cache.
	 *
	 * @return the acl cache
	 */
	@Bean
	public AclCache aclCache() {
		return new EhCacheBasedAclCache(cacheConfiguration().getObject(), permissionGrantingStrategy(),
				aclAuthorizationStrategy());
	}

	/**
	 * Cache configuration.
	 *
	 * @return the eh cache factory bean
	 */
	@Bean
	public EhCacheFactoryBean cacheConfiguration() {
		final EhCacheFactoryBean cacheFactoryBean = new EhCacheFactoryBean();
		final EhCacheManagerFactoryBean cacheManagerFactoryBean = new EhCacheManagerFactoryBean();
		cacheManagerFactoryBean.setCacheManagerName("aclCacheManager");
		cacheFactoryBean.setCacheManager(cacheManagerFactoryBean.getObject());
		cacheFactoryBean.setCacheName("aclCache");
		return cacheFactoryBean;
	}

	/**
	 * Permission granting strategy.
	 *
	 * @return the permission granting strategy
	 */
	@Bean
	public PermissionGrantingStrategy permissionGrantingStrategy() {
		return new DefaultPermissionGrantingStrategy(new ConsoleAuditLogger());
	}

	/**
	 * Acl authorization strategy.
	 *
	 * @return the acl authorization strategy
	 */
	@Bean
	public AclAuthorizationStrategy aclAuthorizationStrategy() {
		return new AclAuthorizationStrategyImpl(new SimpleGrantedAuthority("ROLE_ACL_ADMIN"));
	}

	@Bean
	public AclJpaHelper aclJpaHelper() {
		return new AclJpaHelper();
	}

	public static void main(final String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
}
