package wisoft.nextframe.schedulereservationticketing.service.reservation;

import java.util.List;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.schedulereservationticketing.common.exception.ErrorCode;
import wisoft.nextframe.schedulereservationticketing.dto.reservation.request.ReservationRequest;
import wisoft.nextframe.schedulereservationticketing.dto.reservation.response.ReservationResponse;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.reservation.Reservation;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;
import wisoft.nextframe.schedulereservationticketing.common.exception.DomainException;
import wisoft.nextframe.schedulereservationticketing.repository.reservation.ReservationRepository;
import wisoft.nextframe.schedulereservationticketing.repository.seat.SeatStateRepository;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReservationService {

	private final SeatStateRepository seatStateRepository;
	private final ReservationRepository reservationRepository;
	private final PriceCalculator priceCalculator;
	private final ReservationFactory reservationFactory;
	private final ReservationDataProvider dataProvider;

	@CacheEvict(cacheNames = "seatStates", key = "#request.scheduleId")
	@Transactional
	public ReservationResponse reserveSeat(UUID userId, ReservationRequest request) {
		// 1. 예매에 필요한 데이터를 준비합니다.
		final ReservationContext context = dataProvider.provide(userId, request);
		final Performance performance = context.performance();
		final User user = context.user();
		final List<SeatDefinition> seats = context.seats();
		final Schedule schedule = context.schedule();
		log.debug("예매 데이터 준비 완료.");

		// 2. 공연 연령 제한에 대한 사용자를 검증합니다.
		performance.verifyAgeLimit(user);
		log.debug("사용자 연령 제한 검증 통과.");

		// 3. 사용자가 선택한 좌석의 총 금액을 계산합니다.
		final int calculatedTotalPrice = priceCalculator.calculateTotalPrice(schedule, seats);
		log.debug("좌석 금액 계산 완료. calculatedTotalPrice: {}", calculatedTotalPrice);

		// 4. 요청 금액(클라이언트)과 계산 금액이 일치하는지 검증합니다.
		if (calculatedTotalPrice != request.totalAmount()) {
			// 4-1. 금액 불일치 시 WARN 로그 (문제 해결의 핵심 단서)
			log.warn("요청 금액 불일치. userId: {}, scheduleId: {}, clientAmount: {}, serverAmount: {}",
				userId, request.scheduleId(), request.totalAmount(), calculatedTotalPrice);
			throw new DomainException(ErrorCode.TOTAL_PRICE_MISMATCH);
		}
		log.debug("요청 금액 검증 통과.");

		// 5. 예매 좌석에 대한 검증 및 잠금 처리를 합니다.
		log.debug("좌석 잠금 처리 시작. seats: {}", seats.stream().map(SeatDefinition::getId).toList());
		schedule.lockSeatsForReservation(seats, seatStateRepository);
		log.debug("좌석 잠금 처리 성공.");

		// 6. 예매 정보를 생성 및 저장합니다.
		final Reservation reservation = reservationFactory.create(user, schedule, seats, calculatedTotalPrice);
		reservationRepository.save(reservation);
		log.debug("예매 정보 저장 완료. reservationId: {}", reservation.getId());

		// 7. 응답 DTO 생성 및 반환합니다.
		return ReservationResponse.from(reservation, performance, schedule, seats);
	}
}
