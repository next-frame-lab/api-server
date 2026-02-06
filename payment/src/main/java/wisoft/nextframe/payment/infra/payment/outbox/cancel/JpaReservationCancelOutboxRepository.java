package wisoft.nextframe.payment.infra.payment.outbox.cancel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaReservationCancelOutboxRepository extends JpaRepository<ReservationCancelOutboxEntity, UUID> {

	Optional<ReservationCancelOutboxEntity> findByReservationId(UUID reservationId);

	@Query("""
		    select o
		    from ReservationCancelOutboxEntity o
		    where o.status = 'PENDING'
		      and o.nextRetryAt <= :now
		    order by o.createdAt asc
		""")
	List<ReservationCancelOutboxEntity> findReadyToRetry(
		@Param("now") LocalDateTime now,
		Pageable pageable
	);

	long countByStatus(String status);
}
