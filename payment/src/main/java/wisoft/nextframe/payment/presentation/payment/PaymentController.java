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
import wisoft.nextframe.payment.domain.payment.Payment;
import wisoft.nextframe.payment.domain.payment.PaymentNotFoundException;
import wisoft.nextframe.payment.domain.payment.exception.PaymentConfirmedException;
import wisoft.nextframe.payment.presentation.payment.dto.PaymentConfirmRequest;
import wisoft.nextframe.payment.presentation.payment.dto.PaymentConfirmedData;

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
		try {
			// 서비스는 엔티티(혹은 비즈니스 결과)만 반환
			Payment payment = paymentService.confirmPayment(request);

			// 이미 승인된 결제라면 성공 응답
			if (payment.isSucceeded()) {
				PaymentConfirmedData approvedData = new PaymentConfirmedData(
					request.orderId(),
					request.amount()
				);
				return ResponseEntity.ok(ApiResponse.success(
					new PaymentConfirmedData(approvedData.reservationId(), approvedData.totalAmount())
				));
			}
			// 기타 특별한 성공 케이스가 있다면 분기 가능

			// 일반 성공 응답
			PaymentConfirmedData approvedData = new PaymentConfirmedData(
				request.orderId(),
				request.amount()
			);
			return ResponseEntity.ok(ApiResponse.success(
				new PaymentConfirmedData(approvedData.reservationId(), approvedData.totalAmount())
			));

		} catch (PaymentNotFoundException e) {
			return ResponseEntity.badRequest().body(
				ApiResponse.failed("존재하지 않는 결제입니다.")
			);
		} catch (PaymentConfirmedException e) {
			// 실패 코드 → 사용자 메시지로 변환
			String userMsg = mapToUserMessage(e.getErrorCode());
			return ResponseEntity.badRequest().body(
				ApiResponse.failed(userMsg)
			);
		} catch (Exception e) {
			// 예외 로그 남기고 일반 메시지 반환
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponse.failed("결제 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."));
		}
	}

	private String mapToUserMessage(String tossCode) {
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
}