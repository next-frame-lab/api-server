package wisoft.nextframe.reservation;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import wisoft.nextframe.performance.Performance;
import wisoft.nextframe.seat.Seat;
import wisoft.nextframe.stadium.Stadium;
import wisoft.nextframe.util.PerformanceFixture;
import wisoft.nextframe.util.SeatFixture;
import wisoft.nextframe.util.StadiumFixture;

class TotalPricePolicyTest {

	private final TotalPricePolicy policy = new TotalPricePolicy();

	@DisplayName("공연 기본 가격과 공연장 좌석별 추가 가격을 합산하여 총 결제 금액을 계산한다")
	@Test
	void calculate_totalPrice_withBasePriceAndSectionPrice() {
		// given
		final Map<String, Integer> sectionPrice = Map.of("A", 20_000, "B", 0);
		final Stadium stadium = StadiumFixture.createWithSectionPrice(sectionPrice);
		int basePrice = 130_000;
		final Performance performance = PerformanceFixture.withBasePriceAndStadium(basePrice, stadium);

		final Set<Seat> seats = Set.of(
			SeatFixture.available("A", 1, 1),
			SeatFixture.available("B", 2, 1)
		);

		// when
		final int totalPrice = policy.calculate(performance, seats);

		// then
		assertThat(totalPrice).isEqualTo((130_000 + 20_000) + 130_000);
	}
}
