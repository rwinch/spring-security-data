package demo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * @author Rob Winch
 */
public interface MyDomainRepository extends PagingAndSortingRepository<MyDomain, Long> {

	@Query("select d from MyDomain d")
	Page<MyDomain> findAll(Pageable pageable);
}
