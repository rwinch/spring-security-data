package demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.repository.augment.QueryAugmentor;
import org.springframework.data.repository.augment.QueryContext;
import org.springframework.data.repository.augment.UpdateContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = DemoApplication.class)
public class DemoApplicationTests {
	@Autowired
	EntityManager entityManager;
	
	MyDomainRepository repository;
	
	
	@Before
	public void setup() {
		JpaRepositoryFactory factory = new JpaRepositoryFactory(entityManager);
		AclQueryAugmentor augmentor = new AclQueryAugmentor();

		List<QueryAugmentor<? extends QueryContext<?>, ? extends QueryContext<?>, ? extends UpdateContext<?>>> augmentors = //
		new ArrayList<QueryAugmentor<? extends QueryContext<?>, ? extends QueryContext<?>, ? extends UpdateContext<?>>>();
		augmentors.add(augmentor);

		factory.setQueryAugmentors(augmentors);
		
		repository = factory.getRepository(MyDomainRepository.class);
	}

	@WithMockUser("rob")
	@Test
	public void findAllOnlyRobFindsAllowed() {
		Page<MyDomain> results = repository.findAll(new PageRequest(0, 10));

		assertThat(results.getTotalElements()).isEqualTo(2);
	}

	@WithMockUser("rob")
	@Test
	public void findOneSuccess() {
		assertThat(repository.findOne(1L)).isNotNull();
	}


	@WithMockUser("luke")
	@Test
	public void findAllLukeOnlyFindsAllowed() {
		Page<MyDomain> results = repository.findAll(new PageRequest(0, 10));

		assertThat(results.getTotalElements()).isEqualTo(0);
	}
}
