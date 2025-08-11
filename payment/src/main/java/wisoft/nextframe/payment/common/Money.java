package wisoft.nextframe.payment.common;

import java.math.BigDecimal;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import wisoft.nextframe.schedulereservationticketing.common.exception.InvalidAmountException;

@EqualsAndHashCode
@ToString
@Getter
public class Money {

	private final BigDecimal value;

	public static final Money ZERO = new Money(BigDecimal.ZERO);

	private Money(BigDecimal value) {
		if (value.compareTo(BigDecimal.ZERO) < 0) {
			throw new InvalidAmountException();
		}
		this.value = value;
	}

	public static Money of(BigDecimal amount) {
		return new Money(amount);
	}

	public static Money of(long amount) {
		return new Money(BigDecimal.valueOf(amount));
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

	public Money plus(Money other) {
		return new Money(this.value.add(other.value));
	}
}
