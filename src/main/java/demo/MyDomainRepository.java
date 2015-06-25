package demo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * @author Rob Winch
 */
public interface MyDomainRepository extends JpaRepository<MyDomain, Long> {

	// Demo Bug in Spring Data
	@Query("select d from MyDomain d")
	List<MyDomain> findAllWithQuery();
}
