package wisoft.nextframe.schedulereservationticketing.service.performance;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.dto.performance.performancedetail.response.PerformanceDetailResponse;
import wisoft.nextframe.schedulereservationticketing.dto.performance.performancedetail.response.SeatSectionPriceResponse;
import wisoft.nextframe.schedulereservationticketing.dto.performance.performancelist.response.PaginationResponse;
import wisoft.nextframe.schedulereservationticketing.dto.performance.performancelist.response.PerformanceListResponse;
import wisoft.nextframe.schedulereservationticketing.dto.performance.performancelist.response.PerformanceResponse;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.repository.performance.PerformancePricingRepository;
import wisoft.nextframe.schedulereservationticketing.repository.performance.PerformanceRepository;
import wisoft.nextframe.schedulereservationticketing.repository.schedule.ScheduleRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PerformanceService {

	private final PerformanceRepository performanceRepository;
	private final ScheduleRepository scheduleRepository;
	private final PerformancePricingRepository performancePricingRepository;

	public PerformanceDetailResponse getPerformanceDetail(UUID performanceId) {
		// 1. 공연(Performance)를 조회합니다.
		final Performance performance = performanceRepository.findById(performanceId)
			.orElseThrow(() -> new EntityNotFoundException("해당 공연을 찾을 수 없습니다."));

		// 2. 공연(Performanc)에 해당하는 공연일정(Schedule)을 조회합니다.
		final List<Schedule> schedules = scheduleRepository.findByPerformanceId(performanceId);
		if (schedules.isEmpty()) {
			throw new EntityNotFoundException("해당 공연 일정을 찾을 수 없습니다.");
		}

		// 3. 섹션별 가격(SeatSectionPrice) 정보를 조회합니다.
		// 공연 일정에서 공연장 아이디를 가져옵니다.
		final UUID stadiumId = schedules.getFirst().getStadium().getId();
		// 공연 아이디, 공연장 아이디를 통해 섹션별 가격 정보를 조회합니다.
		final List<SeatSectionPriceResponse> seatSectionPrices
			= performancePricingRepository.findSeatSectionPrices(performanceId, stadiumId);

		return PerformanceDetailResponse.from(performance, schedules, seatSectionPrices);
	}

	public PerformanceListResponse getReservablePerformances(Pageable pageable) {
		// 1. DTO로 구성된 Page 객체 조회
		final Page<PerformanceResponse> performancePage = performanceRepository.findReservablePerformances(pageable);

		// 2. Page 객체를 사용하여 최종 응답 DTO를 조립
		return PerformanceListResponse.builder()
			.performances(performancePage.getContent())
			.pagination(PaginationResponse.from(performancePage))
			.build();
	}
}
