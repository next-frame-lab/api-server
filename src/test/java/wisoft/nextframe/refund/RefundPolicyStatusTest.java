package wisoft.nextframe.refund;

import static org.assertj.core.api.Assertions.*;
import static wisoft.nextframe.payment.TestPaymentFactory.*;
import static wisoft.nextframe.refund.TestRefundFactory.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class RefundPolicyStatusTest {

	@Test
	@DisplayName("공연 24시간 전이면 전액 환불 가능하다")
	void fullRefund_before24Hours() {

		Refund refund = fullRefund();

		assertThat(refund.getPolicyStatus()).isEqualTo(RefundPolicyStatus.FULL_REFUND);
	}

	@Test
	@DisplayName("전액 환불 정책인 경우 결제 금액 전부를 환불한다")
	void returnsFullAmount_whenFullRefund() {

		BigDecimal result = RefundPolicyStatus.FULL_REFUND.calculateRefund(defaultAmount());

		assertThat(result).isEqualByComparingTo(defaultAmount());
	}

	@Test
	@DisplayName("공연 1시간 ~ 24시간 사이 요청 시 50% 환불 처리된다")
	void partialRefund_between1hAnd24h() {
		Refund refund = partialRefund();

		assertThat(refund.getPolicyStatus()).isEqualTo(RefundPolicyStatus.PARTIAL_REFUND);
	}

	@Test
	@DisplayName("부분 환불 정책인 경우 결제 금액의 50% 환불한다")
	void returnsPartialAmount_whenPartialRefund() {

		BigDecimal expected = BigDecimal.valueOf(5_000);

		BigDecimal result = RefundPolicyStatus.PARTIAL_REFUND.calculateRefund(defaultAmount());

		assertThat(result).isEqualByComparingTo(expected);
	}

	@Test
	@DisplayName("공연 시작 1시간 이내에는 환불 시도 시 예외가 발생한다")
	void refundDeny_within1Hour() {
		LocalDateTime requestTime = contentStartTime().minusMinutes(30); // 공연 30분 전

		assertThatThrownBy(() -> Refund.refund(paid(), requestTime, contentStartTime()))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("공연 시작 1시간 전에는 환불할 수 없습니다.");
	}

}
