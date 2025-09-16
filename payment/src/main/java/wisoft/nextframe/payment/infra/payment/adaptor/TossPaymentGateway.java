package wisoft.nextframe.payment.infra.payment.adaptor;

import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.payment.application.payment.port.output.PaymentGateway;
import wisoft.nextframe.payment.application.payment.port.output.PaymentClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class TossPaymentGateway implements PaymentGateway {

	private final PaymentClient tossClient;

	public PaymentConfirmResult confirmPayment(String paymentKey, String orderId, int amount) {
		try {
			// 1. TossPaymentsClient를 통해 실제 외부 API 호출
			Map<String, Object> body = tossClient.confirmPayment(paymentKey, orderId, amount);

			// 2. 외부 API 응답을 파싱하여 도메인 친화적인 DTO로 변환
			String paymentStatus = String.valueOf(body.getOrDefault("status", ""));
			Object approvedAt = body.get("approvedAt");

			if ("DONE".equals(paymentStatus) && approvedAt != null) {
				int totalAmount = extractInt(body.get("totalAmount"), amount);
				return new PaymentConfirmResult(true, totalAmount, null, null);
			} else {
				String tossCode = String.valueOf(body.getOrDefault("code", "TOSS_ERROR"));
				String tossMsg = String.valueOf(body.getOrDefault("message", "토스 승인 실패"));
				log.warn("Toss confirm failed. paymentStatus={}, tossCode={}, tossMsg={}, body={}", paymentStatus, tossCode,
					tossMsg, body);
				return new PaymentConfirmResult(false, 0, tossCode, tossMsg);
			}
		} catch (Exception ex) {
			// 5. 네트워크/타임아웃/예외 처리
			String msg = ex.getMessage() != null ? ex.getMessage() : "NETWORK_ERROR";
			log.error("Toss confirmPayment exception", ex);
			return new PaymentConfirmResult(false, 0, "NETWORK_ERROR", "네트워크 오류: " + msg);
		}
	}

	// 외부 API 응답 파싱
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

}
