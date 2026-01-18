package wisoft.nextframe.payment.application.payment;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.payment.application.payment.exception.ReservationNotFoundException;
import wisoft.nextframe.payment.application.payment.port.output.PaymentGateway;
import wisoft.nextframe.payment.application.payment.port.output.PaymentRepository;
import wisoft.nextframe.payment.application.payment.port.output.ReservationReader;
import wisoft.nextframe.payment.common.Money;
import wisoft.nextframe.payment.common.exception.InvalidAmountException;
import wisoft.nextframe.payment.domain.ReservationId;
import wisoft.nextframe.payment.domain.payment.Payment;
import wisoft.nextframe.payment.domain.payment.exception.PaymentConfirmedFailedException;
import wisoft.nextframe.payment.presentation.payment.dto.PaymentConfirmRequest;

// 도메인 핵심 로직을 담당하는 서비스. 외부 시스템의 세부 구현에 의존하지 않습니다.
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentTransactionService {

	private final PaymentRepository paymentRepository;
	private final ReservationReader reservationReader;
	private final ApplicationEventPublisher eventPublisher;

	@Transactional
	public Payment applyConfirmResult(PaymentConfirmRequest request, PaymentGateway.PaymentConfirmResult result) {

		ReservationId reservationId = ReservationId.of(UUID.fromString(request.orderId()));

		if (!reservationReader.exists(reservationId)) {
			throw new ReservationNotFoundException(reservationId.value());
		}

		// 1. 중복 승인 체크
		Optional<Payment> existingOpt = paymentRepository.findByReservationId(reservationId);
		if (existingOpt.isPresent()) {
			Payment existing = existingOpt.get();
			if (existing.isSucceeded()) {
				log.warn("이미 승인된 결제 - orderId: {}", request.orderId());
				return existing;
			}
			// 실패/대기 상태면 이후 로직 진행 (혹은 예외)
		}

		log.info("결제 요청 시작 - reservationId: {}, amount: {}", reservationId.value(), request.amount());
		Payment payment = existingOpt.orElseGet(() ->
			Payment.request(
				Money.of(request.amount()),
				reservationId,
				LocalDateTime.now()
			));
		log.debug("결제 엔티티 생성 완료: {}", payment);

		if (result.isSuccess()) {
			if (!payment.getAmount().equals(Money.of(result.totalAmount()))) {
				log.error("결제 금액 불일치 - 예상: {}, 실제: {}", payment.getAmount(), result.totalAmount());
				payment.fail(); // 상태를 실패로 변경
				paymentRepository.save(payment);
				throw new InvalidAmountException();
			}
			log.info("결제 승인 성공 - paymentId: {}, totalAmount: {}", payment.getId(), result.totalAmount());
			payment.approve();
			paymentRepository.save(payment);
			log.info("결제 저장 완료 - paymentId: {}", payment.getId());

			payment.getDomainEvents().forEach(eventPublisher::publishEvent);
			payment.clearDomainEvents();

			return payment;
		} else {
			log.warn("결제 승인 실패 - paymentId: {}, errorCode: {}, errorMessage: {}",
				payment.getId(),
				result.errorCode(),
				result.errorMessage()
			);
			payment.fail();
			paymentRepository.save(payment);
			throw new PaymentConfirmedFailedException(result.errorCode());
		}
	}
}