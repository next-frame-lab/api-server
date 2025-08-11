package wisoft.nextframe.payment.presentation;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import wisoft.nextframe.common.Money;
import wisoft.nextframe.common.response.ApiResponse;
import wisoft.nextframe.payment.application.PaymentService;
import wisoft.nextframe.payment.domain.payment.Payment;
import wisoft.nextframe.payment.domain.payment.PaymentId;
import wisoft.nextframe.payment.presentation.dto.PaymentApprovalRequest;
import wisoft.nextframe.payment.presentation.dto.PaymentFailureResponse;
import wisoft.nextframe.payment.presentation.dto.PaymentRequest;
import wisoft.nextframe.payment.presentation.dto.PaymentSuccessResponse;
import wisoft.nextframe.reservation.domain.ReservationId;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentService paymentService;

	/**
	 * 예약에 대한 결제 요청
	 */
	@PostMapping("/reservations/{id}/payment")
	public ResponseEntity<ApiResponse<?>> requestPayment(
		@PathVariable("id") UUID reservationId,
		@RequestBody PaymentRequest request
	) {
		try {
			Payment payment = paymentService.requestPayment(
				ReservationId.of(reservationId),
				Money.of(request.amount()),
				LocalDateTime.now()
			);
			return ResponseEntity.ok(
				ApiResponse.success(new PaymentSuccessResponse(payment.getId().getValue())));
		} catch (Exception e) {
			// todo 외부 결제사 응답 파싱 필요
			return ResponseEntity.ok(
				ApiResponse.failed(new PaymentFailureResponse("외부 결제 승인 실패 - 잔액 부족"))
			);
		}
	}

	/**
	 * 외부 결제 시스템에서 결제 성공 후 호출하는 콜백
	 */
	@PostMapping("/payment/callback/approve")
	public ResponseEntity<Void> approve(@RequestBody PaymentApprovalRequest request) {
		// 예: 외부 결제사에서 결제 ID를 전달해줄 때
		paymentService.approvePayment(PaymentId.of(request.paymentId()));

		return ResponseEntity.ok().build();
	}
}
