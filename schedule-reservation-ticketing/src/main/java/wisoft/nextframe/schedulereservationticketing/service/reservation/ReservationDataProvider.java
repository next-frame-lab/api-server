package wisoft.nextframe.schedulereservationticketing.service.reservation;

import java.util.List;

import org.springframework.stereotype.Component;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.dto.reservation.request.ReservationRequest;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;
import wisoft.nextframe.schedulereservationticketing.exception.reservation.ReservationException;
import wisoft.nextframe.schedulereservationticketing.exception.reservation.SeatNotDefinedException;
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
	 * ReservationRequest를 기반으로 예매에 필요한 모든 엔티티를 조회하고 검증하여
	 * ReservationContext 객체로 반환합니다.
	 * @param request 예매 요청 DTO
	 * @return 예매 컨텍스트 객체
	 */
	public ReservationContext provide(ReservationRequest request) {
		// 1. 각 ID를 사용하여 엔티티를 조회합니다.
		final User user = userRepository.findById(request.getUserId())
			.orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

		final Schedule schedule = scheduleRepository.findById(request.getScheduleId())
			.orElseThrow(() -> new EntityNotFoundException("스케줄을 찾을 수 없습니다."));

		final List<SeatDefinition> seats = seatDefinitionRepository.findWithStadiumSectionByIdIn(request.getSeatIds());

		// 2. 조회된 데이터의 정합성을 검증합니다.
		// 2-1. 요청된 좌석이 모두 존재하는지 확인합니다.
		if (seats.size() != request.getSeatIds().size()) {
			throw new SeatNotDefinedException("요청한 좌석 중 일부를 찾을 수 없습니다.");
		}

		// 2-2. 요청된 스케줄이 해당 공연의 스케줄이 맞는지 확인합니다.
		final Performance performance = schedule.getPerformance();
		if (!performance.getId().equals(request.getPerformanceId())) {
			throw new ReservationException("요청된 공연과 스케줄 정보가 일치하지 않습니다.");
		}

		// 3. 조회 및 검증이 완료된 엔티티들을 Context 객체에 담아 반환합니다.
		return new ReservationContext(user, schedule, performance, seats);
	}
}
