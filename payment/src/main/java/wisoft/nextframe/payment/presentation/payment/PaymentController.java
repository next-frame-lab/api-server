package wisoft.nextframe.payment.presentation.payment;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import wisoft.nextframe.payment.application.payment.PaymentService;
import wisoft.nextframe.payment.common.response.ApiResponse;
import wisoft.nextframe.payment.presentation.payment.dto.PaymentConfirmRequest;
import wisoft.nextframe.payment.presentation.payment.dto.PaymentConfirmedData;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentService paymentService;

	@PostMapping("/confirm")
	public ResponseEntity<ApiResponse<?>> confirm(@RequestBody PaymentConfirmRequest request) {
		paymentService.confirmPayment(request);

		PaymentConfirmedData confirmedData = new PaymentConfirmedData(
			request.orderId(),
			request.amount()
		);

		return ResponseEntity.ok(ApiResponse.success(confirmedData));
	}
}
