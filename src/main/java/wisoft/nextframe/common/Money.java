package wisoft.nextframe.common;

import java.math.BigDecimal;

import lombok.Getter;
import wisoft.nextframe.common.exception.InvalidAmountException;

@Getter
public class Money {

	private final BigDecimal value;

	private Money(BigDecimal value) {
		this.value = value;
	}

	public static Money of(long amount) {
		BigDecimal decimalAmount = BigDecimal.valueOf(amount);

		if (decimalAmount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new InvalidAmountException();
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
