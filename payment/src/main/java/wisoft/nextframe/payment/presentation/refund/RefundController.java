package wisoft.nextframe.payment.presentation.refund;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import wisoft.nextframe.payment.application.refund.RefundService;
import wisoft.nextframe.payment.common.response.ApiResponse;
import wisoft.nextframe.payment.domain.refund.Refund;
import wisoft.nextframe.payment.presentation.refund.dto.RefundApprovedData;
import wisoft.nextframe.payment.presentation.refund.dto.RefundRequest;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class RefundController {

	private final RefundService refundService;

	@PostMapping("/{paymentId}/refund")
	public ResponseEntity<ApiResponse<?>> refund(
		@PathVariable UUID paymentId,
		@RequestBody RefundRequest request
	) {
		Refund refund = refundService.refund(paymentId, request.reason());
		RefundApprovedData refundData = new RefundApprovedData(
			refund.getRefundId().getValue(),
			refund.getRefundedAmount().getValue().intValue(),
			refund.getPolicyStatus().name(),
			refund.getStatus().name()
		);
		return ResponseEntity.ok(ApiResponse.success(refundData));
	}
}
