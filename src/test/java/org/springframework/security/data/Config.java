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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.support.DetachingJpaResultPostProcessor;
import org.springframework.data.jpa.repository.support.ExpressionEvaluationContextProvider;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.Serializable;

/**
 * @author Rob Winch
 */

@Configuration
@ComponentScan
@EnableJpaRepositories(repositoryFactoryBeanClass = Config.CustomJpaRepositoryFactoryBean.class)
@EnableGlobalMethodSecurity(prePostEnabled = true)
class Config {
	public static class CustomJpaRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable> extends
			JpaRepositoryFactoryBean<T, S, ID> {

		@Override
		public void afterPropertiesSet() {

			setJpaResultPostProcessor(new DetachingJpaResultPostProcessor(entityManager));

			super.afterPropertiesSet();
		}
	}

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
	public ExpressionEvaluationContextProvider expressionEvaluationContextProvider() {
		return new ExpressionEvaluationContextProvider() {
			@Override
			public StandardEvaluationContext getEvaluationContext() {
				StandardEvaluationContext ctx = new StandardEvaluationContext();
				Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
				ctx.setVariable("authentication", authentication);
				ctx.setVariable("principal", authentication == null ? null : authentication.getPrincipal());
				ctx.setRootObject(new SecurityExpressionRoot(authentication) {});
				return ctx;
			}
		};
	}

	@Bean
	public PlatformTransactionManager transactionManager() {
		JpaTransactionManager txManager = new JpaTransactionManager();
		txManager.setEntityManagerFactory(entityManagerFactory().getObject());
		return txManager;
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.inMemoryAuthentication();
	}
}