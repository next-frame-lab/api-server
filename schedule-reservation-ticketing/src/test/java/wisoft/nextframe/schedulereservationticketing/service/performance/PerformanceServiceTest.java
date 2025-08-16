package wisoft.nextframe.schedulereservationticketing.service.performance;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.sql.Date;
import java.time.Duration;
import java.time.LocalDate;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import jakarta.persistence.EntityNotFoundException;
import wisoft.nextframe.schedulereservationticketing.dto.performance.response.PerformanceDetailResponse;
import wisoft.nextframe.schedulereservationticketing.dto.performancelist.PerformanceListResponse;
import wisoft.nextframe.schedulereservationticketing.dto.performancelist.PerformanceSummaryDto;
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

	@Test
	@DisplayName("예매 가능한 공연 목록 조회 성공 테스트")
	void getReservablePerformances_Success() {
		// given
		final List<PerformanceSummaryDto> summaryList = List.of(createPerformanceSummaryDto());
		final PageRequest pageable = PageRequest.of(0, 10);
		final PageImpl<PerformanceSummaryDto> mockPage = new PageImpl<>(summaryList, pageable, 1);
		given(performanceRepository.findReservablePerformances(any(Pageable.class))).willReturn(mockPage);

		// when
		final PerformanceListResponse response = performanceService.getReservablePerformances(pageable);

		// then
		assertThat(response).isNotNull();
		// 공연 목록 검증
		assertThat(response.getPerformances()).hasSize(1);
		assertThat(response.getPerformances().getFirst().getName()).isEqualTo("햄릿");
		// 페이지네이션 정보 검증
		assertThat(response.getPagination().getTotalItems()).isEqualTo(1);
		assertThat(response.getPagination().getTotalPages()).isEqualTo(1);
		assertThat(response.getPagination().getPage()).isZero();
	}

	private PerformanceSummaryDto createPerformanceSummaryDto() {
		Date startDate = Date.valueOf(LocalDate.of(2025, 8, 1));
		Date endDate = Date.valueOf(LocalDate.of(2025, 8, 31));

		return new PerformanceSummaryDto(
			UUID.randomUUID(),
			"햄릿",
			"http://example.com/image.jpg",
			PerformanceType.록,
			PerformanceGenre.콘서트,
			"서울예술의전당",
			startDate, // Date 타입으로 전달
			endDate,   // Date 타입으로 전달
			false
		);
	}

	private Performance createPerformance(UUID id, String name) {
		return Performance.builder()
			.id(id)
			.name(name)
			.type(PerformanceType.클래식)
			.genre(PerformanceGenre.뮤지컬)
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
