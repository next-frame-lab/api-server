package wisoft.nextframe.payment.application.payment;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.payment.application.payment.port.output.PaymentRepository;
import wisoft.nextframe.payment.application.payment.port.output.TicketingClient;
import wisoft.nextframe.payment.application.payment.port.output.TossPaymentsClient;
import wisoft.nextframe.payment.common.Money;
import wisoft.nextframe.payment.domain.payment.Payment;
import wisoft.nextframe.payment.domain.payment.PaymentId;
import wisoft.nextframe.payment.domain.payment.PaymentNotFoundException;
import wisoft.nextframe.payment.presentation.payment.dto.PaymentApprovedData;
import wisoft.nextframe.payment.presentation.payment.dto.PaymentConfirmRequest;
import wisoft.nextframe.payment.presentation.payment.dto.PaymentConfirmResponse;
import wisoft.nextframe.schedulereservationticketing.reservation.ReservationId;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

	private final TicketingClient ticketingClient;
	private final PaymentRepository paymentRepository;
	private final TossPaymentsClient tossClient;

	public PaymentConfirmResponse confirmPayment(PaymentConfirmRequest request) {

		if (request == null || request.paymentKey() == null || request.orderId() == null || request.amount() == null) {
			return new PaymentConfirmResponse("FAILURE", null, "MISSING_PARAM: paymentKey, orderId, amount 필수");
		}

		try {
			// 1. 토스 결제 승인 요청(동기)
			// tossClient는 동기 Map<String,Object> 반환하는 구조여야 함!
			Map<String, Object> body = tossClient.confirmPayment(
				request.paymentKey(), request.orderId(), request.amount()
			);

			// [1] HTTP status code, 바디 status, approvedAt 동시 체크
			String paymentStatus = String.valueOf(body.getOrDefault("status", ""));
			Object approvedAt = body.get("approvedAt");

			// 200~299 또는 바디의 status == DONE, approvedAt 존재하면 성공으로 간주
			boolean httpSuccess = true; // TossPaymentsAdaptor가 항상 200으로 내려주면 true, 아니면 실제 http status 받아서 비교
			boolean logicalSuccess = "DONE".equals(paymentStatus) && approvedAt != null;

			if (httpSuccess && logicalSuccess) {
				String reservationId = request.orderId();
				int totalAmount = extractInt(body.get("totalAmount"), request.amount());
				PaymentApprovedData data = new PaymentApprovedData(reservationId, totalAmount);
				return new PaymentConfirmResponse("SUCCESS", data, null);
			} else {
				String tossCode = String.valueOf(body.getOrDefault("code", "TOSS_ERROR"));
				String tossMsg = String.valueOf(body.getOrDefault("message", "토스 승인 실패"));
				log.warn("Toss confirm failed. paymentStatus={}, tossCode={}, tossMsg={}, body={}", paymentStatus, tossCode,
					tossMsg, body);
				String userMessage = mapToUserMessage(tossCode, tossMsg);
				return new PaymentConfirmResponse("FAILURE", null, userMessage);
			}
		} catch (Exception ex) {
			// 5. 네트워크/타임아웃/예외 처리
			String msg = ex.getMessage() != null ? ex.getMessage() : "NETWORK_ERROR";
			log.error("Toss confirmPayment exception", ex);
			return new PaymentConfirmResponse("FAILURE", null, "NETWORK_ERROR: " + msg);
		}
	}

	private int extractInt(Object v, int fallback) {
		if (v instanceof Number n)
			return n.intValue();
		if (v == null)
			return fallback;
		try {
			return Integer.parseInt(v.toString());
		} catch (NumberFormatException e) {
			return fallback;
		}
	}

	@SuppressWarnings("unchecked")
	private String extractMethod(Map<String, Object> body) {
		Object m = body.get("method"); // 예: "CARD" 또는 "간편결제"
		if (m != null)
			return String.valueOf(m);
		Object easyPay = body.get("easyPay");
		if (easyPay instanceof Map<?, ?> map) {
			Object provider = ((Map<String, Object>)map).get("provider");
			if (provider != null)
				return String.valueOf(provider);
		}
		return "UNKNOWN";
	}

	private String mapToUserMessage(String tossCode, String tossMsg) {
		switch (tossCode) {
			case "ALREADY_PROCESSED_PAYMENT":
				return "이미 결제가 처리되었습니다.";
			case "INVALID_API_KEY":
			case "UNAUTHORIZED_KEY":
				return "시스템 오류가 발생했습니다. 서비스 관리자에게 문의하세요.";
			case "PROVIDER_ERROR":
				return "결제사에 문제가 발생했습니다. 잠시 후 다시 시도해주세요.";
			case "REJECT_CARD_PAYMENT":
				return "카드 결제가 거절되었습니다. 카드 정보를 확인하세요.";
			case "FDS_ERROR":
				return "거래가 제한되었습니다. 고객센터에 문의하세요.";
			default:
				return "결제에 실패했습니다. 잠시 후 다시 시도하거나 고객센터에 문의하세요.";
		}
	}

	@Transactional
	public Payment requestPayment(ReservationId reservationId, Money amount, LocalDateTime requestedAt) {
		log.info("결제 요청 시작 - reservationId: {}, amount: {}", reservationId, amount);
		Payment payment = Payment.request(amount, reservationId, requestedAt);
		log.debug("결제 엔티티 생성 완료: {}", payment);

		// tossClient.confirmPayment(payment,1,1);
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

