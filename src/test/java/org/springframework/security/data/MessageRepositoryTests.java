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
import static org.fest.assertions.Fail.fail;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Rob Winch
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Config.class)
@Transactional
public class MessageRepositoryTests {

	@PersistenceContext
	EntityManager em;

	@Autowired
	MessageRepository messageRepository;
	@Autowired
	SecurityMessageRepository securityMessageRepository;
	@Autowired
	UserRepository userRepository;
	Message message;

	@Before
	public void setUp() {
		message = new Message();
		message.setText("Hi");

		userRepository.save(createUser("user"));
		userRepository.save(createUser("user2"));

		userRepository.flush();

		withUser("user");
	}

	@After
	public void teardown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void ownerCanPerformAllOperations() {
		message.setTo(getUser());
		message = securityMessageRepository.save(message);
		securityMessageRepository.flush();

		assertThat(securityMessageRepository.findAll()).contains(message);

		message.setText("Goodbye");
		message = messageRepository.save(message);
		securityMessageRepository.flush();

		message = securityMessageRepository.getOne(message.getId());

		assertThat(message.getText()).isEqualTo("Goodbye");

		securityMessageRepository.delete(message.getId());

		assertThat(securityMessageRepository.findOne(message.getId())).isNull();
	}

	@Test
	 public void nonOwnerCannotPerformAnyOperations() {
		withUser("user");

		message.setTo(getUser());
		message = securityMessageRepository.save(message);
		messageRepository.flush();

		withUser("user2");

		// acl prevents user2 from finding the Message
		assertThat(securityMessageRepository.findAll()).isEmpty();

		// without acl we still find the Message
		assertThat(messageRepository.findAll()).contains(message);

		// acl prevents updating the Message (could accept an Exception here)
		message.setText("Goodbye");
		try {
			message = securityMessageRepository.save(message);
			fail("Expected Error");
		} catch(AccessDeniedException success) {}

		// verify the Message is not updated
		message = messageRepository.findOne(message.getId());
		assertThat(message.getText()).isEqualTo("Hi");

		// Message cannot be deleted (could accept an Exception here)
		try {
			securityMessageRepository.delete(message);
			securityMessageRepository.flush();
			fail("Expected Error");
		} catch (Exception success) {}

		// Message still exists
		assertThat(messageRepository.findOne(message.getId())).isNotNull();
	}

	@Test
	public void detachedTest() {
		withUser("user");

		message.setTo(getUser());
		message = securityMessageRepository.save(message);
		securityMessageRepository.flush();


		assertThat(securityMessageRepository.findAll()).contains(message);

		withUser("user2");

		message = securityMessageRepository.findOne(message.getId());
		message.setText("Goodbye");
		securityMessageRepository.flush();

		message = securityMessageRepository.findOne(message.getId());

		assertThat(message.getText()).isEqualTo("Hi");
	}

	public static MyUser getUser() {
		return (MyUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	public void withUser(String username) {
		List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("ROLE_USER");
		MyUser user = userRepository.findByEmail(username);
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, user.getPassword(), authorities);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	public static MyUser createUser(String username) {
		MyUser user = new MyUser();
		user.setEmail(username);
		user.setPassword("password");
		user.setFirstName(username);
		user.setLastName("Last");
		return user;
	}
}
