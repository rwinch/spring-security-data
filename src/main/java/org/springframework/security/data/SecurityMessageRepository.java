package org.springframework.security.data;

import org.springframework.data.jpa.repository.Query;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Rob Winch
 */
@Repository
public interface SecurityMessageRepository extends MessageRepository {
	@Override
	@Query("select m from Message m where m.to.id = ?#{ principal?.id }")
	List<Message> findAll();

	@PreAuthorize("#m?.to?.id == principal?.id")
	@Override
	<M extends Message> M save(@P("m") M m);

	@PreAuthorize("#m?.to?.id == principal?.id")
	@Override
	void delete(@P("m") Message m);
}
