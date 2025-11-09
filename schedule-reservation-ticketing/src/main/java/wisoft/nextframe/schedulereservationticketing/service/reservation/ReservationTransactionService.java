package wisoft.nextframe.schedulereservationticketing.service.reservation;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.entity.reservation.Reservation;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;
import wisoft.nextframe.schedulereservationticketing.repository.reservation.ReservationRepository;
import wisoft.nextframe.schedulereservationticketing.repository.seat.SeatStateRepository;

@Service
@RequiredArgsConstructor
public class ReservationTransactionService {

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
		// 5. 예매 좌석에 대한 검증 및 잠금 처리를 합니다.
		// (이 로직은 이제 트랜잭션 안에서 수행됩니다.)
		schedule.lockSeatsForReservation(seats, seatStateRepository);

		// 6. 예매 정보를 생성 및 저장합니다.
		final Reservation reservation = reservationFactory.create(user, schedule, seats, calculatedTotalPrice);
		reservationRepository.save(reservation);

		return reservation;
	}
}
