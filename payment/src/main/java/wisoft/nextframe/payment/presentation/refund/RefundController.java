package wisoft.nextframe.payment.presentation.refund;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.payment.application.refund.RefundCancelFailedException;
import wisoft.nextframe.payment.application.refund.RefundService;
import wisoft.nextframe.payment.common.response.ApiResponse;
import wisoft.nextframe.payment.domain.payment.PaymentNotFoundException;
import wisoft.nextframe.payment.domain.payment.exception.RefundAlreadyExistsException;
import wisoft.nextframe.payment.domain.refund.Refund;
import wisoft.nextframe.payment.domain.refund.exception.NotRefundableException;
import wisoft.nextframe.payment.domain.refund.exception.RefundException;
import wisoft.nextframe.payment.presentation.refund.dto.RefundApprovedData;
import wisoft.nextframe.payment.presentation.refund.dto.RefundRequest;

@Slf4j
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
		try {
			Refund refund = refundService.refund(paymentId, request.reason());
			RefundApprovedData refundData = new RefundApprovedData(
				refund.getRefundId().getValue(),
				refund.getRefundedAmount().getValue().intValue(),
				refund.getPolicyStatus().name(),
				refund.getStatus().name()
			);
			return ResponseEntity.ok(ApiResponse.success(refundData));
		} catch (PaymentNotFoundException e) {
			return ResponseEntity.badRequest().body(
				ApiResponse.failed("존재하지 않는 결제입니다.")
			);
		} catch (RefundAlreadyExistsException e) {
			return ResponseEntity.badRequest().body(
				ApiResponse.failed("이미 환불된 결제입니다.")
			);
		} catch (NotRefundableException e) {
			return ResponseEntity.badRequest().body(
				ApiResponse.failed(e.getMessage())
			);
		} catch (RefundException e) {
			return ResponseEntity.badRequest().body(
				ApiResponse.failed(e.getMessage())
			);
		} catch (RefundCancelFailedException e) {
			return ResponseEntity.badRequest().body(
				ApiResponse.failed("결제사 환불 처리에 실패했습니다. 잠시 후 다시 시도해주세요.")
			);
		} catch (Exception e) {
			log.error("환불 처리 에러", e);
			throw e;
		}
	}
}
