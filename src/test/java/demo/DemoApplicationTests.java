package demo;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = DemoApplication.class)
public class DemoApplicationTests {
	@Autowired
	MyDomainRepository repository;

	@WithMockUser("rob")
	@Test
	public void findAllOnlyFindsAllowed() {
		Page<MyDomain> results = repository.findAll(new PageRequest(0, 10));

		assertThat(results.getTotalElements()).isEqualTo(2);
	}

}
