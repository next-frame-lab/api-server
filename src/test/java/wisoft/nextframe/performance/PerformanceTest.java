package wisoft.nextframe.performance;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import wisoft.nextframe.common.Money;
import wisoft.nextframe.domain.performance.Performance;
import wisoft.nextframe.domain.seat.Seat;
import wisoft.nextframe.domain.stadium.Stadium;
import wisoft.nextframe.util.PerformanceFixture;
import wisoft.nextframe.util.SeatFixture;
import wisoft.nextframe.util.StadiumFixture;

class PerformanceTest {

	@DisplayName("좌석 섹션별 추가 요금과 기본 가격을 합산하여 총 금액을 계산한다")
	@Test
	void calculateTotalPrice_returnsCorrectAmount() {
		// given
		final Map<String, Money> sectionPrice = Map.of("A", Money.of(20_000), "B", Money.of(0));
		final Stadium stadium = StadiumFixture.createWithSectionPrice(sectionPrice);
		final Money basePrice = Money.of(100_000);
		final Performance performance = PerformanceFixture.withBasePriceAndStadium(basePrice, stadium);

		final Set<Seat> seats = Set.of(
			SeatFixture.available("A", 1, 1),
			SeatFixture.available("B", 1, 2)
		);

		// when
		final Money totalPrice = performance.calculateTotalPrice(seats);

		// then
		assertThat(totalPrice).isEqualTo(Money.of((100_000 + 20_000) + 100_000));
	}
}
