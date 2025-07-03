package wisoft.nextframe.refund;

import static org.assertj.core.api.Assertions.*;
import static wisoft.nextframe.refund.TestRefundFactory.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class RefundTest {

	@Test
	@DisplayName("환불 요청 시 상태는 REQUESTED가 된다")
	void initialRequestedStatus() {
		Refund refund = requested();

		// then
		assertThat(refund.getStatus()).isEqualTo(RefundStatus.REQUESTED);
	}

	@Test
	@DisplayName("환불 승인 시 상태는 APPROVED가 된다")
	void approveRefund_changesStatusToApproved() {
		Refund refund = requested();

		refund.approve();

		// then
		assertThat(refund.getStatus()).isEqualTo(RefundStatus.APPROVED);
	}

	@Test
	@DisplayName("환불 거절 시 상태는 REJECTED가 된다")
	void rejectRefund_changesStatusToRejected() {
		LocalDateTime requestAt = LocalDateTime.of(2025, 7, 8, 10, 0);
		LocalDateTime contentAt = LocalDateTime.of(2025, 7, 10, 20, 0);

		Refund refund = new Refund(requestAt, contentAt, BigDecimal.valueOf(10000));

		refund.reject();

		assertThat(refund.getStatus()).isEqualTo(RefundStatus.REJECTED);
	}

	@Test
	@DisplayName("승인 상태에서 거절을 시도하면 예외가 발생한다")
	void cannotRejectAfterApproved() {
		Refund refund = new Refund(LocalDateTime.now(), LocalDateTime.now().plusDays(2), BigDecimal.valueOf(10000));
		refund.approve();

		assertThatThrownBy(refund::reject)
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("거절은 REQUESTED 상태에서만 가능합니다.");
	}

	@Test
	@DisplayName("결제 완료된 사용자에 대해 환불 요청 시 정책에 따라 환불 상태가 결정된다:전액환불정책경우")
	void refundApproved_whenPaidAndFullyRefund() {
		Refund refund = fullRefund();
		refund.approve();

		// then
		assertThat(refund.getStatus()).isEqualTo(RefundStatus.APPROVED);
		assertThat(refund.getPolicyStatus()).isEqualTo(RefundPolicyStatus.FULL_REFUND);
	}

	@Test
	@DisplayName("결제 완료된 사용자에 대해 환불 요청 시 정책과 금액에 따라 환불이 승인된다")
	void approveRefundWithCorrectAmountAndPolicy() {

		// when
		Refund refund = partialRefund();
		refund.approve();

		// then
		assertThat(refund.getStatus()).isEqualTo(RefundStatus.APPROVED);
		assertThat(refund.getPolicyStatus()).isEqualTo(RefundPolicyStatus.PARTIAL_REFUND);
		assertThat(refund.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(5000));
	}

}