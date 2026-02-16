package wisoft.nextframe.schedulereservationticketing.service.performance;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;

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

import wisoft.nextframe.schedulereservationticketing.dto.performance.performancelist.response.PerformanceSummaryResponse;
import wisoft.nextframe.schedulereservationticketing.dto.performance.search.request.PerformanceSearchCondition;
import wisoft.nextframe.schedulereservationticketing.dto.performance.search.request.PerformanceSearchSort;
import wisoft.nextframe.schedulereservationticketing.dto.performance.search.response.PerformanceSearchResponse;
import wisoft.nextframe.schedulereservationticketing.repository.performance.PerformancePricingRepository;
import wisoft.nextframe.schedulereservationticketing.repository.performance.PerformanceRepository;
import wisoft.nextframe.schedulereservationticketing.repository.performance.PerformanceSearchPort;
import wisoft.nextframe.schedulereservationticketing.repository.performance.PerformanceStatisticRepository;
import wisoft.nextframe.schedulereservationticketing.repository.schedule.ScheduleRepository;

@ExtendWith(MockitoExtension.class)
class PerformanceServiceSearchTest {

	@InjectMocks
	private PerformanceService performanceService;

	@Mock
	private PerformanceRepository performanceRepository;
	@Mock
	private PerformanceSearchPort performanceSearchPort;
	@Mock
	private ScheduleRepository scheduleRepository;
	@Mock
	private PerformancePricingRepository performancePricingRepository;
	@Mock
	private PerformanceStatisticRepository performanceStatisticRepository;

	@Nested
	@DisplayName("searchPerformances 테스트")
	class SearchPerformancesTest {

		@Test
		@DisplayName("키워드와 정렬 기준으로 공연을 검색한다")
		void searchPerformances_withKeywordAndSort() {
			// given
			String keyword = "햄릿";
			PerformanceSearchSort sort = PerformanceSearchSort.HIT_DESC;
			Pageable pageable = PageRequest.of(0, 32);

			PerformanceSummaryResponse summaryDto = mock(PerformanceSummaryResponse.class);
			Page<PerformanceSummaryResponse> mockPage = new PageImpl<>(List.of(summaryDto), pageable, 1);

			given(performanceSearchPort.search(any(PerformanceSearchCondition.class)))
				.willReturn(mockPage);

			// when
			PerformanceSearchResponse result = performanceService.searchPerformances(keyword, sort, pageable);

			// then
			assertThat(result).isNotNull();
			assertThat(result.keyword()).isEqualTo("햄릿");
			assertThat(result.performances()).hasSize(1);
			assertThat(result.pagination().totalItems()).isEqualTo(1);

			ArgumentCaptor<PerformanceSearchCondition> conditionCaptor =
				ArgumentCaptor.forClass(PerformanceSearchCondition.class);
			verify(performanceSearchPort).search(conditionCaptor.capture());

			PerformanceSearchCondition captured = conditionCaptor.getValue();
			assertThat(captured.keyword()).isEqualTo("햄릿");
			assertThat(captured.sort()).isEqualTo(PerformanceSearchSort.HIT_DESC);
			assertThat(captured.pageable()).isEqualTo(pageable);
		}

		@Test
		@DisplayName("키워드 없이 검색하면 전체 조회된다")
		void searchPerformances_withoutKeyword() {
			// given
			Pageable pageable = PageRequest.of(0, 32);

			PerformanceSummaryResponse summaryDto1 = mock(PerformanceSummaryResponse.class);
			PerformanceSummaryResponse summaryDto2 = mock(PerformanceSummaryResponse.class);
			Page<PerformanceSummaryResponse> mockPage = new PageImpl<>(List.of(summaryDto1, summaryDto2), pageable, 2);

			given(performanceSearchPort.search(any(PerformanceSearchCondition.class)))
				.willReturn(mockPage);

			// when
			PerformanceSearchResponse result = performanceService.searchPerformances(null, null, pageable);

			// then
			assertThat(result).isNotNull();
			assertThat(result.keyword()).isNull();
			assertThat(result.performances()).hasSize(2);

			ArgumentCaptor<PerformanceSearchCondition> conditionCaptor =
				ArgumentCaptor.forClass(PerformanceSearchCondition.class);
			verify(performanceSearchPort).search(conditionCaptor.capture());

			PerformanceSearchCondition captured = conditionCaptor.getValue();
			assertThat(captured.keyword()).isNull();
			assertThat(captured.sort()).isEqualTo(PerformanceSearchSort.LATEST);
		}

		@Test
		@DisplayName("검색 결과가 없으면 빈 목록을 반환한다")
		void searchPerformances_emptyResult() {
			// given
			Pageable pageable = PageRequest.of(0, 32);
			Page<PerformanceSummaryResponse> emptyPage = new PageImpl<>(List.of(), pageable, 0);

			given(performanceSearchPort.search(any(PerformanceSearchCondition.class)))
				.willReturn(emptyPage);

			// when
			PerformanceSearchResponse result = performanceService.searchPerformances("존재하지않는공연", null, pageable);

			// then
			assertThat(result).isNotNull();
			assertThat(result.keyword()).isEqualTo("존재하지않는공연");
			assertThat(result.performances()).isEmpty();
			assertThat(result.pagination().totalItems()).isZero();
		}

		@Test
		@DisplayName("공백 키워드는 null로 정규화되어 전달된다")
		void searchPerformances_blankKeywordNormalized() {
			// given
			Pageable pageable = PageRequest.of(0, 32);
			Page<PerformanceSummaryResponse> mockPage = new PageImpl<>(List.of(), pageable, 0);

			given(performanceSearchPort.search(any(PerformanceSearchCondition.class)))
				.willReturn(mockPage);

			// when
			PerformanceSearchResponse result = performanceService.searchPerformances("   ", null, pageable);

			// then
			assertThat(result.keyword()).isNull();

			ArgumentCaptor<PerformanceSearchCondition> conditionCaptor =
				ArgumentCaptor.forClass(PerformanceSearchCondition.class);
			verify(performanceSearchPort).search(conditionCaptor.capture());

			assertThat(conditionCaptor.getValue().keyword()).isNull();
		}
	}
}
