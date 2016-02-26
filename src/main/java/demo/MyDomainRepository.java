package demo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

import com.querydsl.core.types.dsl.StringPath;

/**
 * @author Rob Winch
 */
@Acled
public interface MyDomainRepository
		extends JpaRepository<MyDomain, Long>, QueryDslPredicateExecutor<MyDomain>, QuerydslBinderCustomizer<QMyDomain> {

	@Query("select d from MyDomain d")
	List<MyDomain> findAllWithQuery();

	@Override
	default void customize(final QuerydslBindings bindings, final QMyDomain root) {
		bindings.bind(String.class).first((final StringPath path, final String value) -> path.containsIgnoreCase(value));
	}
}
