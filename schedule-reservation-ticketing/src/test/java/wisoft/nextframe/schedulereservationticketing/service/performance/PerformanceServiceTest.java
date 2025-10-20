package wisoft.nextframe.schedulereservationticketing.service.performance;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.sql.Date;
import java.time.LocalDate;
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

import wisoft.nextframe.schedulereservationticketing.builder.PerformanceBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.ScheduleBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.StadiumBuilder;
import wisoft.nextframe.schedulereservationticketing.dto.performance.performancedetail.response.PerformanceDetailResponse;
import wisoft.nextframe.schedulereservationticketing.dto.performance.performancedetail.response.SeatSectionPriceResponse;
import wisoft.nextframe.schedulereservationticketing.dto.performance.performancelist.response.PerformanceListResponse;
import wisoft.nextframe.schedulereservationticketing.dto.performance.performancelist.response.PerformanceSummaryResponse;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformanceGenre;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformanceType;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.Stadium;
import wisoft.nextframe.schedulereservationticketing.exception.DomainException;
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
	@DisplayName("성공: 공연 상세 조회 성공 테스트")
	void getPerformanceDetail_Success() {
		// given
		final UUID performanceId = UUID.randomUUID();
		final UUID stadiumId = UUID.randomUUID();

		// 1. 테스트용 엔티티 준비
		final Performance performance = new PerformanceBuilder()
			.withId(performanceId)
			.withName("오페라의 유령")
			.build();

		final Stadium stadium = new StadiumBuilder()
			.withId(stadiumId)
			.build();

		final List<Schedule> schedules = List.of(
			new ScheduleBuilder()
				.withPerformance(performance)
				.withStadium(stadium) // Stadium 정보 추가
				.build()
		);

		// 2. Repository가 반환할 DTO 목록 준비
		final List<SeatSectionPriceResponse> seatPrices = List.of(
			SeatSectionPriceResponse.builder()
				.section("A")// DTO 생성 (빌더가 있다면 빌더 사용)
				.price(150000)
				.build()
		);
		// 3. Mock Repository 설정
		given(performanceRepository.findById(performanceId)).willReturn(Optional.of(performance));
		given(scheduleRepository.findByPerformanceId(performanceId)).willReturn(schedules);
		// 'findCommonPricingByPerformanceAndStadium' 메소드를 Mocking
		given(performancePricingRepository.findSeatSectionPrices(performanceId, stadiumId))
			.willReturn(seatPrices);

		// when
		final PerformanceDetailResponse response = performanceService.getPerformanceDetail(performanceId);

		// then
		assertThat(response).isNotNull();
		assertThat(response.id()).isEqualTo(performanceId);
		assertThat(response.name()).isEqualTo("오페라의 유령");
		assertThat(response.performanceSchedules()).hasSize(1);
		assertThat(response.seatSectionPrices()).hasSize(1);
		assertThat(response.seatSectionPrices().getFirst().section()).isEqualTo("A");
		assertThat(response.seatSectionPrices().getFirst().price()).isEqualTo(150000);
	}

	@Test
	@DisplayName("실패: 공연 상세 조회 실패 테스트 - 존재하지 않는 공연 ID")
	void getPerformanceDetail_Fail_NotFound() {
		// given
		UUID nonExistentId = UUID.randomUUID();
		given(performanceRepository.findById(nonExistentId)).willReturn(Optional.empty());

		// when and then
		assertThatThrownBy(() -> performanceService.getPerformanceDetail(nonExistentId))
			.isInstanceOf(DomainException.class);
	}

	@Test
	@DisplayName("성공: 예매 가능한 공연 목록 조회 성공 테스트")
	void getPerformanceList_Success() {
		// given
		final List<PerformanceSummaryResponse> summaryList = List.of(createPerformanceSummaryDto());
		final PageRequest pageable = PageRequest.of(0, 32);
		final PageImpl<PerformanceSummaryResponse> mockPage = new PageImpl<>(summaryList, pageable, 1);
		given(performanceRepository.findReservablePerformances(any(Pageable.class))).willReturn(mockPage);

		// when
		final PerformanceListResponse response = performanceService.getPerformanceList(pageable);

		// then
		assertThat(response).isNotNull();
		// 공연 목록 검증
		assertThat(response.performances()).hasSize(1);
		assertThat(response.performances().getFirst().getName()).isEqualTo("햄릿");
		// 페이지네이션 정보 검증
		assertThat(response.pagination().totalItems()).isEqualTo(1);
		assertThat(response.pagination().totalPages()).isEqualTo(1);
		assertThat(response.pagination().page()).isZero();
	}

	private PerformanceSummaryResponse createPerformanceSummaryDto() {
		Date startDate = Date.valueOf(LocalDate.of(2025, 8, 1));
		Date endDate = Date.valueOf(LocalDate.of(2025, 8, 31));

		return new PerformanceSummaryResponse(
			UUID.randomUUID(),
			"햄릿",
			"http://example.com/image.jpg",
			PerformanceType.ROCK,
			PerformanceGenre.CONCERT,
			"서울예술의전당",
			startDate, // Date 타입으로 전달
			endDate,   // Date 타입으로 전달
			false
		);
	}
}
