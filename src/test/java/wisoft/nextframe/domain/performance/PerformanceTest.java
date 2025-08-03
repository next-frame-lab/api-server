package wisoft.nextframe.domain.performance;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import wisoft.nextframe.common.Money;
import wisoft.nextframe.domain.performance.Performance;
import wisoft.nextframe.domain.performance.exception.SeatSectionNotDefinedException;
import wisoft.nextframe.domain.seat.Seat;
import wisoft.nextframe.util.PerformanceFixture;
import wisoft.nextframe.util.SeatFixture;

class PerformanceTest {

	@DisplayName("좌석 섹션별 추가 요금과 기본 가격을 합산하여 총 금액을 계산한다")
	@Test
	void calculateTotalPrice_returnsCorrectAmount() {
		// given
		final Map<String, Money> sectionPrice = Map.of(
			"A", Money.of(120_000),
			"B", Money.of(100_000)
		);
		final Performance performance = PerformanceFixture.withSectionPrice(sectionPrice);
		final Set<Seat> seats = Set.of(
			SeatFixture.available("A", 1, 1),
			SeatFixture.available("B", 1, 2)
		);

		// when
		final Money totalPrice = performance.calculateTotalPrice(seats);

		// then
		assertThat(totalPrice).isEqualTo(Money.of( 120_000 + 100_000));
	}

	@DisplayName("정의되지 않은 구역의 좌석이 포함되면 예외를 발생시킨다")
	@Test
	void calculateTotalPrice_throwsException_whenSectionIsNotDefined() {
		// given
		final Map<String, Money> sectionPrice = Map.of(
			"A", Money.of(120_000),
			"B", Money.of(100_000)
		);
		final Performance performance = PerformanceFixture.withSectionPrice(sectionPrice);
		final Set<Seat> seats = Set.of(SeatFixture.available("Z", 1, 2)); // 정의되지 않은 공연장 구역

		// when and then
		assertThatThrownBy(() -> performance.calculateTotalPrice(seats))
			.isInstanceOf(SeatSectionNotDefinedException.class);
	}
}
