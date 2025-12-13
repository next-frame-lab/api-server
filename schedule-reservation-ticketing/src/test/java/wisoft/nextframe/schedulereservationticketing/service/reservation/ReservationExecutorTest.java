package wisoft.nextframe.schedulereservationticketing.service.reservation;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import wisoft.nextframe.schedulereservationticketing.builder.PerformanceBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.ReservationBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.ScheduleBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.SeatDefinitionBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.SeatStateBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.StadiumBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.StadiumSectionBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.UserBuilder;
import wisoft.nextframe.schedulereservationticketing.common.exception.DomainException;
import wisoft.nextframe.schedulereservationticketing.common.exception.ErrorCode;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.reservation.Reservation;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.seat.SeatState;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.Stadium;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.StadiumSection;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;
import wisoft.nextframe.schedulereservationticketing.repository.reservation.ReservationRepository;
import wisoft.nextframe.schedulereservationticketing.repository.seat.SeatStateRepository;

@ExtendWith(MockitoExtension.class)
class ReservationExecutorTest {

	@InjectMocks
	private ReservationExecutor reservationExecutor;

	@Mock
	private SeatStateRepository seatStateRepository;
	@Mock
	private ReservationRepository reservationRepository;
	@Mock
	private ReservationFactory reservationFactory;

	// 공통 테스트 데이터
	private User user;
	private Schedule schedule;
	private Schedule spySchedule;
	private UUID scheduleId;
	private UUID seatId1;
	private UUID seatId2;
	private List<SeatDefinition> seats;

	@BeforeEach
	void setUp() {
		// 사용자
		user = UserBuilder.builder().build();
		ReflectionTestUtils.setField(user, "id", UUID.randomUUID());

		// 공연/경기장/스케줄
		Performance performance = PerformanceBuilder.builder().build();
		Stadium stadium = StadiumBuilder.builder().build();
		schedule = ScheduleBuilder.builder()
			.withPerformance(performance)
			.withStadium(stadium)
			.build();
		spySchedule = spy(schedule);
		scheduleId = UUID.randomUUID();
		ReflectionTestUtils.setField(spySchedule, "id", scheduleId);

		// 구역/좌석 2개
		StadiumSection section = StadiumSectionBuilder.builder()
			.withStadium(stadium)
			.build();

		SeatDefinition seat1 = SeatDefinitionBuilder.builder()
			.withStadiumSection(section)
			.build();
		SeatDefinition seat2 = SeatDefinitionBuilder.builder()
			.withStadiumSection(section)
			.build();
		seatId1 = UUID.randomUUID();
		seatId2 = UUID.randomUUID();
		ReflectionTestUtils.setField(seat1, "id", seatId1);
		ReflectionTestUtils.setField(seat2, "id", seatId2);
		seats = List.of(seat1, seat2);
	}

	@Test
	@DisplayName("예매 실행 성공: 좌석 상태 잠금, 예매 생성/저장, 반환 검증")
	void executeReservation_success() {
		// given: SeatStateRepository 조회값 세팅(좌석 수와 동일하게 2개)
		final SeatState ss1 = SeatStateBuilder.builder()
			.withScheduleId(scheduleId)
			.withSeatId(seatId1)
			.withIsLocked(false)
			.build();
		final SeatState ss2 = SeatStateBuilder.builder()
			.withScheduleId(scheduleId)
			.withSeatId(seatId2)
			.withIsLocked(false)
			.build();
		given(seatStateRepository.findByScheduleIdAndSeatIds(eq(scheduleId), anyList()))
			.willReturn(List.of(ss1, ss2));

		// ReservationFactory가 생성해 줄 예약 객체
		final Reservation reservation = ReservationBuilder.builder()
			.withSchedule(schedule)
			.withUser(user)
			.build();
		ReflectionTestUtils.setField(reservation, "id", UUID.randomUUID());
		final int totalPrice = 200000;
		given(reservationFactory.create(eq(user), eq(spySchedule), eq(seats), eq(totalPrice)))
			.willReturn(reservation);

		// when
		final Reservation result = reservationExecutor.executeReservation(
			spySchedule,
			seats,
			user,
			totalPrice
		);

		// then
		assertThat(result).isNotNull();
		assertThat(result).isSameAs(reservation);

		// 좌석 상태 잠금이 수행되어 isLocked=true가 되었는지 확인
		assertThat(ss1.getIsLocked()).isTrue();
		assertThat(ss2.getIsLocked()).isTrue();

		// 내부 협력 객체 호출 검증
		verify(seatStateRepository).findByScheduleIdAndSeatIds(
			eq(scheduleId),
			argThat(ids -> ids.containsAll(List.of(seatId1, seatId2)) && ids.size() == 2)
		);
		verify(spySchedule).lockSeatsForReservation(List.of(ss1, ss2), 2);
		verify(reservationFactory).create(user, spySchedule, seats, totalPrice);
		verify(reservationRepository).save(reservation);
	}

	@Test
	@DisplayName("예매 실행 실패: 조회된 좌석 상태 수가 선택 좌석 수와 다르면 SEAT_NOT_DEFINED 예외")
	void executeReservation_fail_seatCountMismatch() {
		// given: SeatStateRepository는 1개만 반환하여 불일치 유도
		final SeatState onlyOne = SeatStateBuilder.builder()
			.withScheduleId(scheduleId)
			.withSeatId(seatId1)
			.withIsLocked(false)
			.build();
		given(seatStateRepository.findByScheduleIdAndSeatIds(eq(scheduleId), anyList()))
			.willReturn(List.of(onlyOne));

		// when and then
		assertThatThrownBy(() -> reservationExecutor.executeReservation(spySchedule, seats, user, 10000))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.SEAT_NOT_DEFINED);

		// 실패 시 생성/저장은 일어나지 않음
		verify(reservationFactory, never()).create(any(), any(), any(), anyInt());
		verify(reservationRepository, never()).save(any());
	}
}