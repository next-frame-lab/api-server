package wisoft.nextframe.schedulereservationticketing.service.performance;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.dto.performancedetail.response.PerformanceDetailResponse;
import wisoft.nextframe.schedulereservationticketing.dto.performancedetail.response.PerformanceScheduleResponse;
import wisoft.nextframe.schedulereservationticketing.dto.performancedetail.response.SeatSectionPriceResponse;
import wisoft.nextframe.schedulereservationticketing.dto.performancedetail.response.StadiumResponse;
import wisoft.nextframe.schedulereservationticketing.dto.performancelist.response.PaginationResponse;
import wisoft.nextframe.schedulereservationticketing.dto.performancelist.response.PerformanceListResponse;
import wisoft.nextframe.schedulereservationticketing.dto.performancelist.response.PerformanceResponse;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.Stadium;
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
		// 1. 기본 공연 정보 조회
		final Performance performance = performanceRepository.findById(performanceId)
			.orElseThrow(EntityNotFoundException::new);

		// 2. 공연 일정, 공연 가격 정보 조회
		final List<Schedule> schedules = scheduleRepository.findByPerformanceId(performanceId);

		// 3. 공통 좌석 정보 조회
		List<SeatSectionPriceResponse> seatSectionPrices;
		if (schedules.isEmpty()) {
			// 스케줄이 없으면 가격 정보도 없음
			seatSectionPrices = Collections.emptyList();
		} else {
			// 첫 번째 스케줄에서 stadiumId를 추출
			UUID stadiumId = schedules.getFirst().getStadium().getId();
			// 새로 만든 Repository 메소드 호출
			seatSectionPrices = performancePricingRepository.findCommonPricingByPerformanceAndStadium(performanceId, stadiumId);
		}

		return toPerformanceDetailResponse(performance, schedules, seatSectionPrices);
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

	private PerformanceDetailResponse toPerformanceDetailResponse(
		Performance performance,
		List<Schedule> schedules,
		List<SeatSectionPriceResponse> seatSectionPrices
	) {

		// Stadium 정보 반환
		final StadiumResponse stadiumResponse = schedules.stream()
			.findFirst()
			.map(schedule -> {
				final Stadium stadium = schedule.getStadium();
				return StadiumResponse.builder()
					.id(stadium.getId())
					.name(stadium.getName())
					.address(stadium.getAddress())
					.build();
			}).orElse(null);

		// 스케줄 정보 반환
		final List<PerformanceScheduleResponse> scheduleDtos = schedules.stream()
			.map(schedule -> PerformanceScheduleResponse.builder()
				.id(schedule.getId())
				.date(schedule.getPerformanceDatetime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
				.time(schedule.getPerformanceDatetime().format(DateTimeFormatter.ofPattern("HH:mm")))
				.build())
			.toList();

		LocalDateTime ticketOpenTime = null;
		LocalDateTime ticketCloseTime = null;

		if (!schedules.isEmpty()) {
			ticketOpenTime = schedules.getFirst().getTicketOpenTime();
			ticketCloseTime = schedules.getFirst().getTicketCloseTime();
		}

		// 최종 응답 DTO 조립
		return PerformanceDetailResponse.builder()
			.id(performance.getId())
			.imageUrl(performance.getImageUrl())
			.name(performance.getName())
			.type(performance.getType().name())
			.genre(performance.getGenre().name())
			.averageStar(4.8)
			.runningTime((int)performance.getRunningTime().toMinutes())
			.description(performance.getDescription())
			.adultOnly(performance.getAdultOnly())
			.ticketOpenTime(ticketOpenTime)
			.ticketCloseTime(ticketCloseTime)
			.stadium(stadiumResponse)
			.performanceSchedules(scheduleDtos)
			.seatSectionPrices(seatSectionPrices)
			.build();
	}
}
