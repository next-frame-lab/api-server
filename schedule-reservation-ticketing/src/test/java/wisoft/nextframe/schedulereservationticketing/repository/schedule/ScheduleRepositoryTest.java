package wisoft.nextframe.schedulereservationticketing.repository.schedule;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.transaction.Transactional;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.Stadium;
import wisoft.nextframe.schedulereservationticketing.repository.performance.PerformanceRepository;
import wisoft.nextframe.schedulereservationticketing.repository.schedule.ScheduleRepository;
import wisoft.nextframe.schedulereservationticketing.repository.stadium.StadiumRepository;

@SpringBootTest
@Transactional
class ScheduleRepositoryTest {

	@Autowired
	private ScheduleRepository scheduleRepository;
	@Autowired
	private PerformanceRepository performanceRepository;
	@Autowired
	private StadiumRepository stadiumRepository;
	private Performance savedPerformance;
	private Stadium savedStadium;

	@BeforeEach
	void setUp() {
		savedPerformance = performanceRepository.save(
			Performance.builder().id(UUID.randomUUID()).name("캣츠").adultOnly(true).runningTime(Duration.ofMinutes(140)).build());

		savedStadium = stadiumRepository.save(
			Stadium.builder().id(UUID.randomUUID()).name("세종문화회관").address("서울특별시 종로구 세종대로 175").build());
	}

	@Test
	@DisplayName("새로운 공연 일정을 저장하고 ID로 조회하면 성공한다.")
	void saveAndFindById_test() {
		// given
		UUID scheduleId = UUID.randomUUID();
		LocalDateTime performanceTime = LocalDateTime.of(2025, 12, 25, 19, 30);
		LocalDateTime openTime = LocalDateTime.of(2025, 11, 1, 14, 0);
		LocalDateTime closeTime = LocalDateTime.of(2025, 12, 24, 17, 0);

		Schedule newSchedule = Schedule.builder()
			.id(scheduleId)
			.performance(savedPerformance)
			.stadium(savedStadium)
			.performanceDatetime(performanceTime)
			.ticketOpenTime(openTime)
			.ticketCloseTime(closeTime)
			.build();

		// when
		scheduleRepository.save(newSchedule);
		Optional<Schedule> foundScheduleOptional = scheduleRepository.findById(scheduleId);

		// then
		assertThat(foundScheduleOptional).isPresent();

		Schedule foundSchedule = foundScheduleOptional.get();
		assertThat(foundSchedule.getId()).isEqualTo(scheduleId);
		assertThat(foundSchedule.getPerformanceDatetime()).isEqualTo(performanceTime);
		assertThat(foundSchedule.getTicketOpenTime()).isEqualTo(openTime);
		assertThat(foundSchedule.getTicketCloseTime()).isEqualTo(closeTime);
		assertThat(foundSchedule.getPerformance().getId()).isEqualTo(savedPerformance.getId());
		assertThat(foundSchedule.getStadium().getId()).isEqualTo(savedStadium.getId());
	}
}