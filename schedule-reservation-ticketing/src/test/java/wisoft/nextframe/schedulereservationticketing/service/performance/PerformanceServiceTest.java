package wisoft.nextframe.schedulereservationticketing.service.performance;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import wisoft.nextframe.schedulereservationticketing.builder.PerformanceBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.ScheduleBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.StadiumBuilder;
import wisoft.nextframe.schedulereservationticketing.common.exception.DomainException;
import wisoft.nextframe.schedulereservationticketing.common.exception.ErrorCode;
import wisoft.nextframe.schedulereservationticketing.dto.performance.performancedetail.response.PerformanceDetailResponse;
import wisoft.nextframe.schedulereservationticketing.dto.performance.performancedetail.response.SeatSectionPriceResponse;
import wisoft.nextframe.schedulereservationticketing.dto.performance.performancelist.response.PerformanceListResponse;
import wisoft.nextframe.schedulereservationticketing.dto.performance.performancelist.response.PerformanceSummaryResponse;
import wisoft.nextframe.schedulereservationticketing.dto.performance.performancelist.response.Top10PerformanceListResponse;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.Stadium;
import wisoft.nextframe.schedulereservationticketing.repository.performance.PerformancePricingRepository;
import wisoft.nextframe.schedulereservationticketing.repository.performance.PerformanceRepository;
import wisoft.nextframe.schedulereservationticketing.repository.performance.PerformanceStatisticRepository;
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
	@Mock
	private PerformanceStatisticRepository performanceStatisticRepository;

	@Nested
	class getPerformanceDetailTest {

		@Test
		@DisplayName("공연 상세 정보를 정확히 조회한다")
		void getPerformanceDetail_success() {
			UUID performanceId = UUID.randomUUID();
			UUID stadiumId = UUID.randomUUID();

			Stadium stadium = StadiumBuilder.builder().withId(stadiumId).build();
			Performance performance = PerformanceBuilder.builder()
				.withId(performanceId)
				.withName("테스트 공연")
				.build();

			Schedule schedule = ScheduleBuilder.builder()
				.withId(UUID.randomUUID())
				.withPerformance(performance)
				.withStadium(stadium)
				.build();

			given(performanceRepository.findById(performanceId))
				.willReturn(Optional.of(performance));
			given(scheduleRepository.findByPerformanceId(performanceId))
				.willReturn(List.of(schedule));

			SeatSectionPriceResponse priceResponse = SeatSectionPriceResponse.builder()
				.section("A")
				.price(150000)
				.build();
			given(performancePricingRepository.findSeatSectionPrices(performanceId, stadiumId))
				.willReturn(List.of(priceResponse));

			given(performanceStatisticRepository.findById(performanceId))
				.willReturn(Optional.empty());

			// when
			PerformanceDetailResponse result = performanceService.getPerformanceDetail(performanceId);

			// then
			assertThat(result).isNotNull();
			assertThat(result.name()).isEqualTo("테스트 공연");
			assertThat(result.performanceSchedules()).hasSize(1);

			// 검증
			verify(performanceRepository).findById(performanceId);
			verify(scheduleRepository).findByPerformanceId(performanceId);
		}

		@Test
		@DisplayName("공연은 있지만 일정이 없으면 예외 발생")
		void getPerformanceDetail_fail_scheduleNotFound() {
			// given
			UUID performanceId = UUID.randomUUID();

			Performance performance = PerformanceBuilder.builder()
				.withId(performanceId)
				.withName("취소된 공연")
				.build();

			given(performanceRepository.findById(performanceId))
				.willReturn(Optional.of(performance));

			// 공연 일정은 빈 리스트로 반환
			given(scheduleRepository.findByPerformanceId(performanceId))
				.willReturn(Collections.emptyList());

			// when and then
			assertThatThrownBy(() -> performanceService.getPerformanceDetail(performanceId))
				.isInstanceOf(DomainException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.SCHEDULE_NOT_FOUND);
		}
	}

	@Nested
	class getPerformanceListTest {

		@Test
		@DisplayName("예매 가능한 공연 목록을 페이징하여 성공적으로 조회한다")
		void getPerformanceList_success() {
			// given
			Pageable pageable = PageRequest.of(0, 10);

			PerformanceSummaryResponse summaryDto = mock(PerformanceSummaryResponse.class);
			Page<PerformanceSummaryResponse> mockPage = new PageImpl<>(List.of(summaryDto), pageable, 1);

			given(performanceRepository.findReservablePerformances(pageable))
				.willReturn(mockPage);

			// when
			PerformanceListResponse result = performanceService.getPerformanceList(pageable);

			// then
			assertThat(result).isNotNull();
			assertThat(result.performances()).hasSize(1);
			assertThat(result.pagination().totalPages()).isEqualTo(1);

			verify(performanceRepository).findReservablePerformances(pageable);
		}
	}

	@Nested
	class getTop10PerformancesTest {

		@Test
		@DisplayName("인기 공연 조회: 인기 공연 목록을 상위 10개만 성공적으로 조회한다.")
		void getTop10Performances_success() {
			// given
			PerformanceSummaryResponse summaryDto1 = mock(PerformanceSummaryResponse.class);
			PerformanceSummaryResponse summaryDto2 = mock(PerformanceSummaryResponse.class);
			List<PerformanceSummaryResponse> topList = List.of(summaryDto1, summaryDto2);

			Page<PerformanceSummaryResponse> mockPage = new PageImpl<>(topList);

			given(performanceRepository.findTop10Performances(any(Pageable.class)))
				.willReturn(mockPage);

			// when
			Top10PerformanceListResponse response = performanceService.getTop10Performances();

			// then
			assertThat(response).isNotNull();
			assertThat(response.performances()).hasSize(2); // 리스트 크기 확인

			// 검증: 실제로 PageSize가 10인 Pageable이 전달되었는지 확인
			ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
			verify(performanceRepository).findTop10Performances(pageableCaptor.capture());

			Pageable capturedPageable = pageableCaptor.getValue();
			assertThat(capturedPageable.getPageSize()).isEqualTo(10);
			assertThat(capturedPageable.getPageNumber()).isEqualTo(0);
		}
	}

}
