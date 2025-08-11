package wisoft.nextframe.refund;

import static org.assertj.core.api.Assertions.*;
import static wisoft.nextframe.payment.fixture.TestRefundFactory.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import wisoft.nextframe.payment.refund.Refund;

public class RefundServiceTest {

	@Test
	@DisplayName("거절 상태에서 승인을 시도하면 예외가 발생한다")
	void cannotApproveAfterRejected() {
		Refund refund = refundDeny();
		refund.reject();

		assertThatThrownBy(refund::approve)
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("승인은 REQUESTED 상태에서만 가능합니다.");
	}

}
