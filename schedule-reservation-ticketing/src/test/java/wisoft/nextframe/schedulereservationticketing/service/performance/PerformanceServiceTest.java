package wisoft.nextframe.schedulereservationticketing.service.performance;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityNotFoundException;
import wisoft.nextframe.schedulereservationticketing.dto.performance.PerformanceDetailResponse;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformanceGenre;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformancePricing;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformancePricingId;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformanceType;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.Stadium;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.StadiumSection;
import wisoft.nextframe.schedulereservationticketing.repository.performance.PerformancePricingRepository;
import wisoft.nextframe.schedulereservationticketing.repository.performance.PerformanceRepository;
import wisoft.nextframe.schedulereservationticketing.repository.schedule.ScheduleRepository;

@ExtendWith(MockitoExtension.class)
class PerformanceServiceTest {

	@InjectMocks
	private PerformanceService performanceService;
	@Mock
	private PerformanceRepository performanceRepository;
	@Mock
	private ScheduleRepository scheduleRepository;
	@Mock
	private PerformancePricingRepository performancePricingRepository;

	@Test
	@DisplayName("공연 상세 조회 성공 테스트")
	void getPerformanceDetail_Success() {
		// given
		final UUID performanceId = UUID.randomUUID();
		final Performance performance = createPerformance(performanceId, "오페라의 유령");
		final List<Schedule> schedules = List.of(createSchedule(performance));
		final List<PerformancePricing> pricings = List.of(createPricing(performance));

		given(performanceRepository.findById(performanceId)).willReturn(Optional.of(performance));
		given(scheduleRepository.findByPerformanceId(performanceId)).willReturn(schedules);
		given(performancePricingRepository.findByPerformanceId(performanceId)).willReturn(pricings);

		// when
		final PerformanceDetailResponse response = performanceService.getPerformanceDetail(performanceId);

		// then
		assertThat(response).isNotNull();
		assertThat(response.getName()).isEqualTo("오페라의 유령");
		assertThat(response.getPerformanceSchedules()).hasSize(1);
		assertThat(response.getSeatSectionPrices()).hasSize(1);
		assertThat(response.getSeatSectionPrices().getFirst().getSection()).isEqualTo("A");
	}

	@Test
	@DisplayName("공연 상세 조회 실패 테스트 - 존재하지 않는 공연 ID")
	void getPerformanceDetail_Fail_NotFound() {
		// given
		UUID nonExistentId = UUID.randomUUID();
		given(performanceRepository.findById(nonExistentId)).willReturn(Optional.empty());

		// when and then
		assertThatThrownBy(() -> performanceService.getPerformanceDetail(nonExistentId))
			.isInstanceOf(EntityNotFoundException.class);
	}

	private Performance createPerformance(UUID id, String name) {
		return Performance.builder()
			.id(id)
			.name(name)
			.type(PerformanceType.ROMANCE)
			.genre(PerformanceGenre.MUSICAL)
			.adultOnly(true)
			.runningTime(Duration.ofMinutes(150))
			.imageUrl("http://example.com/image.jpg")
			.description("전설적인 뮤지컬")
			.build();
	}

	private Schedule createSchedule(Performance performance) {
		Stadium stadium = Stadium.builder()
			.id(UUID.randomUUID())
			.name("부산문화회관")
			.address("부산광역시 남구")
			.build();

		return Schedule.builder()
			.id(UUID.randomUUID())
			.performance(performance)
			.stadium(stadium)
			.performanceDatetime(LocalDateTime.of(2025, 9, 10, 19, 0))
			.build();
	}

	private PerformancePricing createPricing(Performance performance) {
		StadiumSection section = StadiumSection.builder()
			.id(UUID.randomUUID())
			.section("A")
			.build();

		PerformancePricingId id = new PerformancePricingId(performance.getId(), section.getId());

		return PerformancePricing.builder()
			.id(id)
			.performance(performance)
			.stadiumSection(section)
			.price(150000)
			.build();
	}
}
