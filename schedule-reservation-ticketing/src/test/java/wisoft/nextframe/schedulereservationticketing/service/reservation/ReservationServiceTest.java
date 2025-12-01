package wisoft.nextframe.schedulereservationticketing.service.reservation;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.util.ReflectionTestUtils;

import wisoft.nextframe.schedulereservationticketing.builder.PerformanceBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.ReservationBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.ScheduleBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.SeatDefinitionBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.StadiumBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.StadiumSectionBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.UserBuilder;
import wisoft.nextframe.schedulereservationticketing.common.exception.DomainException;
import wisoft.nextframe.schedulereservationticketing.common.exception.ErrorCode;
import wisoft.nextframe.schedulereservationticketing.dto.reservation.request.ReservationRequest;
import wisoft.nextframe.schedulereservationticketing.dto.reservation.response.ReservationResponse;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.reservation.Reservation;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.Stadium;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.StadiumSection;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

	@InjectMocks
	private ReservationService reservationService;

	@Mock
	private PriceCalculator priceCalculator;
	@Mock
	private ReservationDataProvider dataProvider;
	@Mock
	private ReservationExecutor reservationExecutor;

	private UUID userId;
	private int totalPrice;
	private ReservationRequest request;
	private User user;
	private Performance performance;
	private Schedule schedule;
	private List<SeatDefinition> seats;

	@BeforeEach
	void setUp() {
		userId = UUID.randomUUID();
		totalPrice = 150000;

		request = new ReservationRequest(
			UUID.randomUUID(),
			UUID.randomUUID(),
			List.of(UUID.randomUUID()),
			300L,
			totalPrice
		);

		user = UserBuilder.builder()
			.withBirthDate(LocalDate.of(1998, 4, 7)) // 성인
			.build();
		ReflectionTestUtils.setField(user, "id", userId);

		performance = PerformanceBuilder.builder().build();

		Stadium stadium = StadiumBuilder.builder().build();

		schedule = ScheduleBuilder.builder()
			.withPerformance(performance)
			.withStadium(stadium)
			.build();

		StadiumSection section = StadiumSectionBuilder.builder()
			.withStadium(stadium)
			.build();

		SeatDefinition seat = SeatDefinitionBuilder.builder()
			.withStadiumSection(section)
			.build();
		ReflectionTestUtils.setField(seat, "id", UUID.randomUUID());

		seats = List.of(seat);
	}

	@Nested
	class reserveSeatTest {

		@Test
		@DisplayName("예매 성공: 금액 검증 및 연령 제한을 통과하면 예매가 완료된다")
		void reserveSeat_success() {
			// given
			// Context Mocking
			ReservationContext context = createMockContext(user, performance, schedule, seats);
			given(dataProvider.provide(userId, request)).willReturn(context);

			// Price Calculator Mocking
			given(priceCalculator.calculateTotalPrice(schedule, seats)).willReturn(totalPrice);

			// Executor Mocking
			Reservation reservation = ReservationBuilder.builder()
				.withSchedule(schedule)
				.withUser(user)
				.build();
			ReflectionTestUtils.setField(reservation, "id", UUID.randomUUID());

			given(reservationExecutor.executeReservation(any(), any(), any(), anyInt()))
				.willReturn(reservation);

			// when
			ReservationResponse response = reservationService.reserveSeat(userId, request);

			// then
			assertThat(response).isNotNull();

			// 핵심 검증
			verify(reservationExecutor).executeReservation(schedule, seats, user, totalPrice);
		}

		@Test
		@DisplayName("예매 실패: 요청 금액과 서버 계산 금액이 다르면 예외 발생")
		void reserveSeat_fail_priceMismatch() {
			// given
			int realPrice = totalPrice + 3000;

			final ReservationContext context = createMockContext(user, performance, schedule, seats);
			given(dataProvider.provide(userId, request)).willReturn(context);

			// priceCalculator는 요청금액과 다른 금액을 반환
			given(priceCalculator.calculateTotalPrice(schedule, seats)).willReturn(realPrice);

			// when and then
			assertThatThrownBy(() -> reservationService.reserveSeat(userId, request))
				.isInstanceOf(DomainException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.TOTAL_PRICE_MISMATCH);

			verify(reservationExecutor, never()).executeReservation(any(), any(), any(), anyInt());
		}

		@Test
		@DisplayName("예매 실패: 낙관적 락 충돌 발생 시 SEAT_ALREADY_LOCKED 예외로 변환")
		void reserveSeat_fail_optimisticLocking() {
			// given
			ReservationContext context = createMockContext(user, performance, schedule, seats);
			given(dataProvider.provide(userId, request)).willReturn(context);
			given(priceCalculator.calculateTotalPrice(schedule, seats)).willReturn(totalPrice);

			// Executor가 낙관적 락 예외를 던짐
			given(reservationExecutor.executeReservation(any(), any(), any(), anyInt()))
				.willThrow(new OptimisticLockingFailureException("Mock Concurrency Error"));

			// when and then
			assertThatThrownBy(() -> reservationService.reserveSeat(userId, request))
				.isInstanceOf(DomainException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.SEAT_ALREADY_LOCKED);
		}

		private ReservationContext createMockContext(
			User user,
			Performance performance,
			Schedule schedule,
			List<SeatDefinition> seats
		) {
			ReservationContext context = mock(ReservationContext.class);
			given(context.user()).willReturn(user);
			given(context.performance()).willReturn(performance);
			given(context.schedule()).willReturn(schedule);
			given(context.seats()).willReturn(seats);
			return context;
		}
	}
}