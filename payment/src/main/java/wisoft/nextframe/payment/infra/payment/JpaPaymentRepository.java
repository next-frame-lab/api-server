package wisoft.nextframe.payment.infra.payment;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaPaymentRepository extends JpaRepository<PaymentEntity, UUID> {
	Optional<PaymentEntity> findByReservationId(UUID reservationId);
}