package wisoft.nextframe.schedulereservationticketing.repository.seat;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import wisoft.nextframe.schedulereservationticketing.builder.PerformanceBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.ScheduleBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.SeatDefinitionBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.SeatStateBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.StadiumBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.StadiumSectionBuilder;
import wisoft.nextframe.schedulereservationticketing.config.TestContainersConfig;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.seat.SeatState;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.Stadium;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.StadiumSection;
import wisoft.nextframe.schedulereservationticketing.repository.performance.PerformanceRepository;
import wisoft.nextframe.schedulereservationticketing.repository.schedule.ScheduleRepository;
import wisoft.nextframe.schedulereservationticketing.repository.stadium.SeatDefinitionRepository;
import wisoft.nextframe.schedulereservationticketing.repository.stadium.StadiumRepository;
import wisoft.nextframe.schedulereservationticketing.repository.stadium.StadiumSectionRepository;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestContainersConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SeatStateRepositoryTest {

	@Autowired
	private SeatStateRepository seatStateRepository;
	@Autowired
	private ScheduleRepository scheduleRepository;
	@Autowired
	private StadiumSectionRepository stadiumSectionRepository;
	@Autowired
	private SeatDefinitionRepository seatDefinitionRepository;
	@Autowired
	private StadiumRepository stadiumRepository;
	@Autowired
	private PerformanceRepository performanceRepository;

	private Schedule schedule;

	private SeatDefinition seat1;
	private SeatDefinition seat2;

	@BeforeEach
	void setUp() {
		Stadium stadium = stadiumRepository.save(StadiumBuilder.builder().build());
		Performance performance = performanceRepository.save(PerformanceBuilder.builder().build());

		StadiumSection stadiumSection = stadiumSectionRepository.save(
			StadiumSectionBuilder.builder().withStadium(stadium).withSectionName("A").build());

		schedule = scheduleRepository.save(
			ScheduleBuilder.builder().withStadium(stadium).withPerformance(performance).build());

		seat1 = seatDefinitionRepository.save(
			SeatDefinitionBuilder.builder()
				.withStadiumSection(stadiumSection)
				.withRowNo(1).withColumnNo(1)
				.build()
		);
		seat2 = seatDefinitionRepository.save(
			SeatDefinitionBuilder.builder()
				.withStadiumSection(stadiumSection)
				.withRowNo(1).withColumnNo(2)
				.build()
		);

		seatStateRepository.save(
			SeatStateBuilder.builder()
				.withScheduleId(schedule.getId())
				.withSeatId(seat1.getId())
				.withSchedule(schedule)
				.withSeat(seat1)
				.withIsLocked(false)
				.build()
		);
		seatStateRepository.save(
			SeatStateBuilder.builder()
				.withScheduleId(schedule.getId())
				.withSeatId(seat2.getId())
				.withSchedule(schedule)
				.withSeat(seat2)
				.withIsLocked(true)
				.build()
		);
	}

	@Nested
	class findByScheduleIdAndSeatIdsTest {

		@Test
		@DisplayName("특정 스케줄 ID와 좌석 ID 목록으로 SeatState를 정확히 조회한다")
		void findByScheduleIdAndSeatIds_Success() {
			// given
			final UUID scheduleId = schedule.getId();
			final List<UUID> seatIds = Arrays.asList(
				seat1.getId(),
				seat2.getId()
			);

			// when
			final List<SeatState> resultList = seatStateRepository.findByScheduleIdAndSeatIds(scheduleId, seatIds);

			// then
			assertThat(resultList).hasSize(2);

			final List<UUID> resultSeatIds = resultList.stream()
				.map(seatState -> seatState.getId().getSeatId())
				.toList();
			assertThat(resultSeatIds)
				.containsExactlyInAnyOrder(seat1.getId(), seat2.getId());

			resultList.forEach(seatState -> {
				assertAll(
					() -> assertThat(seatState.getId().getScheduleId()).isEqualTo(scheduleId),
					() -> assertThat(seatState.getSchedule().getId()).isEqualTo(scheduleId)
				);
			});
		}

		@Test
		@DisplayName("요청된 좌석 ID 목록 중 일부만 존재할 경우, 존재하는 엔티티만 조회된다")
		void findByScheduleIdAndSeatIds_PartialMatch() {
			// given
			final UUID scheduleId = schedule.getId();
			final UUID nonExistentSeatId = UUID.randomUUID(); // 가짜 ID

			final List<UUID> seatIds = Arrays.asList(
				seat1.getId(),
				nonExistentSeatId
			);

			// when
			final List<SeatState> resultList = seatStateRepository.findByScheduleIdAndSeatIds(
				scheduleId,
				seatIds
			);

			// then
			assertThat(resultList).hasSize(1);
			assertThat(resultList.getFirst().getId().getSeatId()).isEqualTo(seat1.getId());
		}
	}
}
