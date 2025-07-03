package wisoft.nextframe.refund;

import static org.assertj.core.api.Assertions.*;
import static wisoft.nextframe.payment.TestPaymentFactory.*;
import static wisoft.nextframe.refund.TestRefundFactory.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class RefundServiceTest {

	@Test
	@DisplayName("거절 상태에서 승인을 시도하면 예외가 발생한다")
	void cannotApproveAfterRejected() {
		LocalDateTime requestTime = contentStartTime().minusMinutes(30); // 공연 30분 전

		assertThatThrownBy(() -> new RefundService().refund(paid(), requestTime, contentStartTime()))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("공연 시작 1시간 전에는 환불할 수 없습니다.");
	}


}
