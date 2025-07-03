package wisoft.nextframe.schedulereservationticketing.repository.schedule;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import wisoft.nextframe.schedulereservationticketing.builder.PerformanceBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.StadiumBuilder;
import wisoft.nextframe.schedulereservationticketing.config.AbstractIntegrationTest;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.Stadium;
import wisoft.nextframe.schedulereservationticketing.repository.performance.PerformanceRepository;
import wisoft.nextframe.schedulereservationticketing.repository.stadium.StadiumRepository;

class ScheduleRepositoryTest extends AbstractIntegrationTest {

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
		savedPerformance = performanceRepository.save(new PerformanceBuilder().build());

		savedStadium = stadiumRepository.save(new StadiumBuilder().build());
	}

	@Test
	@DisplayName("성공: 새로운 공연 일정을 저장하고 ID로 조회하면 성공한다")
	void saveAndFindById_Success() {
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