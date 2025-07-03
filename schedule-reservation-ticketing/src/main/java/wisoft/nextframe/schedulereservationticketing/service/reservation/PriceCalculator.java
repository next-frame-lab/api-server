package wisoft.nextframe.schedulereservationticketing.service.reservation;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformancePricing;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;
import wisoft.nextframe.schedulereservationticketing.repository.performance.PerformancePricingRepository;

@Component
@RequiredArgsConstructor
public class PriceCalculator {

	private final PerformancePricingRepository performancePricingRepository;

	/**
	 * 스케줄과 좌석 목록을 기반으로 총 가격을 계산합니다.
	 * @param schedule 공연 일정 엔티티
	 * @param seats 선택된 좌석 목록
	 * @return 계산된 총 가격
	 */
	public int calculateTotalPrice(Schedule schedule, List<SeatDefinition> seats) {
		// 1. 좌석들로부터 섹션 ID 목록을 추출합니다.
		final List<UUID> sectionIds = seats.stream()
			.map(seat -> seat.getStadiumSection().getId())
			.distinct()
			.toList();

		// 2. 스케줄 ID와 섹션 ID 목록을 사용하여 가격 정보를 조회합니다. (수정된 부분)
		final Map<UUID, Integer> priceMapBySection = performancePricingRepository
			.findByScheduleIdAndSectionIds(schedule.getId(), sectionIds)
			.stream()
			.collect(Collectors.toMap(
				pricing -> pricing.getStadiumSection().getId(), // stadiumSectionId를 키로 사용
				PerformancePricing::getPrice
			));

		// 3. 각 좌석의 섹션에 맞는 가격을 찾아 합산
		return seats.stream()
			.mapToInt(seat -> priceMapBySection.get(seat.getStadiumSection().getId()))
			.sum();
	}
}
