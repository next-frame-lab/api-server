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

import jakarta.persistence.EntityNotFoundException;
import wisoft.nextframe.schedulereservationticketing.builder.PerformanceBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.PerformancePricingBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.ScheduleBuilder;
import wisoft.nextframe.schedulereservationticketing.dto.performancedetail.response.PerformanceDetailResponse;
import wisoft.nextframe.schedulereservationticketing.dto.performancelist.reponse.PerformanceListResponse;
import wisoft.nextframe.schedulereservationticketing.dto.performancelist.reponse.PerformanceResponse;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformanceGenre;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformancePricing;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformanceType;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
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
		final Performance performance = new PerformanceBuilder().withId(performanceId).withName("오페라의 유령").build();
		final List<Schedule> schedules = List.of(new ScheduleBuilder().withPerformance(performance).build());
		final List<PerformancePricing> pricings = List.of(
			new PerformancePricingBuilder().withPerformance(performance).build());

		given(performanceRepository.findById(performanceId)).willReturn(Optional.of(performance));
		given(scheduleRepository.findByPerformanceId(performanceId)).willReturn(schedules);
		given(performancePricingRepository.findByPerformanceId(performanceId)).willReturn(pricings);

		// when
		final PerformanceDetailResponse response = performanceService.getPerformanceDetail(performanceId);

		// then
		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(performanceId);
		assertThat(response.getName()).isEqualTo("오페라의 유령");
		assertThat(response.getPerformanceSchedules()).hasSize(1);
		assertThat(response.getSeatSectionPrices()).hasSize(1);
		assertThat(response.getSeatSectionPrices().getFirst().getSection()).isEqualTo("A");
	}

	@Test
	@DisplayName("실패: 공연 상세 조회 실패 테스트 - 존재하지 않는 공연 ID")
	void getPerformanceDetail_Fail_NotFound() {
		// given
		UUID nonExistentId = UUID.randomUUID();
		given(performanceRepository.findById(nonExistentId)).willReturn(Optional.empty());

		// when and then
		assertThatThrownBy(() -> performanceService.getPerformanceDetail(nonExistentId))
			.isInstanceOf(EntityNotFoundException.class);
	}

	@Test
	@DisplayName("성공: 예매 가능한 공연 목록 조회 성공 테스트")
	void getReservablePerformances_Success() {
		// given
		final List<PerformanceResponse> summaryList = List.of(createPerformanceSummaryDto());
		final PageRequest pageable = PageRequest.of(0, 32);
		final PageImpl<PerformanceResponse> mockPage = new PageImpl<>(summaryList, pageable, 1);
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

	private PerformanceResponse createPerformanceSummaryDto() {
		Date startDate = Date.valueOf(LocalDate.of(2025, 8, 1));
		Date endDate = Date.valueOf(LocalDate.of(2025, 8, 31));

		return new PerformanceResponse(
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
}
