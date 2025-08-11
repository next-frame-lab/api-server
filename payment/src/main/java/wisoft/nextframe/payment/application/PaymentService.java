package wisoft.nextframe.payment.application;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import wisoft.nextframe.payment.application.port.output.ExternalPaymentClient;
import wisoft.nextframe.payment.application.port.output.PaymentRepository;
import wisoft.nextframe.payment.application.port.output.TicketingClient;
import wisoft.nextframe.payment.common.Money;
import wisoft.nextframe.payment.domain.Payment;
import wisoft.nextframe.payment.domain.PaymentId;
import wisoft.nextframe.payment.domain.PaymentNotFoundException;
import wisoft.nextframe.schedulereservationticketing.reservation.domain.ReservationId;

@Service
@RequiredArgsConstructor
public class PaymentService {

	private final TicketingClient ticketingClient;
	private final PaymentRepository paymentRepository;
	private final ExternalPaymentClient externalPaymentClient;

	public Payment requestPayment(ReservationId reservationId, Money amount, LocalDateTime requestedAt) {
		Payment payment = Payment.request(amount, reservationId, requestedAt);
		externalPaymentClient.requestPayment(payment);
		paymentRepository.save(payment);
		return payment;
	}

	public void approvePayment(PaymentId id) {
		Payment payment = paymentRepository.findById(id)
			.orElseThrow(PaymentNotFoundException::new);

		payment.approve();
		paymentRepository.save(payment);

		// Ticket 발급 요청 (ReservationId 기반)
		ticketingClient.issueTicket(payment.getReservationId().getValue());
	}

	public void failPayment(PaymentId paymentId) {
		Payment payment = paymentRepository.findById(paymentId)
			.orElseThrow(PaymentNotFoundException::new);

		payment.fail();
		paymentRepository.save(payment);
	}
}

