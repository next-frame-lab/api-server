package wisoft.nextframe.schedulereservationticketing.service.performance;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.schedulereservationticketing.common.exception.ErrorCode;
import wisoft.nextframe.schedulereservationticketing.dto.performance.performancedetail.response.PerformanceDetailResponse;
import wisoft.nextframe.schedulereservationticketing.dto.performance.performancedetail.response.SeatSectionPriceResponse;
import wisoft.nextframe.schedulereservationticketing.dto.performance.performancelist.response.PerformanceListResponse;
import wisoft.nextframe.schedulereservationticketing.dto.performance.performancelist.response.PerformanceSummaryResponse;
import wisoft.nextframe.schedulereservationticketing.dto.performance.performancelist.response.Top10PerformanceListResponse;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.exception.DomainException;
import wisoft.nextframe.schedulereservationticketing.repository.performance.PerformancePricingRepository;
import wisoft.nextframe.schedulereservationticketing.repository.performance.PerformanceRepository;
import wisoft.nextframe.schedulereservationticketing.repository.schedule.ScheduleRepository;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PerformanceService {

	private final PerformanceRepository performanceRepository;
	private final ScheduleRepository scheduleRepository;
	private final PerformancePricingRepository performancePricingRepository;

	public PerformanceDetailResponse getPerformanceDetail(UUID performanceId) {
		// 1. 공연(Performance)를 조회합니다.
		final Performance performance = performanceRepository.findById(performanceId).orElseThrow(() -> {
			log.warn("존재하지 않는 공연 정보 조회 시도. performanceId: {}", performanceId);
			return new DomainException(ErrorCode.PERFORMANCE_NOT_FOUND);
		});
		log.debug("공연 정보 조회 완료. performanceId: {}", performanceId);

		// 2. 공연(Performance)에 해당하는 공연일정(Schedule)을 조회합니다.
		final List<Schedule> schedules = scheduleRepository.findByPerformanceId(performanceId);
		if (schedules.isEmpty()) {
			log.warn("공연은 존재하지만, 일정이 없는 경우. performanceId: {}", performanceId);
			throw new DomainException(ErrorCode.SCHEDULE_NOT_FOUND);
		}
		log.debug("공연 일정 조회 완료. 찾은 일정 수: {}", schedules.size());

		// 3. 좌석의 섹션별 가격(SeatSectionPrice) 정보를 조회합니다.
		final UUID stadiumId = schedules.getFirst().getStadium().getId();
		log.debug("공연장 좌석 가격 조회 시작. stadiumId: {}", stadiumId);
		final List<SeatSectionPriceResponse> seatSectionPrices = performancePricingRepository.findSeatSectionPrices(
			performanceId, stadiumId);

		return PerformanceDetailResponse.from(performance, schedules, seatSectionPrices);
	}

	public PerformanceListResponse getPerformanceList(Pageable pageable) {
		// 1. PerformanceSummaryResponse로 구성된 공연 목록 Page 객체 조회합니다.
		final Page<PerformanceSummaryResponse> performancePage = performanceRepository.findReservablePerformances(pageable);
		log.debug("예매 가능 공연 목록 조회 완료. 총 {} 페이지 중 {} 페이지 조회", performancePage.getTotalPages(), performancePage.getNumber());

		// 2. Page 객체를 사용하여 최종 응답 DTO를 조립합니다.
		return PerformanceListResponse.from(performancePage);
	}

	public Top10PerformanceListResponse getTop10Performances() {
		// 1. 상위 10개만 조회하기 위한 Pageable 객체를 생성합니다.
		Pageable topTenPageable = PageRequest.of(0, 10);

		// 2. Repository를 호출하여 JPQL로 정의된 인기 공연 목록을 조회합니다.
		//    (결과는 PerformanceSummaryResponse DTO 리스트로 바로 매핑됩니다.)
		List<PerformanceSummaryResponse> top10List = performanceRepository.findTop10Performances(topTenPageable).getContent();
		log.debug("인기 공연 목록 조회 완료. 조회된 공연 수: {}", top10List.size());

		// 3. 조회된 DTO 목록을 최종 응답 DTO(Top10PerformanceListResponse)로 감싸 반환합니다.
		return new Top10PerformanceListResponse(top10List);
	}
}
