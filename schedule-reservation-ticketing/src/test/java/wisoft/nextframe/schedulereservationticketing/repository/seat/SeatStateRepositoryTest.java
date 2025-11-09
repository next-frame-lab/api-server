package wisoft.nextframe.schedulereservationticketing.repository.seat;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import wisoft.nextframe.schedulereservationticketing.builder.PerformanceBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.ScheduleBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.StadiumBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.StadiumSectionBuilder;
import wisoft.nextframe.schedulereservationticketing.config.AbstractIntegrationTest;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.seat.SeatState;
import wisoft.nextframe.schedulereservationticketing.entity.seat.SeatStateId;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.Stadium;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.StadiumSection;
import wisoft.nextframe.schedulereservationticketing.repository.performance.PerformanceRepository;
import wisoft.nextframe.schedulereservationticketing.repository.schedule.ScheduleRepository;
import wisoft.nextframe.schedulereservationticketing.repository.stadium.SeatDefinitionRepository;
import wisoft.nextframe.schedulereservationticketing.repository.stadium.StadiumRepository;
import wisoft.nextframe.schedulereservationticketing.repository.stadium.StadiumSectionRepository;

class SeatStateRepositoryTest extends AbstractIntegrationTest {

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
	private SeatDefinition seat3;

	@BeforeEach
	void setUp() {
		// 기본 데이터 저장 (참조 무결성 보장)
		Stadium savedStadium = stadiumRepository.save(new StadiumBuilder().build());
		Performance savedPerformance = performanceRepository.save(new PerformanceBuilder().build());
		StadiumSection stadiumSection = stadiumSectionRepository.save(
			new StadiumSectionBuilder().withStadium(savedStadium).build());
		schedule = scheduleRepository.save(
			new ScheduleBuilder()
				.withStadium(savedStadium)
				.withPerformance(savedPerformance)
				.build()
		);

		// 좌석 정의 3개 생성
		seat1 = seatDefinitionRepository.save(
			SeatDefinition.builder()
				.id(UUID.randomUUID())
				.rowNo(1)
				.columnNo(1)
				.stadiumSection(stadiumSection)
				.build()
		);
		seat2 = seatDefinitionRepository.save(
			SeatDefinition.builder()
				.id(UUID.randomUUID())
				.rowNo(1)
				.columnNo(2)
				.stadiumSection(stadiumSection)
				.build()
		);
		seat3 = seatDefinitionRepository.save(
			SeatDefinition.builder()
				.id(UUID.randomUUID())
				.rowNo(1)
				.columnNo(3)
				.stadiumSection(stadiumSection)
				.build()
		);

		// 좌석 상태 생성 및 저장
		SeatState ss1 = SeatState.builder()
			.id(SeatStateId.builder().scheduleId(schedule.getId()).seatId(seat1.getId()).build())
			.schedule(schedule)
			.seat(seat1)
			.isLocked(false)
			.build();
		SeatState ss2 = SeatState.builder()
			.id(SeatStateId.builder().scheduleId(schedule.getId()).seatId(seat2.getId()).build())
			.schedule(schedule)
			.seat(seat2)
			.isLocked(true)
			.build();
		SeatState ss3 = SeatState.builder()
			.id(SeatStateId.builder().scheduleId(schedule.getId()).seatId(seat3.getId()).build())
			.schedule(schedule)
			.seat(seat3)
			.isLocked(true)
			.build();

		seatStateRepository.saveAll(Arrays.asList(ss1, ss2, ss3));
	}

	@Test
	@DisplayName("findByScheduleIdAndSeatIds: 스케줄과 좌석 ID 목록으로 SeatState를 조회한다")
	void findByScheduleIdAndSeatIds_returnsMatchingSeatStates() {
		// when
		List<SeatState> found = seatStateRepository.findByScheduleIdAndSeatIds(
			schedule.getId(), List.of(seat1.getId(), seat3.getId())
		);

		// then
		assertThat(found)
			.hasSize(2)
			.extracting(ss -> ss.getId().getSeatId())
			.containsExactlyInAnyOrder(seat1.getId(), seat3.getId());
	}

	@Test
	@DisplayName("findByScheduleIdAndIsLockedTrue: 해당 스케줄에서 잠겨 있는 좌석만 조회한다")
	void findByScheduleIdAndIsLockedTrue_returnsOnlyLockedSeats() {
		// when
		List<SeatState> locked = seatStateRepository.findByScheduleIdAndIsLockedTrue(schedule.getId());

		// then
		assertThat(locked)
			.hasSize(2)
			.allMatch(SeatState::getIsLocked)
			.extracting(ss -> ss.getId().getSeatId())
			.containsExactlyInAnyOrder(seat2.getId(), seat3.getId());
	}
}