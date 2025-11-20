package wisoft.nextframe.schedulereservationticketing.service.seat;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import wisoft.nextframe.schedulereservationticketing.builder.SeatStateBuilder;
import wisoft.nextframe.schedulereservationticketing.dto.seat.seatstate.SeatStateListResponse;
import wisoft.nextframe.schedulereservationticketing.entity.seat.SeatState;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;
import wisoft.nextframe.schedulereservationticketing.repository.seat.SeatStateRepository;

@SpringBootTest(classes = {
	SeatStateService.class,
	SeatStateServiceTest.TestConfig.class,
	CacheAutoConfiguration.class
})
@EnableCaching
public class SeatStateServiceTest {

	@Autowired
	private SeatStateService seatStateService;

	@Autowired
	private SeatStateRepository seatStateRepository;

	@TestConfiguration
	static class TestConfig {
		@Bean
		@Primary
		public SeatStateRepository seatStateRepository() {
			return Mockito.mock(SeatStateRepository.class);
		}
	}

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