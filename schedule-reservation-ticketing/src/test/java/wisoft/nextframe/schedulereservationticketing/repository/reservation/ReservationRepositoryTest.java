package wisoft.nextframe.schedulereservationticketing.repository.reservation;

import static org.assertj.core.api.Assertions.*;

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
import wisoft.nextframe.schedulereservationticketing.builder.ReservationBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.ScheduleBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.StadiumBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.UserBuilder;
import wisoft.nextframe.schedulereservationticketing.config.TestContainersConfig;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.Stadium;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;
import wisoft.nextframe.schedulereservationticketing.repository.performance.PerformanceRepository;
import wisoft.nextframe.schedulereservationticketing.repository.schedule.ScheduleRepository;
import wisoft.nextframe.schedulereservationticketing.repository.stadium.StadiumRepository;
import wisoft.nextframe.schedulereservationticketing.repository.user.UserRepository;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestContainersConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReservationRepositoryTest {

	@Autowired
	private ReservationRepository reservationRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PerformanceRepository performanceRepository;
	@Autowired
	private StadiumRepository stadiumRepository;
	@Autowired
	private ScheduleRepository scheduleRepository;

	private User targetUser;
	private User otherUser;
	private Performance targetPerformance;
	private Performance otherPerformance;
	private Schedule targetSchedule;
	private Schedule otherSchedule;
	private Stadium stadium;

	@BeforeEach
	void setUp() {
		targetUser = userRepository.save(UserBuilder.builder().build());
		otherUser = userRepository.save(UserBuilder.builder().build());

		targetPerformance = performanceRepository.save(PerformanceBuilder.builder().build());
		otherPerformance = performanceRepository.save(PerformanceBuilder.builder().build());

		stadium = stadiumRepository.save(StadiumBuilder.builder().build());

		// 타켓 공연의 스케줄(TargetUser가 예약할 일정)
		targetSchedule = scheduleRepository.save(
			ScheduleBuilder.builder()
				.withPerformance(targetPerformance)
				.withStadium(stadium)
				.build()
		);

		// 다른 공연의 스케줄
		otherSchedule = scheduleRepository.save(
			ScheduleBuilder.builder()
				.withPerformance(otherPerformance)
				.withStadium(stadium)
				.build()
		);

		// 테스트 데이터: TargetUser가 TargetPerformance를 예약
		reservationRepository.save(
			ReservationBuilder.builder()
				.withUser(targetUser)
				.withSchedule(targetSchedule)
				.build()
		);

		// 테스트 데이터: OtherUser가 OtherPerformance를 예약
		reservationRepository.save(
			ReservationBuilder.builder()
				.withUser(otherUser)
				.withSchedule(otherSchedule)
				.build()
		);
	}

	@Nested
	class existsByUserAndPerformanceTest {

		@Test
		@DisplayName("사용자가 특정 공연을 예약한 경우 true를 반환한다")
		void existsByUserAndPerformance_WhenReservationExists() {
			// when
			final boolean exists = reservationRepository.existsByUserAndPerformance(targetUser, targetPerformance);

			// then
			assertThat(exists).isTrue();
		}

		@Test
		@DisplayName("사용자가 해당 공연을 예약한 내역이 없는 경우 false를 반환한다.")
		void existsByUserAndPerformance_WhenNoReservationExists() {
			// when
			final boolean exists = reservationRepository.existsByUserAndPerformance(targetUser, otherPerformance);

			// then
			assertThat(exists).isFalse();
		}

		@Test
		@DisplayName("다른 사용자가 특정 공연을 예약한 경우: FALSE 반환")
		void existsByUserAndPerformance_WhenOtherUserReserved() {
			// when
			final boolean exists = reservationRepository.existsByUserAndPerformance(otherUser, targetPerformance);

			// then
			assertThat(exists).isFalse();
		}
	}
}