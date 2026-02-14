package wisoft.nextframe.payment.application.refund;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.payment.application.payment.port.output.PaymentRepository;
import wisoft.nextframe.payment.application.payment.port.output.ReservationReader;
import wisoft.nextframe.payment.application.refund.port.output.RefundRepository;
import wisoft.nextframe.payment.domain.payment.Payment;
import wisoft.nextframe.payment.domain.payment.PaymentId;
import wisoft.nextframe.payment.domain.payment.PaymentIssuer;
import wisoft.nextframe.payment.domain.payment.PaymentNotFoundException;
import wisoft.nextframe.payment.domain.refund.Refund;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundTransactionService {

	private final PaymentRepository paymentRepository;
	private final RefundRepository refundRepository;
	private final ReservationReader reservationReader;
	private final PaymentIssuer paymentIssuer;

	/**
	 * 환불 준비: 검증 + Refund 생성 (PG 호출 전 단계)
	 * 멱등성 체크 후 이미 환불된 경우 기존 환불을 포함한 결과를 반환한다.
	 */
	@Transactional(readOnly = true)
	public RefundPrepareResult prepareRefund(UUID paymentId) {
		// 1. Payment 조회
		Payment payment = paymentRepository.findById(PaymentId.of(paymentId))
			.orElseThrow(PaymentNotFoundException::new);

		// 2. 멱등성 체크 - 이미 환불된 경우
		Optional<Refund> existingRefund = refundRepository.findByPaymentId(paymentId);
		if (existingRefund.isPresent()) {
			log.warn("이미 환불된 결제 - paymentId: {}", paymentId);
			return RefundPrepareResult.alreadyRefunded(existingRefund.get());
		}

		// 3. 공연 시작 시간 조회
		LocalDateTime performanceDateTime = reservationReader.getPerformanceDateTime(payment.getReservationId());

		// 4. 환불 발급 (정책 결정 + Refund 생성 + 환불 가능 검증 + payment.assignRefund)
		LocalDateTime now = LocalDateTime.now();
		Refund refund = paymentIssuer.issueRefund(payment, now, performanceDateTime);

		return RefundPrepareResult.prepared(payment, refund);
	}

	/**
	 * 환불 완료: PG 승인 후 DB 저장
	 */
	@Transactional
	public Refund completeRefund(UUID paymentId, Refund refund, String reason) {
		// 1. Payment 재조회 (트랜잭션 컨텍스트 내에서)
		Payment payment = paymentRepository.findById(PaymentId.of(paymentId))
			.orElseThrow(PaymentNotFoundException::new);

		// 2. 승인 처리
		refund.approve();
		payment.assignRefund(refund);

		// 3. 저장
		log.info("환불 처리 완료 - paymentId: {}, refundId: {}, refundAmount: {}",
			paymentId, refund.getRefundId().getValue(), refund.getRefundedAmount());
		refundRepository.save(refund, paymentId, reason);
		paymentRepository.save(payment);

		return refund;
	}

	public record RefundPrepareResult(
		boolean alreadyRefunded,
		Payment payment,
		Refund refund
	) {
		public static RefundPrepareResult alreadyRefunded(Refund existingRefund) {
			return new RefundPrepareResult(true, null, existingRefund);
		}

		public static RefundPrepareResult prepared(Payment payment, Refund refund) {
			return new RefundPrepareResult(false, payment, refund);
		}
	}
}
