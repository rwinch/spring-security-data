package org.springframework.security.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Rob Winch
 */
@Repository
public interface UserRepository extends JpaRepository<MyUser,Long> {
	MyUser findByEmail(String email);
}
