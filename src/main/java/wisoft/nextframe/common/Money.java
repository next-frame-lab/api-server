package wisoft.nextframe.common;

import java.math.BigDecimal;

import lombok.Getter;

@Getter
public class Money {

	private final BigDecimal value;

	private Money(BigDecimal value) {
		this.value = value;
	}

	public static Money of(long amount) {
		BigDecimal decimalAmount = BigDecimal.valueOf(amount);

		if (decimalAmount == null) {
			throw new IllegalArgumentException("금액은 null일 수 없습니다.");
		}
		if (decimalAmount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("결제 금액은 0보다 커야 합니다.");
		}
		return new Money(decimalAmount);
	}

	public boolean isGreaterThan(Money other) {
		return this.value.compareTo(other.value) > 0;
	}

	public boolean isGreaterThanOrEqual(Money other) {
		return this.value.compareTo(other.value) >= 0;
	}

	public boolean isPositive() {
		return this.value.compareTo(BigDecimal.ZERO) > 0;
	}

	public Money multiply(BigDecimal ratio) {
		return new Money(this.value.multiply(ratio));
	}
}
