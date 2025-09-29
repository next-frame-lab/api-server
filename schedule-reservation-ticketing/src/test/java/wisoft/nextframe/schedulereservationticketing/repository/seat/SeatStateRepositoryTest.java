package wisoft.nextframe.schedulereservationticketing.repository.seat;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import wisoft.nextframe.schedulereservationticketing.builder.PerformanceBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.ScheduleBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.StadiumBuilder;
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

		// given: 테스트에 사용할 좌석 상태를 미리 저장
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
	@DisplayName("성공: 요청한 좌석들을 비관적 락을 걸어 정상적으로 조회한다")
	void findAndLockByScheduleIdAndSeatIds_Success() {
		// when: 새로운 메서드를 호출하여 좌석 상태를 조회 (이때 락이 걸림)
		List<SeatState> lockedSeats = seatStateRepository.findAndLockByScheduleIdAndSeatIds(
			savedSchedule.getId(),
			List.of(savedSeat1.getId(), savedSeat2.getId())
		);

		// then
		// 1. 요청한 좌석들이 정확하게 조회되었는지 확인
		assertThat(lockedSeats).hasSize(2);
		assertThat(lockedSeats).extracting(seatState -> seatState.getId().getSeatId())
			.containsExactlyInAnyOrder(savedSeat1.getId(), savedSeat2.getId());

		// 2. 조회된 엔티티에 PESSIMISTIC_WRITE 락이 걸려있는지 확인
		for (SeatState seat : lockedSeats) {
			LockModeType lockMode = entityManager.getLockMode(seat);
			assertThat(lockMode).isEqualTo(LockModeType.PESSIMISTIC_FORCE_INCREMENT);
		}
	}
}