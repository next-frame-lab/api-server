package wisoft.nextframe.schedulereservationticketing.service.reservation;

import java.util.List;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.entity.reservation.Reservation;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.seat.SeatState;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;
import wisoft.nextframe.schedulereservationticketing.repository.reservation.ReservationRepository;
import wisoft.nextframe.schedulereservationticketing.repository.seat.SeatStateRepository;

@Service
@RequiredArgsConstructor
public class ReservationExecutor {

	private final SeatStateRepository seatStateRepository;
	private final ReservationRepository reservationRepository;
	private final ReservationFactory reservationFactory;

	/**
	 * 실제 예매(좌석 잠금, 예매 정보 생성)를 담당하는 트랜잭션 메서드입니다.
	 *
	 * @CacheEvict: 트랜잭션이 성공적으로 커밋될 때만 캐시를 제거합니다.
	 */
	@Transactional
	@CacheEvict(cacheNames = "seatStates", key = "#schedule.id")
	public Reservation executeReservation(
		Schedule schedule,
		List<SeatDefinition> seats,
		User user,
		int calculatedTotalPrice
	) {
		// 1. 선택한 좌석의 아이디 목록 추출
		final List<UUID> seatIds = seats.stream()
			.map(SeatDefinition::getId)
			.toList();

		// 2. 스케줄에 해당하는 공연 좌석 상태 목록을 조회
		final List<SeatState> seatStates = seatStateRepository.findByScheduleIdAndSeatIds(
			schedule.getId(),
			seatIds
		);

		// 3. 좌석 잠금 수행(Schedule 엔티티)
		schedule.lockSeatsForReservation(seatStates, seats.size());

		// 4. 예매 정보를 생성 및 저장
		final Reservation reservation = reservationFactory.create(user, schedule, seats, calculatedTotalPrice);
		reservationRepository.save(reservation);

		return reservation;
	}
}
