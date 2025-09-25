package wisoft.nextframe.paymentgateway.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.paymentgateway.provider.ProviderRouter;

@Slf4j
@RestController
@RequestMapping("/api/v1/gateway")
@RequiredArgsConstructor
public class PaymentGatewayController {

	private final ProviderRouter router;

	@PostMapping("/payments/confirm")
	public ResponseEntity<ConfirmResponse> confirm(
		@RequestParam(defaultValue = "toss") String provider,
		@RequestBody ConfirmRequest request
	) {
		log.info("[Gateway] confirm 요청 수신 - provider={}, request={}", provider, request);
		ResponseEntity<ConfirmResponse> response = ResponseEntity.ok(router.confirm(provider, request));
		log.info("[Gateway] confirm 처리 완료 - response={}", response);
		return response;
	}

	public record ConfirmRequest(String paymentKey, String orderId, int amount) {
	}

	public record ConfirmResponse(boolean isSuccess, int totalAmount, String errorCode, String errorMessage) {
	}
}
