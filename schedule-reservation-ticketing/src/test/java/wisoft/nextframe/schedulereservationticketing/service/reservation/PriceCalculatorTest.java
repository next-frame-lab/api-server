package wisoft.nextframe.schedulereservationticketing.service.reservation;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import wisoft.nextframe.schedulereservationticketing.builder.PerformanceBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.ScheduleBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.StadiumSectionBuilder;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformancePricing;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformancePricingId;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.StadiumSection;
import wisoft.nextframe.schedulereservationticketing.repository.performance.PerformancePricingRepository;

@ExtendWith(MockitoExtension.class)
class PriceCalculatorTest {

	@Mock
	private PerformancePricingRepository performancePricingRepository;

	@InjectMocks
	private PriceCalculator priceCalculator;

	@Test
	@DisplayName("좌석들의 총 가격을 정확히 계산한다")
	void calculateTotalPrice_Success() {
		// given
		final Schedule schedule = new ScheduleBuilder().withId(UUID.randomUUID()).build();

		final StadiumSection sectionA = new StadiumSectionBuilder().withId(UUID.randomUUID()).build();
		final StadiumSection sectionB = new StadiumSectionBuilder().withId(UUID.randomUUID()).build();
		final StadiumSection sectionC = new StadiumSectionBuilder().withId(UUID.randomUUID()).build();

		final SeatDefinition seatA = createSeat(sectionA);
		final SeatDefinition seatB = createSeat(sectionB);
		final SeatDefinition seatC = createSeat(sectionC);
		final List<SeatDefinition> seats = List.of(seatA, seatB, seatC);

		// 2. Mock Repository가 반환할 가격 정보를 설정합니다.
		final List<PerformancePricing> performancePricings = List.of(
			createPerformancePricing(schedule, sectionA, 70000), // A등급 가격
			createPerformancePricing(schedule, sectionB, 80000), // B등급 가격
			createPerformancePricing(schedule, sectionC, 90000)  // C등급 가격
		);

		List<UUID> expectedSectionIds = List.of(sectionA.getId(), sectionB.getId(), sectionC.getId());
		when(performancePricingRepository.findByScheduleIdAndSectionIds(schedule.getId(), expectedSectionIds))
			.thenReturn(performancePricings);

		// when
		int totalPrice = priceCalculator.calculateTotalPrice(schedule, seats);

		// then
		int expectedTotalPrice = 70000 + 80000 + 90000;
		assertThat(totalPrice).isEqualTo(expectedTotalPrice);
	}

	private SeatDefinition createSeat(StadiumSection section) {
		return SeatDefinition.builder()
			.id(UUID.randomUUID())
			.rowNo(1)
			.columnNo(1)
			.stadiumSection(section)
			.build();
	}

	private PerformancePricing createPerformancePricing(Schedule schedule, StadiumSection section, int price) {
		return PerformancePricing.builder()
			.schedule(schedule)
			.stadiumSection(section)
			.price(price)
			.build();
	}
}