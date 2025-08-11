package wisoft.nextframe.payment.infra.persistence.jpa;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaPaymentRepository extends JpaRepository<PaymentEntity, UUID> {
}