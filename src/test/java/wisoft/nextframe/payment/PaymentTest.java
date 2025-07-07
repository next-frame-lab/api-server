package wisoft.nextframe.payment;

import static org.assertj.core.api.Assertions.*;
import static wisoft.nextframe.payment.TestPaymentFactory.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import wisoft.nextframe.common.Money;
import wisoft.nextframe.common.exception.InvalidAmountException;
import wisoft.nextframe.payment.exception.MissingReservationException;
import wisoft.nextframe.payment.exception.PaymentAlreadySucceededException;
import wisoft.nextframe.payment.exception.TooLargeAmountException;

@DisplayName("Payment 도메인 단위 테스트")
class PaymentTest {

	@Test
	@DisplayName("결제 요청 시 상태는 REQUESTED가 된다")
	void createPayment_requestedStatus() {
		Payment payment = requested();
		assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REQUESTED);
	}

	@Test
	@DisplayName("결제 금액이 0 이하일 경우 예외가 발생한다")
	void denyPayment_amountNonPositive() {
		assertThatThrownBy(() -> requestedWithAmount(Money.of( -10000)))
			.isInstanceOf(InvalidAmountException.class)
			.hasMessage("결제 금액은 0보다 커야 합니다.");

	}

	@Test
	@DisplayName("결제 금액이 최대값(1천만 원 이상)을 초과하면 예외가 발생한다")
	void denyPayment_amountExceedsLimit() {
		assertThatThrownBy(() -> requestedWithAmount(Money.of( 10_000_001)))
			.isInstanceOf(TooLargeAmountException.class)
			.hasMessage("결제 금액은 최대 1천만 원 미만이어야 합니다.");
	}

	@Test
	@DisplayName("예매 정보가 null이면 결제 생성 시 예외가 발생한다")
	void denyPayment_reservationNull() {
		assertThatThrownBy(() -> requestedWithReservationId(null))
			.isInstanceOf(MissingReservationException.class)
			.hasMessage("결제를 위해서는 예매 정보가 필요합니다.");
	}

	@Test
	@DisplayName("결제 요청 후 10분이 지나면 상태가 FAILED로 변경된다")
	void expirePayment_over10Minutes() {
		LocalDateTime expired = LocalDateTime.now().minusMinutes(10).minusSeconds(1);
		Payment payment = requestedAt(expired);

		payment.checkTimeout();

		assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
	}

	@Nested
	@DisplayName("결제 성공 상태일 때")
	class WhenSucceeded {

		@Test
		@DisplayName("이미 성공한 결제건에 대해 다시 성공 처리를 시도하면 예외가 발생한다")
		void denySucceed_alreadySucceededOrPaid() {
			Payment payment = requested();
			payment.markAsSucceed();

			assertThatThrownBy(payment::markAsSucceed)
				.isInstanceOf(PaymentAlreadySucceededException.class)
				.hasMessage("이미 결제 성공 처리된 건입니다.");
		}

		@Test
		@DisplayName("결제를 확정하면 상태가 PAID가 된다")
		void confirmPayment_paidStatus() {
			Payment payment = requested();
			payment.markAsSucceed();
			payment.confirm();

			assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
		}
	}

	@Nested
	@DisplayName("결제 요청 완료 상태일 때")
	class WhenRequested {

		@Test
		@DisplayName("결제 성공 시 상태는 SUCCEEDED가 된다")
		void succeedPayment_succeededStatus() {
			Payment payment = requested();
			payment.markAsSucceed();
			assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
		}

		@Test
		@DisplayName("결제 실패 시 상태는 FAILED이 된다.")
		void failPayment_failStatus() {
			Payment payment = requested();
			payment.fail();
			assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
		}
	}

}