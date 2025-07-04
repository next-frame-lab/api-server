package wisoft.nextframe.payment.refund;

import static org.assertj.core.api.Assertions.*;
import static wisoft.nextframe.payment.refund.TestRefundFactory.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import wisoft.nextframe.common.Money;

public class RefundPolicyStatusTest {

	@Test
	@DisplayName("공연 24시간 전이면 전액 환불 가능하다")
	void fullIssue_before24Hours() {

		Refund refund = fullRefund();

		assertThat(refund.getPolicyStatus()).isEqualTo(RefundPolicyStatus.FULL_REFUND);
	}

	@Test
	@DisplayName("공연 1시간 ~ 24시간 사이 요청 시 50% 환불 처리된다")
	void partialIssue_between1HAnd24H() {
		Refund refund = partialRefund();

		assertThat(refund.getPolicyStatus()).isEqualTo(RefundPolicyStatus.PARTIAL_REFUND);
	}

	@Test
	@DisplayName("부분 환불 정책인 경우 결제 금액의 50% 환불한다")
	void returnsPartialAmount_whenPartialIssue() {

		BigDecimal expected = BigDecimal.valueOf(5_000);

		Money result = RefundPolicyStatus.PARTIAL_REFUND.calculateRefund(defaultAmount());

		assertThat(result.getValue()).isEqualByComparingTo(expected);
	}

	@Test
	@DisplayName("공연 시작 1시간 이내에는 환불 시도 시 예외가 발생한다")
	void issueDeny_within1Hour() {

		assertThatThrownBy(() -> denyRefund())
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("공연 시작 1시간 전에는 환불할 수 없습니다.");
	}

}
