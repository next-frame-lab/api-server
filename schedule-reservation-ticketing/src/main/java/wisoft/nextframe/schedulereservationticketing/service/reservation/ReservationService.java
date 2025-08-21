package wisoft.nextframe.schedulereservationticketing.service.reservation;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.dto.reservation.request.ReservationRequest;
import wisoft.nextframe.schedulereservationticketing.dto.reservation.response.ReservationResponse;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.reservation.Reservation;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;
import wisoft.nextframe.schedulereservationticketing.exception.reservation.SeatNotDefinedException;
import wisoft.nextframe.schedulereservationticketing.exception.reservation.TotalPriceMismatchException;
import wisoft.nextframe.schedulereservationticketing.repository.reservation.ReservationRepository;
import wisoft.nextframe.schedulereservationticketing.repository.schedule.ScheduleRepository;
import wisoft.nextframe.schedulereservationticketing.repository.seat.SeatStateRepository;
import wisoft.nextframe.schedulereservationticketing.repository.stadium.SeatDefinitionRepository;
import wisoft.nextframe.schedulereservationticketing.repository.user.UserRepository;

@RequiredArgsConstructor
@Service
public class ReservationService {

	private final UserRepository userRepository;
	private final ScheduleRepository scheduleRepository;
	private final SeatDefinitionRepository seatDefinitionRepository;
	private final SeatStateRepository seatStateRepository;
	private final ReservationRepository reservationRepository;
	private final PriceCalculator priceCalculator;
	private final ReservationFactory reservationFactory;
	private final ReservationMapper reservationMapper;

	@Transactional
	public ReservationResponse reserveSeat(ReservationRequest request) {
		// 1. 엔티티를 조회 및 검증합니다.
		final User user = userRepository.findById(request.getUserId())
			.orElseThrow(EntityNotFoundException::new);
		final Schedule schedule = scheduleRepository.findById(request.getScheduleId())
			.orElseThrow(EntityNotFoundException::new);
		final List<SeatDefinition> seats = seatDefinitionRepository.findWithStadiumSectionByIdIn(request.getSeatIds());
		if (seats.size() != request.getSeatIds().size()) {
			throw new SeatNotDefinedException("요청한 좌석 중 일부를 찾을 수 없습니다.");
		}

		// 2. 공연 연령 제한에 대한 사용자를 검증합니다.
		final Performance performance = schedule.getPerformance();
		performance.verifyAgeLimit(user);

		// 3. 사용자가 선택한 좌석의 총 금액을 계산합니다.
		final int calculatedTotalPrice = priceCalculator.calculateTotalPrice(performance, seats);
		// 4. 요청 금액(클라이언트)과 계산 금액이 일치하는지 검증합니다.
		if (calculatedTotalPrice != request.getTotalAmount()) {
			throw new TotalPriceMismatchException("요청된 금액과 계산된 금액이 일치하지 않습니다.");
		}

		// 5. 예매 좌석에 대한 검증 및 잠금 처리를 합니다.
		schedule.lockSeatsForReservation(seats, seatStateRepository);

		// 6. 예매 정보를 생성 및 저장합니다.
		final Reservation reservation = reservationFactory.create(user, schedule, seats, calculatedTotalPrice);
		reservationRepository.save(reservation);

		// 7. 응답 DTO 생성 및 반환합니다.
		return reservationMapper.toResponse(reservation, performance, schedule, seats);
	}
}
