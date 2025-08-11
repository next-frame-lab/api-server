package wisoft.nextframe.schedulereservationticketing.common;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import wisoft.nextframe.common.exception.InvalidAmountException;

class MoneyTest {

	@Test
	@DisplayName("정상적인 금액으로 Money 객체를 생성할 수 있다")
	void createMoneyWithValidAmount() {
		Money money = Money.of(10000L);
		assertThat(money.getValue()).isEqualByComparingTo(BigDecimal.valueOf(10000L));
	}

	@Test
	@DisplayName("음수로 Money를 생성하면 예외가 발생한다")
	void createMoneyWithZeroOrNegativeAmountThrowsException() {
		assertThatThrownBy(() -> Money.of(-100L))
			.isInstanceOf(InvalidAmountException.class)
			.hasMessageContaining("금액은 음수일 수 없습니다.");
	}

	@Test
	@DisplayName("두 Money 비교: 크기 비교")
	void compareMoney() {
		Money m1 = Money.of(10000L);
		Money m2 = Money.of(5000L);
		Money m3 = Money.of(10000L);

		assertThat(m1.isGreaterThan(m2)).isTrue();
		assertThat(m2.isGreaterThan(m1)).isFalse();
		assertThat(m1.isGreaterThan(m3)).isFalse();

		assertThat(m1.isGreaterThanOrEqual(m3)).isTrue();
		assertThat(m2.isGreaterThanOrEqual(m1)).isFalse();
	}

	@Test
	@DisplayName("양수 여부 확인")
	void checkPositive() {
		Money money = Money.of(1L);
		assertThat(money.isPositive()).isTrue();
	}

	@Test
	@DisplayName("금액 곱셈 계산")
	void multiplyMoney() {
		Money original = Money.of(10000L);
		Money result = original.multiply(BigDecimal.valueOf(0.5));
		assertThat(result.getValue()).isEqualByComparingTo(BigDecimal.valueOf(5000));
	}
}
