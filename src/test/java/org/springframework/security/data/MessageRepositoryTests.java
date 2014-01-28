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

import static org.fest.assertions.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.repository.augment.QueryAugmentor;
import org.springframework.data.repository.augment.QueryContext;
import org.springframework.data.repository.augment.UpdateContext;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

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
    MessageRepository aclRepository;
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

        noAclRepository = factory.getRepository(MessageRepository.class);
        aclRepository = factory.getRepository(AclMessageRepository.class);
    }

    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void ownerCanPerformAllOperations() {
        withUser("user");

        message = aclRepository.save(message);
        aclRepository.flush();

        assertThat(aclRepository.findAll()).contains(message);

        message.setText("Goodbye");
        message = aclRepository.save(message);
        aclRepository.flush();

        message = aclRepository.getOne(message.getId());

        assertThat(message.getText()).isEqualTo("Goodbye");

        aclRepository.delete(message.getId());

        assertThat(aclRepository.findOne(message.getId())).isNull();
    }

    @Test
    public void nonOwnerCannotPerformAnyOperations() {
        withUser("user");

        message = aclRepository.save(message);
        aclRepository.flush();

        withUser("user2");

        // acl prevents user2 from finding the Message
        assertThat(aclRepository.findAll()).isEmpty();

        // without acl we still find the Message
        assertThat(noAclRepository.findAll()).contains(message);

        // acl prevents updating the Message (could accept an Exception here)
        message.setText("Goodbye");
        message = aclRepository.save(message);
        aclRepository.flush();

        // verify the Message is not updated
        message = noAclRepository.getOne(message.getId());
        assertThat(message.getText()).isEqualTo("Hello");

        // Message cannot be deleted (could accept an Exception here)
        aclRepository.delete(message.getId());
        aclRepository.flush();

        // Message still exists
        assertThat(noAclRepository.findOne(message.getId())).isNotNull();
    }


    public void withUser(String username) {
        List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("ROLE_USER");
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, "password", authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
