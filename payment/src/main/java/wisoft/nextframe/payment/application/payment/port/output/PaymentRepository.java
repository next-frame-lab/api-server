package wisoft.nextframe.payment.application.payment.port.output;

import java.util.Optional;

import wisoft.nextframe.payment.domain.ReservationId;
import wisoft.nextframe.payment.domain.payment.Payment;
import wisoft.nextframe.payment.domain.payment.PaymentId;

public interface PaymentRepository {
	Payment save(Payment payment);

	Optional<Payment> findById(PaymentId id);

	Optional<Payment> findByReservationId(ReservationId reservationId);
}
