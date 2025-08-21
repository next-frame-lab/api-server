package wisoft.nextframe.payment.application.payment;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.payment.application.payment.port.output.PaymentRepository;
import wisoft.nextframe.payment.application.payment.port.output.TicketingClient;
import wisoft.nextframe.payment.common.Money;
import wisoft.nextframe.payment.domain.ReservationId;
import wisoft.nextframe.payment.domain.payment.Payment;
import wisoft.nextframe.payment.domain.payment.PaymentId;
import wisoft.nextframe.payment.domain.payment.exception.PaymentConfirmedException;
import wisoft.nextframe.payment.presentation.payment.dto.PaymentConfirmRequest;

// 도메인 핵심 로직을 담당하는 서비스. 외부 시스템의 세부 구현에 의존하지 않습니다.
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

	private final TicketingClient ticketingClient;
	private final PaymentRepository paymentRepository;
	private final TossPaymentsGateway tossPaymentsGateway;

	@Transactional
	public Payment confirmPayment(PaymentConfirmRequest request) {

		// 1. 이미 승인된 결제가 있는지 우선 조회 (orderId 등으로)
		Optional<Payment> existingOpt = paymentRepository.findById(PaymentId.of(UUID.fromString(request.orderId())));
		if (existingOpt.isPresent()) {
			Payment existing = existingOpt.get();
			if (existing.isSucceeded()) {
				log.warn("이미 승인된 결제 - orderId: {}", request.orderId());
				return existing;
			}
			// 실패/대기 상태면 이후 로직 진행 (혹은 예외)
		}


		log.info("결제 요청 시작 - reservationId: {}, amount: {}", request.orderId(), request.amount());
		Payment payment = Payment.request(
			Money.of(request.amount()),
			ReservationId.of(UUID.fromString(request.orderId())),
			LocalDateTime.now()
		);
		log.debug("결제 엔티티 생성 완료: {}", payment);


		// 외부 결제 승인
		TossPaymentsGateway.TossPaymentConfirmResult result = tossPaymentsGateway.confirmPayment(
			request.paymentKey(),
			request.orderId(),
			request.amount()
		);

		if (result.isSuccess()) {
			log.info("결제 승인 성공 - paymentId: {}, totalAmount: {}", payment.getId(), result.totalAmount());
			payment.approve();
			paymentRepository.save(payment);
			log.info("결제 저장 완료 - paymentId: {}", payment.getId());

			ticketingClient.issueTicket(payment.getReservationId());
			log.info("티켓 발급 요청 완료 - reservationId: {}", payment.getReservationId());

			return payment;
		} else {
			log.warn("결제 승인 실패 - paymentId: {}, errorCode: {}, errorMessage: {}",
				payment.getId(),
				result.errorCode(),
				result.errorMessage()
			);
			payment.fail();
			paymentRepository.save(payment);
			throw new PaymentConfirmedException(result.errorCode());
		}
	}
}

