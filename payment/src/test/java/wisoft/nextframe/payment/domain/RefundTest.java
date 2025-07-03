package wisoft.nextframe.payment.domain;

import static org.assertj.core.api.Assertions.*;
import static wisoft.nextframe.payment.domain.fixture.TestRefundFactory.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import wisoft.nextframe.payment.domain.refund.Refund;
import wisoft.nextframe.payment.domain.refund.RefundPolicyStatus;
import wisoft.nextframe.payment.domain.refund.RefundStatus;

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
	void approveIssue_changesStatusToApproved() {
		Refund refund = requested();

		refund.approve();

		// then
		assertThat(refund.getStatus()).isEqualTo(RefundStatus.APPROVED);
	}

	@Test
	@DisplayName("환불 거절 시 상태는 REJECTED가 된다")
	void rejectIssue_changesStatusToRejected() {

		Refund refund = requested();


		refund.reject();

		assertThat(refund.getStatus()).isEqualTo(RefundStatus.REJECTED);
	}

	@Test
	@DisplayName("승인 상태에서 거절을 시도하면 예외가 발생한다")
	void cannotRejectAfterApproved() {
		Refund refund = requested();
		refund.approve();

		assertThatThrownBy(refund::reject)
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("거절은 REQUESTED 상태에서만 가능합니다.");
	}

	@Test
	@DisplayName("결제 완료된 사용자에 대해 환불 요청 시 정책에 따라 환불 상태가 결정된다:전액환불정책경우")
	void refundApproved_whenPaidAndFullyIssue() {
		Refund refund = refundFull();
		refund.approve();

		// then
		assertThat(refund.getStatus()).isEqualTo(RefundStatus.APPROVED);
		assertThat(refund.getPolicyStatus()).isEqualTo(RefundPolicyStatus.REFUND_FULL);
	}

	@Test
	@DisplayName("결제 완료된 사용자에 대해 환불 요청 시 정책과 금액에 따라 환불이 승인된다")
	void approveIssueWithCorrectAmountAndPolicy() {

		// when
		Refund refund = refund60percent();
		refund.approve();

		// then
		assertThat(refund.getStatus()).isEqualTo(RefundStatus.APPROVED);
		assertThat(refund.getPolicyStatus()).isEqualTo(RefundPolicyStatus.REFUND_60_PERCENT);
		assertThat(refund.getRefundedAmount().getValue()).isEqualByComparingTo("6000");

	}

}