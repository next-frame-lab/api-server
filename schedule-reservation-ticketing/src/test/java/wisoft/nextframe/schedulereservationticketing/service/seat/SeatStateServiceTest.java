package wisoft.nextframe.schedulereservationticketing.service.seat;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import wisoft.nextframe.schedulereservationticketing.builder.SeatStateBuilder;
import wisoft.nextframe.schedulereservationticketing.config.TestCacheConfig;
import wisoft.nextframe.schedulereservationticketing.dto.seat.seatstate.SeatStateListResponse;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.seat.SeatState;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;
import wisoft.nextframe.schedulereservationticketing.repository.schedule.ScheduleRepository;
import wisoft.nextframe.schedulereservationticketing.repository.seat.SeatStateRepository;

@SpringBootTest(classes = {SeatStateService.class})
@Import(TestCacheConfig.class)
public class SeatStateServiceTest {

	@Autowired
	private SeatStateService seatStateService;

	@MockitoBean
	private SeatStateRepository seatStateRepository;

	@MockitoBean
	private ScheduleRepository scheduleRepository;

	@Nested
	class getSeatStatesTest {

		@Test
		@DisplayName("잠긴 좌석 목록을 정확히 캐싱하여 조회한다")
		void getSeatStates_successAndCaching() {
			// given
			final UUID scheduleId = UUID.randomUUID();

			SeatDefinition mockSeatDefinition = Mockito.mock(SeatDefinition.class);
			final SeatState seatState = SeatStateBuilder.builder()
				.withScheduleId(scheduleId)
				.withIsLocked(true)
				.withSeat(mockSeatDefinition)
				.build();

			Schedule mockSchedule = Mockito.mock(Schedule.class);
			given(scheduleRepository.findById(scheduleId))
				.willReturn(Optional.of(mockSchedule));

			given(seatStateRepository.findByScheduleIdAndIsLockedTrue(scheduleId))
				.willReturn(List.of(seatState));

			// when
			// 첫 번째 호출
			final SeatStateListResponse firstResult = seatStateService.getSeatStates(scheduleId);
			// 두 번째 호출(Cache 조회)
			final SeatStateListResponse secondResult = seatStateService.getSeatStates(scheduleId);

			// then
			assertThat(firstResult).isNotNull();
			assertThat(secondResult).isNotNull();
			assertThat(firstResult.seats()).hasSize(1);

			// 검증: Repository가 1번만 호출되어야 함
			verify(seatStateRepository, times(1)).findByScheduleIdAndIsLockedTrue(scheduleId);
		}
	}
}