package wisoft.nextframe.payment.presentation.payment;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import wisoft.nextframe.payment.application.payment.PaymentService;
import wisoft.nextframe.payment.common.response.ApiResponse;
import wisoft.nextframe.payment.presentation.payment.dto.PaymentConfirmRequest;
import wisoft.nextframe.payment.presentation.payment.dto.PaymentConfirmResponse;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentService paymentService;

	/**
	 * POST /api/v1/payments/confirm
	 * <p>
	 * 클라이언트 버전 헤더에 따라 legacy 모드로 응답을 보낼지 결정함
	 * legacy 클라이언트 : 실패시 data에 메시지 담아서 보냄 (기존 스펙)
	 * modern 클라이언트 : 실패시 message 필드에 메시지 담아서 보냄
	 */
	@PostMapping("/confirm")
	public ResponseEntity<ApiResponse<?>> confirm(@RequestBody PaymentConfirmRequest request) {
		PaymentConfirmResponse resp = paymentService.confirmPayment(request);

		if (resp.isSuccess()) {
			return ResponseEntity.ok(ApiResponse.success(resp.data()));
		}
		String userMsg = resp.message() == null ? "결제에 실패했습니다." : resp.message();
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(ApiResponse.failed(userMsg));
	}
}