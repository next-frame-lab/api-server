package wisoft.nextframe.payment.infra.ticketissue.adaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaTicketIssueOutboxRepository extends JpaRepository<TicketIssueOutboxEntity, UUID> {

	Optional<TicketIssueOutboxEntity> findByReservationId(UUID reservationId);

	@Query("""
		    select o
		    from TicketIssueOutboxEntity o
		    where o.status = 'PENDING'
		      and o.nextRetryAt <= :now
		    order by o.createdAt asc
		""")
	List<TicketIssueOutboxEntity> findReadyToRetry(
		@Param("now") LocalDateTime now,
		Pageable pageable
	);

	long countByStatus(String status);
}
