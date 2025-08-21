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
import wisoft.nextframe.schedulereservationticketing.builder.StadiumSectionBuilder;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformancePricing;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformancePricingId;
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
		final UUID performanceId = UUID.randomUUID();
		final UUID sectionIdA = UUID.randomUUID();
		final UUID sectionIdB = UUID.randomUUID();
		final UUID sectionIdC = UUID.randomUUID();

		final Performance performance = new PerformanceBuilder().withId(performanceId).build();
		final SeatDefinition seatA = createSeat(sectionIdA);
		final SeatDefinition seatB = createSeat(sectionIdB);
		final SeatDefinition seatC = createSeat(sectionIdC);
		final List<SeatDefinition> seats = List.of(seatA, seatB, seatC);

		// 2. Mock Repository가 반환할 가격 정보를 설정합니다.
		final List<PerformancePricing> performancePricings = List.of(
			createPerformancePricing(performanceId, sectionIdA, 70000), // A등급 가격
			createPerformancePricing(performanceId, sectionIdB, 80000), // B등급 가격
			createPerformancePricing(performanceId, sectionIdC, 90000)  // C등급 가격
		);

		List<UUID> expectedSectionIds = List.of(sectionIdA, sectionIdB, sectionIdC);

		when(performancePricingRepository.findById_PerformanceIdAndId_StadiumSectionIdIn(performanceId, expectedSectionIds))
			.thenReturn(performancePricings);

		// when
		int totalPrice = priceCalculator.calculateTotalPrice(performance, seats);

		// then
		int expectedTotalPrice = 70000 + 80000 + 90000;
		assertThat(totalPrice).isEqualTo(expectedTotalPrice);
	}

	private SeatDefinition createSeat(UUID sectionId) {
		final StadiumSection section = new StadiumSectionBuilder().withId(sectionId).build();
		return SeatDefinition.builder().id(UUID.randomUUID()).rowNo(1).columnNo(1).stadiumSection(section).build();
	}

	private PerformancePricing createPerformancePricing(UUID performanceId, UUID sectionId, int price) {
		PerformancePricingId id = new PerformancePricingId(performanceId, sectionId);
		return new PerformancePricing(id, null, null, price);
	}
}