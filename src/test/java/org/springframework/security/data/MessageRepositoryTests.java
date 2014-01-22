/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.security.data;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.augment.JpaSoftDeleteQueryAugmentor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.SoftDelete;
import org.springframework.data.repository.augment.QueryAugmentor;
import org.springframework.data.repository.augment.QueryContext;
import org.springframework.data.repository.augment.UpdateContext;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Rob Winch
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MessageRepositoryTests.Config.class)
@Transactional
public class MessageRepositoryTests {
    @Configuration
//    @ComponentScan
//    @EnableJpaRepositories
    static class Config {
        @Bean
        public DataSource dataSource() {
            EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
            return builder.setType(EmbeddedDatabaseType.H2).build();
        }

        @Bean
        public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
            HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
            vendorAdapter.setDatabase(Database.H2);
            vendorAdapter.setGenerateDdl(true);
            vendorAdapter.setShowSql(true);

            LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
            factory.setJpaVendorAdapter(vendorAdapter);
            factory.setPackagesToScan(Message.class.getPackage().getName());
            factory.setDataSource(dataSource());

            return factory;
        }

        @Bean
        public PlatformTransactionManager transactionManager() {
            JpaTransactionManager txManager = new JpaTransactionManager();
            txManager.setEntityManagerFactory(entityManagerFactory().getObject());
            return txManager;
        }
    }

    @PersistenceContext
    EntityManager em;

    MessageRepository noAclRepository;
    MessageRepository repository;
    Message message;

    @Before
    public void setUp() {
        message = new Message();
        message.setText("Hi");

        JpaRepositoryFactory factory = new JpaRepositoryFactory(em);
		AclQueryAugmentor augmentor = new AclQueryAugmentor();

        List<QueryAugmentor<? extends QueryContext<?>, ? extends QueryContext<?>, ? extends UpdateContext<?>>> augmentors = new ArrayList<QueryAugmentor<? extends QueryContext<?>, ? extends QueryContext<?>, ? extends UpdateContext<?>>>();
        augmentors.add(augmentor);

        factory.setQueryAugmentors(augmentors);

        noAclRepository = factory.getRepository(NoAclMessageRepository.class);
        repository = factory.getRepository(MessageRepository.class);
    }

    @Test
    public void basicSaveAndDelete() {

        message = repository.save(message);
		repository.flush();

        assertThat(repository.findAll()).contains(message);
        assertThat(noAclRepository.findAll()).contains(message);
    }


    interface NoAclMessageRepository extends MessageRepository {

    }
}
