package wisoft.nextframe.payment.application.payment;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.payment.application.payment.port.output.ExternalPaymentClient;
import wisoft.nextframe.payment.application.payment.port.output.PaymentRepository;
import wisoft.nextframe.payment.application.payment.port.output.TicketingClient;
import wisoft.nextframe.payment.common.Money;
import wisoft.nextframe.payment.domain.payment.Payment;
import wisoft.nextframe.payment.domain.payment.PaymentId;
import wisoft.nextframe.payment.domain.payment.PaymentNotFoundException;
import wisoft.nextframe.schedulereservationticketing.reservation.ReservationId;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

	private final TicketingClient ticketingClient;
	private final PaymentRepository paymentRepository;
	private final ExternalPaymentClient externalPaymentClient;

	// 결제 요청: 즉시 성공/실패만, 상태는 REQUESTED
	@Transactional
	public Payment requestPayment(ReservationId reservationId, Money amount, LocalDateTime requestedAt) {
		log.info("결제 요청 시작 - reservationId: {}, amount: {}", reservationId, amount);
		Payment payment = Payment.request(amount, reservationId, requestedAt);
		log.debug("결제 엔티티 생성 완료: {}", payment);

		externalPaymentClient.requestPayment(payment);
		log.debug("외부 결제사 요청 완료: {}", payment.getId());

		paymentRepository.save(payment);
		log.info("결제 저장 완료 - paymentId: {}", payment.getId());
		return payment;
	}

	@Transactional
	public void approvePayment(PaymentId id) {
		log.info("결제 승인 시작 - paymentId: {}", id);
		Payment payment = paymentRepository.findById(id)
			.orElseThrow(() -> {
				log.error("결제 승인 실패 - 존재하지 않는 paymentId: {}", id);
				return new PaymentNotFoundException();
			});

		if (payment.isSucceeded()) {
			log.warn("이미 승인된 결제 - paymentId: {}", id);
			return;
		}

		payment.approve();
		paymentRepository.save(payment);
		log.info("결제 승인 완료 - paymentId: {}", id);

		ticketingClient.issueTicket(payment.getReservationId().getValue());
		log.info("티켓 발급 요청 완료 - reservationId: {}", payment.getReservationId());
	}

	public void failPayment(PaymentId paymentId) {
		Payment payment = paymentRepository.findById(paymentId)
			.orElseThrow(PaymentNotFoundException::new);

		payment.fail();
		paymentRepository.save(payment);
	}
}

