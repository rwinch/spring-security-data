package demo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * @author Rob Winch
 */
public interface MyDomainRepository extends PagingAndSortingRepository<MyDomain, Long> {

	@Query("select d from MyDomain d, Permission p where p.domainType = 'demo.MyDomain' AND p.domainId = d.id AND p.permission = 'read'")
	Page<MyDomain> findAll(Pageable pageable);
}
