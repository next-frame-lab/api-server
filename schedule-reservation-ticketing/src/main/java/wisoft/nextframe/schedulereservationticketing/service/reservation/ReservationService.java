package wisoft.nextframe.schedulereservationticketing.service.reservation;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.dto.reservation.request.ReservationRequest;
import wisoft.nextframe.schedulereservationticketing.dto.reservation.response.ReservationResponse;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.reservation.Reservation;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;
import wisoft.nextframe.schedulereservationticketing.exception.reservation.TotalPriceMismatchException;
import wisoft.nextframe.schedulereservationticketing.repository.reservation.ReservationRepository;
import wisoft.nextframe.schedulereservationticketing.repository.seat.SeatStateRepository;

@RequiredArgsConstructor
@Service
public class ReservationService {

	private final SeatStateRepository seatStateRepository;
	private final ReservationRepository reservationRepository;
	private final PriceCalculator priceCalculator;
	private final ReservationFactory reservationFactory;
	private final ReservationDataProvider dataProvider;

	@Transactional
	public ReservationResponse reserveSeat(UUID userId, ReservationRequest request) {
		// 1. 예매에 필요한 데이터를 준비합니다.
		final ReservationContext context = dataProvider.provide(userId, request);
		final Performance performance = context.performance();
		final User user = context.user();
		final List<SeatDefinition> seats = context.seats();
		final Schedule schedule = context.schedule();

		// 2. 공연 연령 제한에 대한 사용자를 검증합니다.
		performance.verifyAgeLimit(user);

		// 3. 사용자가 선택한 좌석의 총 금액을 계산합니다.
		final int calculatedTotalPrice = priceCalculator.calculateTotalPrice(schedule, seats);

		// 4. 요청 금액(클라이언트)과 계산 금액이 일치하는지 검증합니다.
		if (calculatedTotalPrice != request.totalAmount()) {
			throw new TotalPriceMismatchException("요청된 금액과 계산된 금액이 일치하지 않습니다.");
		}

		// 5. 예매 좌석에 대한 검증 및 잠금 처리를 합니다.
		schedule.lockSeatsForReservation(seats, seatStateRepository);

		// 6. 예매 정보를 생성 및 저장합니다.
		final Reservation reservation = reservationFactory.create(user, schedule, seats, calculatedTotalPrice);
		reservationRepository.save(reservation);

		// 7. 응답 DTO 생성 및 반환합니다.
		return ReservationResponse.from(reservation, performance, schedule, seats);
	}
}
