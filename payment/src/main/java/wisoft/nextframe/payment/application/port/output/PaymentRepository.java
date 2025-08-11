package wisoft.nextframe.payment.application.port.output;

import java.util.Optional;

import wisoft.nextframe.payment.domain.Payment;
import wisoft.nextframe.payment.domain.PaymentId;

public interface PaymentRepository {
	Payment save(Payment payment);

	Optional<Payment> findById(PaymentId id);
}
