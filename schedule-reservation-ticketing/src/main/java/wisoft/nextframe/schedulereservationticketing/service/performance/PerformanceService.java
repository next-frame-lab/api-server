package wisoft.nextframe.schedulereservationticketing.service.performance;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.dto.performance.response.PerformanceDetailResponse;
import wisoft.nextframe.schedulereservationticketing.dto.performance.response.PerformanceScheduleResponseDto;
import wisoft.nextframe.schedulereservationticketing.dto.performance.response.SeatSectionPriceResponseDto;
import wisoft.nextframe.schedulereservationticketing.dto.performance.response.StadiumResponseDto;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformancePricing;
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
		final List<PerformancePricing> pricings = performancePricingRepository.findByPerformanceId(performanceId);

		return toPerformanceDetailResponse(performance, schedules, pricings);
	}

	private PerformanceDetailResponse toPerformanceDetailResponse(
		Performance performance,
		List<Schedule> schedules,
		List<PerformancePricing> pricings
	) {

		// Stadium 정보 반환
		final StadiumResponseDto stadiumResponseDto = schedules.stream()
			.findFirst()
			.map(schedule -> {
				final Stadium stadium = schedule.getStadium();
				return StadiumResponseDto.builder()
					.id(stadium.getId())
					.name(stadium.getName())
					.address(stadium.getAddress())
					.build();
			}).orElse(null);

		// 스케줄 정보 반환
		final List<PerformanceScheduleResponseDto> scheduleDtos = schedules.stream()
			.map(schedule -> PerformanceScheduleResponseDto.builder()
				.id(schedule.getId())
				.date(schedule.getPerformanceDatetime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
				.time(schedule.getPerformanceDatetime().format(DateTimeFormatter.ofPattern("HH:mm")))
				.build())
			.toList();

		// 좌석 가격 정보 반환
		final List<SeatSectionPriceResponseDto> seatSectionPriceResponseDtos = pricings.stream()
			.map(pricing -> SeatSectionPriceResponseDto.builder()
				.section(pricing.getStadiumSection().getSection())
				.price(pricing.getPrice())
				.build())
			.toList();

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
			.stadium(stadiumResponseDto)
			.performanceSchedules(scheduleDtos)
			.seatSectionPrices(seatSectionPriceResponseDtos)
			.build();
	}
}
