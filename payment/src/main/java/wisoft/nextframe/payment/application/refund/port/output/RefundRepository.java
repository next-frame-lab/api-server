package wisoft.nextframe.payment.application.refund.port.output;

import java.util.Optional;
import java.util.UUID;

import wisoft.nextframe.payment.domain.refund.Refund;

public interface RefundRepository {
	Refund save(Refund refund, UUID paymentId, String reason);

	Optional<Refund> findByPaymentId(UUID paymentId);
}
