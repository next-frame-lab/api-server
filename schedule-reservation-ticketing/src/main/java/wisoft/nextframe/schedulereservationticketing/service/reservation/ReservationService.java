package wisoft.nextframe.schedulereservationticketing.service.reservation;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.dto.reservation.request.ReservationRequest;
import wisoft.nextframe.schedulereservationticketing.dto.reservation.response.PerformanceInfo;
import wisoft.nextframe.schedulereservationticketing.dto.reservation.response.ReservationResponse;
import wisoft.nextframe.schedulereservationticketing.dto.reservation.response.SeatInfo;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.reservation.Reservation;
import wisoft.nextframe.schedulereservationticketing.entity.reservation.ReservationSeat;
import wisoft.nextframe.schedulereservationticketing.entity.reservation.ReservationSeatId;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;
import wisoft.nextframe.schedulereservationticketing.exception.reservation.ReservationException;
import wisoft.nextframe.schedulereservationticketing.exception.reservation.SeatAlreadyLockedException;
import wisoft.nextframe.schedulereservationticketing.repository.reservation.ReservationRepository;
import wisoft.nextframe.schedulereservationticketing.repository.reservation.ReservationSeatRepository;
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
	private final ReservationSeatRepository reservationSeatRepository;
	private final PriceCalculator priceCalculator;

	@Transactional
	public ReservationResponse reserveSeat(ReservationRequest request) {
		// 1. 엔티티 조회 및 검증
		final User user = userRepository.findById(request.getUserId()).orElseThrow(EntityNotFoundException::new);
		final Schedule schedule = scheduleRepository.findById(request.getScheduleId())
			.orElseThrow(EntityNotFoundException::new);
		final Performance performance = schedule.getPerformance();
		final List<SeatDefinition> seats = seatDefinitionRepository.findWithStadiumSectionByIdIn(request.getSeatIds());
		if (seats.size() != request.getSeatIds().size()) {
			throw new ReservationException("요청한 좌석 중 일부를 찾을 수 없습니다.");
		}

		// 2. 비즈니스 규칙 검증
		// 공연 연령 제한 검증
		performance.verifyAgeLimit(user);

		// 3. 가격 계산
		final int calculatedTotalPrice = priceCalculator.calculate(performance, seats);
		if (calculatedTotalPrice != request.getTotalAmount()) {
			throw new ReservationException("요청된 총액과 서버에서 계산된 금액이 일치하지 않습니다.");
		}

		// 4. 예매 좌석 검증 및 좌석 잠금 처리
		schedule.lockSeatsForReservation(seats, seatStateRepository);

		// 5. 예매 정보 생성 및 저장
		final Reservation reservation = Reservation.create(user, schedule, calculatedTotalPrice);
		reservationRepository.save(reservation);

		// 6. 예매 좌석 정보 생성 및 저장
		final List<ReservationSeat> reservationSeats = seats.stream()
			.map(seat -> ReservationSeat.builder()
				.id(new ReservationSeatId(reservation.getId(), seat.getId()))
				.reservation(reservation)
				.seatDefinition(seat)
				.build())
			.toList();
		reservationSeatRepository.saveAll(reservationSeats);

		// 7. 응답 DTO 생성 및 반환
		return createReservationResponse(reservation, performance, schedule, seats);
	}

	private ReservationResponse createReservationResponse(Reservation reservation, Performance performance, Schedule schedule, List<SeatDefinition> seats) {
		final PerformanceInfo performanceInfo = new PerformanceInfo(
			performance.getName(),
			schedule.getPerformanceDatetime().toLocalDate(),
			schedule.getPerformanceDatetime().toLocalTime()
		);

		final List<SeatInfo> seatInfos = seats.stream()
			.map(seat -> new SeatInfo(
				seat.getStadiumSection().getSection(),
				seat.getRowNo(),
				seat.getColumnNo()
			))
			.toList();

		return new ReservationResponse(
			reservation.getId(),
			performanceInfo,
			seatInfos,
			reservation.getTotalPrice()
		);
	}
}
