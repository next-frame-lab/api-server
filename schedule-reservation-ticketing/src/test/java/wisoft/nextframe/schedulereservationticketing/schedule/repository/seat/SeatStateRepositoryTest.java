package wisoft.nextframe.schedulereservationticketing.schedule.repository.seat;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.transaction.Transactional;
import wisoft.nextframe.schedulereservationticketing.schedule.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.schedule.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.schedule.entity.seat.SeatState;
import wisoft.nextframe.schedulereservationticketing.schedule.entity.seat.SeatStateId;
import wisoft.nextframe.schedulereservationticketing.schedule.entity.stadium.SeatDefinition;
import wisoft.nextframe.schedulereservationticketing.schedule.entity.stadium.Stadium;
import wisoft.nextframe.schedulereservationticketing.schedule.entity.stadium.StadiumSection;
import wisoft.nextframe.schedulereservationticketing.schedule.repository.performance.PerformanceRepository;
import wisoft.nextframe.schedulereservationticketing.schedule.repository.schedule.ScheduleRepository;
import wisoft.nextframe.schedulereservationticketing.schedule.repository.stadium.SeatDefinitionRepository;
import wisoft.nextframe.schedulereservationticketing.schedule.repository.stadium.StadiumRepository;
import wisoft.nextframe.schedulereservationticketing.schedule.repository.stadium.StadiumSectionRepository;

@SpringBootTest
@Transactional
class SeatStateRepositoryTest {

	@Autowired
	private SeatStateRepository seatStateRepository;
	@Autowired
	private ScheduleRepository scheduleRepository;
	@Autowired
	private SeatDefinitionRepository seatDefinitionRepository;
	@Autowired
	private PerformanceRepository performanceRepository;
	@Autowired
	private StadiumSectionRepository stadiumSectionRepository;
	@Autowired
	private StadiumRepository stadiumRepository;
	private Schedule savedSchedule;
	private SeatDefinition savedSeat;

	@BeforeEach
	void setUp() {
		Stadium stadium = stadiumRepository.save(
			Stadium.builder().id(UUID.randomUUID()).name("올림픽공원 체조경기장").build());

		Performance performance = performanceRepository.save(
			Performance.builder().id(UUID.randomUUID()).name("아이유 콘서트").build());

		StadiumSection section = stadiumSectionRepository.save(
			StadiumSection.builder().id(UUID.randomUUID()).stadium(stadium).section("A").build());

		savedSchedule = scheduleRepository.save(
			Schedule.builder().id(UUID.randomUUID()).stadium(stadium).performance(performance)
				.performanceDatetime(LocalDateTime.now()).build());

		savedSeat = seatDefinitionRepository.save(
			SeatDefinition.builder().id(UUID.randomUUID()).stadium(stadium).stadiumSection(section).row(1).column(1).build());
	}


	@Test
	@DisplayName("새로운 좌석 상태를 저장하고 복합키로 조회하면 성공한다.")
	void saveAndFindById_test() {
		// given
		SeatStateId seatStateId = SeatStateId.builder()
			.scheduleId(savedSchedule.getId())
			.seatId(savedSeat.getId())
			.build();

		SeatState newSeatState = SeatState.builder()
			.id(seatStateId)
			.schedule(savedSchedule)
			.seat(savedSeat)
			.isLocked(true)
			.build();

		// when
		seatStateRepository.save(newSeatState);
		Optional<SeatState> foundSeatStateOptional = seatStateRepository.findById(seatStateId);

		// then
		assertThat(foundSeatStateOptional).isPresent();

		SeatState foundSeatState = foundSeatStateOptional.get();
		assertThat(foundSeatState.getId()).isEqualTo(seatStateId);
		assertThat(foundSeatState.getIsLocked()).isTrue();
		assertThat(foundSeatState.getSchedule().getId()).isEqualTo(savedSchedule.getId());
		assertThat(foundSeatState.getSeat().getId()).isEqualTo(savedSeat.getId());
	}
}