package wisoft.nextframe.schedulereservationticketing.service.reservation;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.common.exception.ErrorCode;
import wisoft.nextframe.schedulereservationticketing.dto.reservation.request.ReservationRequest;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;
import wisoft.nextframe.schedulereservationticketing.exception.DomainException;
import wisoft.nextframe.schedulereservationticketing.repository.schedule.ScheduleRepository;
import wisoft.nextframe.schedulereservationticketing.repository.stadium.SeatDefinitionRepository;
import wisoft.nextframe.schedulereservationticketing.repository.user.UserRepository;

@Component
@RequiredArgsConstructor
public class ReservationDataProvider {

	private final UserRepository userRepository;
	private final ScheduleRepository scheduleRepository;
	private final SeatDefinitionRepository seatDefinitionRepository;

	/**
	 * userId, ReservationRequest를 기반으로 예매에 필요한 모든 엔티티를 조회하고 검증하여
	 * ReservationContext 객체로 반환합니다.
	 * @param request 예매 요청 DTO
	 * @return 예매 컨텍스트 객체
	 */
	public ReservationContext provide(UUID userId, ReservationRequest request) {
		// 1. 각 ID를 사용하여 엔티티를 조회합니다.
		final User user = userRepository.findById(userId)
			.orElseThrow(() -> new DomainException(ErrorCode.USER_NOT_FOUND));

		final Schedule schedule = scheduleRepository.findById(request.scheduleId())
			.orElseThrow(() -> new DomainException(ErrorCode.SCHEDULE_NOT_FOUND));

		if (request.seatIds().isEmpty() || request.seatIds().size() > 4) {
			throw new DomainException(ErrorCode.INVALID_SEAT_SELECTION_COUNT);
		}

		final List<SeatDefinition> seats = seatDefinitionRepository.findWithStadiumSectionByIdIn(request.seatIds());

		// 2. 조회된 데이터의 정합성을 검증합니다.
		// 2-1. 요청된 좌석이 모두 존재하는지 확인합니다.
		if (seats.size() != request.seatIds().size()) {
			throw new DomainException(ErrorCode.SEAT_NOT_DEFINED);
		}

		// 2-2. 요청된 스케줄이 해당 공연의 스케줄이 맞는지 확인합니다.
		final Performance performance = schedule.getPerformance();
		if (!performance.getId().equals(request.performanceId())) {
			throw new DomainException(ErrorCode.PERFORMANCE_SCHEDULE_MISMATCH);
		}

		// 3. 조회 및 검증이 완료된 엔티티들을 Context 객체에 담아 반환합니다.
		return new ReservationContext(user, schedule, performance, seats);
	}
}
