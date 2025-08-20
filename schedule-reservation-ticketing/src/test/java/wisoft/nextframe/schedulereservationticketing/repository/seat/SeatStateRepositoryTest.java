package wisoft.nextframe.schedulereservationticketing.repository.seat;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import wisoft.nextframe.schedulereservationticketing.builder.PerformanceBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.ScheduleBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.StadiumBuilder;
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

@SpringBootTest
@Transactional
class SeatStateRepositoryTest {

	@Autowired
	private EntityManager entityManager;
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
	private SeatDefinition savedSeat1;
	private SeatDefinition savedSeat2;

	@BeforeEach
	void setUp() {
		Stadium stadium = stadiumRepository.save(new StadiumBuilder().build());

		Performance performance = performanceRepository.save(new PerformanceBuilder().build());

		StadiumSection section = stadiumSectionRepository.save(
			StadiumSection.builder().id(UUID.randomUUID()).stadium(stadium).section("A").build());

		savedSchedule = scheduleRepository.save(
			new ScheduleBuilder().withPerformance(performance).withStadium(stadium).build());

		savedSeat1 = seatDefinitionRepository.save(
			SeatDefinition.builder().id(UUID.randomUUID()).stadiumSection(section).rowNo(1).columnNo(1).build());

		savedSeat2 = seatDefinitionRepository.save(
			SeatDefinition.builder().id(UUID.randomUUID()).stadiumSection(section).rowNo(1).columnNo(2).build());
	}

	@Test
	@DisplayName("성공: 새로운 좌석 상태를 저장하고 복합키로 조회하면 성공한다")
	void saveAndFindById_Success() {
		// given
		SeatStateId seatStateId = SeatStateId.builder()
			.scheduleId(savedSchedule.getId())
			.seatId(savedSeat1.getId())
			.build();

		SeatState newSeatState = SeatState.builder()
			.id(seatStateId)
			.schedule(savedSchedule)
			.seat(savedSeat1)
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
		assertThat(foundSeatState.getSeat().getId()).isEqualTo(savedSeat1.getId());
	}

	@Test
	@DisplayName("성공: 좌석 중 하나라도 잠겨있으면 ture를 반환한다")
	void existsByScheduleIdSeatIsLocked_Success1() {
		// given: 1번 좌석은 잠금(true), 2번 좌석은 잠금 해제(false) 상태로 저장
		final SeatState lockedSeatState = SeatState.builder()
			.id(new SeatStateId(savedSchedule.getId(), savedSeat1.getId()))
			.schedule(savedSchedule)
			.seat(savedSeat1)
			.isLocked(true) // 좌석 상태 true
			.build();
		seatStateRepository.save(lockedSeatState);

		final SeatState unlockedSeatState = SeatState.builder()
			.id(new SeatStateId(savedSchedule.getId(), savedSeat2.getId()))
			.schedule(savedSchedule)
			.seat(savedSeat2)
			.isLocked(false) // 좌석 상태 false
			.build();
		seatStateRepository.save(unlockedSeatState);

		// when: 잠긴 좌석과 잠기지 않은 좌석 ID를 모두 포함하여 조회
		boolean result = seatStateRepository.existsByScheduleIdSeatIsLocked(savedSchedule.getId(),
			List.of(savedSeat1.getId(), savedSeat2.getId())
		);

		// then: 결과는 true여야 한다
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("성공: 요청한 모든 좌석이 잠겨있지 않으면 false를 반환한다")
	void existsByScheduleIdSeatIsLocked_Success2() {
		// given: 1번, 2번 좌석 모두 잠금 해제(false) 상태로 저장
		SeatState unlockedSeatState1 = SeatState.builder()
			.id(new SeatStateId(savedSchedule.getId(), savedSeat1.getId()))
			.schedule(savedSchedule)
			.seat(savedSeat1)
			.isLocked(false) // 좌석 상태 false
			.build();
		seatStateRepository.save(unlockedSeatState1);

		SeatState unlockedSeatState2 = SeatState.builder()
			.id(new SeatStateId(savedSchedule.getId(), savedSeat2.getId()))
			.schedule(savedSchedule)
			.seat(savedSeat2)
			.isLocked(false) // 좌석 상태 false
			.build();
		seatStateRepository.save(unlockedSeatState2);

		// when: 잠금 해제된 좌석들의 ID로 조회
		boolean result = seatStateRepository.existsByScheduleIdSeatIsLocked(
			savedSchedule.getId(),
			List.of(savedSeat1.getId(), savedSeat2.getId())
		);

		// then: 결과는 false여야 한다
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("성공: 요청한 좌석들을 성공적으로 잠금 상태로 변경하고, 변경된 개수를 반환한다")
	void lockSeats_Success() {
		// given: 1번, 2번 좌석 모두 잠금 해제(false) 상태로 저장
		SeatState unlockedSeatState1 = SeatState.builder()
			.id(new SeatStateId(savedSchedule.getId(), savedSeat1.getId()))
			.schedule(savedSchedule)
			.seat(savedSeat1)
			.isLocked(false)
			.build();
		seatStateRepository.save(unlockedSeatState1);

		SeatState unlockedSeatState2 = SeatState.builder()
			.id(new SeatStateId(savedSchedule.getId(), savedSeat2.getId()))
			.schedule(savedSchedule)
			.seat(savedSeat2)
			.isLocked(false)
			.build();
		seatStateRepository.save(unlockedSeatState2);

		// when: lockSeats 메서드 호출
		int updatedCount = seatStateRepository.lockSeats(
			savedSchedule.getId(),
			List.of(savedSeat1.getId(), savedSeat2.getId())
		);

		entityManager.flush();
		entityManager.clear();

		// then: 현재 스케줄에 속한 1개의 row만 변경되었음을 확인
		assertThat(updatedCount).isEqualTo(2);

		// then: 실제 DB에서 좌석들의 상태가 true로 변경되었는지 확인
		SeatState foundSeatState1 = seatStateRepository.findById(unlockedSeatState1.getId()).orElseThrow();
		SeatState foundSeatState2 = seatStateRepository.findById(unlockedSeatState2.getId()).orElseThrow();

		assertThat(foundSeatState1.getIsLocked()).isTrue();
		assertThat(foundSeatState2.getIsLocked()).isTrue();
	}
}