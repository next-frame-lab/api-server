package wisoft.nextframe.payment.domain;

import static org.assertj.core.api.Assertions.*;
import static wisoft.nextframe.payment.domain.fixture.TestRefundFactory.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import wisoft.nextframe.payment.common.Money;
import wisoft.nextframe.payment.domain.refund.Refund;
import wisoft.nextframe.payment.domain.refund.RefundPolicyStatus;

public class RefundPolicyStatusTest {

	@Test
	@DisplayName("공연 7일 전이면 전액 환불 가능하다")
	void fullIssue_before7Days() {
		Refund refund = refundFull();
		BigDecimal expected = BigDecimal.valueOf(10_000);

		Money result = RefundPolicyStatus.REFUND_FULL.calculateRefundAmount(defaultAmount());

		assertThat(refund.getPolicyStatus()).isEqualTo(RefundPolicyStatus.REFUND_FULL);
		assertThat(result.getValue()).isEqualByComparingTo(expected);
	}

	@Test
	@DisplayName("공연 3일 전이면 80% 환불 가능하다")
	void fullIssue_before3Days() {
		Refund refund = refund80percent();
		BigDecimal expected = BigDecimal.valueOf(8_000);

		Money result = RefundPolicyStatus.REFUND_80_PERCENT.calculateRefundAmount(defaultAmount());

		assertThat(refund.getPolicyStatus()).isEqualTo(RefundPolicyStatus.REFUND_80_PERCENT);
		assertThat(result.getValue()).isEqualByComparingTo(expected);
	}

	@Test
	@DisplayName("공연 1일 전이면 60% 환불 가능하다")
	void partialIssue_before1Day() {
		Refund refund = refund60percent();
		BigDecimal expected = BigDecimal.valueOf(6_000);

		Money result = RefundPolicyStatus.REFUND_60_PERCENT.calculateRefundAmount(defaultAmount());

		assertThat(refund.getPolicyStatus()).isEqualTo(RefundPolicyStatus.REFUND_60_PERCENT);
		assertThat(result.getValue()).isEqualByComparingTo(expected);
	}

	@Test
	@DisplayName("공연 24시간 이내에는 환불 불가능하다")
	void refundDeny_between1HAnd24H() {
		Refund refund = refundDeny();
		Money refundedAmount = refund.getRefundedAmount();

		assertThat(refund.getPolicyStatus()).isEqualTo(RefundPolicyStatus.NON_REFUNDABLE);
		assertThat(refundedAmount.getValue()).isEqualByComparingTo(BigDecimal.ZERO);
	}

	@Test
	@DisplayName("공연 시작 1시간 이내에는 환불 시도 시 예외가 발생한다")
	void issueDeny_within1Hour() {

		Refund refund = refundDeny();
		assertThatThrownBy(() -> {
			refund.reject();
			refund.validateRefundable();
		}).isInstanceOf(RuntimeException.class)
			.hasMessageContaining("공연 시작 1시간 전에는 환불할 수 없습니다.");

	}

}
