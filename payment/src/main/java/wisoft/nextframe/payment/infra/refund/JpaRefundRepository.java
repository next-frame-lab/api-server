package wisoft.nextframe.payment.infra.refund;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaRefundRepository extends JpaRepository<RefundEntity, UUID> {
	Optional<RefundEntity> findByPaymentId(UUID paymentId);
}
