package wisoft.nextframe.schedulereservationticketing.schedule.repository.performance;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import jakarta.transaction.Transactional;
import wisoft.nextframe.schedulereservationticketing.dto.performancelist.PerformanceSummaryDto;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformanceGenre;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformanceType;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.Stadium;
import wisoft.nextframe.schedulereservationticketing.repository.performance.PerformanceRepository;
import wisoft.nextframe.schedulereservationticketing.repository.schedule.ScheduleRepository;
import wisoft.nextframe.schedulereservationticketing.repository.stadium.StadiumRepository;

@SpringBootTest
@Transactional
class PerformanceRepositoryTest {

	@Autowired
	private PerformanceRepository performanceRepository;
	@Autowired
	private ScheduleRepository scheduleRepository;
	@Autowired
	private StadiumRepository stadiumRepository;

	private Stadium stadium;
	private Pageable pageable;

	@BeforeEach
	void setUp() {
		stadium = stadiumRepository.save(Stadium.builder().id(UUID.randomUUID()).name("대전예술의전당").address("대전 서구").build());
		pageable = PageRequest.of(0, 10);
	}

	@Test
	@DisplayName("성공: 새로운 공연을 저장하고 ID로 조회하면 성공한다.")
	void saveAndFindById_Success() {
		// given
		UUID performanceId = UUID.randomUUID();
		Duration runningTime = Duration.ofMinutes(150);
		Performance newPerformance = Performance.builder()
			.id(performanceId)
			.name("오페라의 유령")
			.type(PerformanceType.클래식)
			.genre(PerformanceGenre.뮤지컬)
			.adultOnly(false)
			.runningTime(runningTime)
			.imageUrl("http://example.com/phantom_of_the_opera.jpg")
			.description("파리 오페라 하우스를 배경으로 한 미스터리 로맨스")
			.build();

		// when
		performanceRepository.save(newPerformance);
		Optional<Performance> foundPerformanceOptional = performanceRepository.findById(performanceId);

		// then
		assertThat(foundPerformanceOptional).isPresent();

		Performance foundPerformance = foundPerformanceOptional.get();
		assertThat(foundPerformance.getId()).isEqualTo(performanceId);
		assertThat(foundPerformance.getName()).isEqualTo("오페라의 유령");
		assertThat(foundPerformance.getType()).isEqualTo(PerformanceType.클래식);
		assertThat(foundPerformance.getGenre()).isEqualTo(PerformanceGenre.뮤지컬);
		assertThat(foundPerformance.getAdultOnly()).isFalse();
		assertThat(foundPerformance.getRunningTime()).isEqualTo(runningTime);
		assertThat(foundPerformance.getImageUrl()).isEqualTo("http://example.com/phantom_of_the_opera.jpg");
		assertThat(foundPerformance.getDescription()).isEqualTo("파리 오페라 하우스를 배경으로 한 미스터리 로맨스");
	}

	@Test
	@DisplayName("성공: 예매 가능한 공연을 정확히 조회한다")
	void findReservablePerformances_Success() {
		// given: 예매 가능한 공연 데이터 1개 저장
		final LocalDateTime now = LocalDateTime.now();
		final Performance reservablePerf = performanceRepository.save(createPerformance("햄릿"));
		scheduleRepository.save(createSchedule(reservablePerf, stadium, now.minusDays(10), now.plusDays(10), LocalDate.of(2025, 9, 1).atStartOfDay()));
		scheduleRepository.save(createSchedule(reservablePerf, stadium, now.minusDays(10), now.plusDays(10), LocalDate.of(2025, 9, 30).atStartOfDay()));

		// when
		final Page<PerformanceSummaryDto> resultPage = performanceRepository.findReservablePerformances(pageable);

		// then
		assertThat(resultPage.getTotalElements()).isEqualTo(1);

		final PerformanceSummaryDto summaryDto = resultPage.getContent().getFirst();
		assertThat(summaryDto.getName()).isEqualTo("햄릿");
		assertThat(summaryDto.getStartDate()).isEqualTo(LocalDate.of(2025, 9, 1));
		assertThat(summaryDto.getEndDate()).isEqualTo(LocalDate.of(2025, 9, 30));
	}

	@Test
	@DisplayName("실패: 예매 시작 전인 공연은 조회되지 않는다")
	void findReservablePerformances_Fail_WhenNotYetOpen() {
		// given: 아직 예매가 시작되지 않은 공연 데이터 1개 저장
		final LocalDateTime now = LocalDateTime.now();
		final Performance notYetOpenPerf = performanceRepository.save(createPerformance("캣츠"));
		scheduleRepository.save(createSchedule(notYetOpenPerf, stadium, now.plusDays(1), now.plusDays(20), LocalDate.of(2025, 10, 1).atStartOfDay()));

		// When
		final Page<PerformanceSummaryDto> resultPage = performanceRepository.findReservablePerformances(pageable);

		// Then
		assertThat(resultPage.getTotalElements()).isZero();
		assertThat(resultPage.getContent()).isEmpty();
	}

	@Test
	@DisplayName("실패: 이미 예매가 마감된 공연은 조회되지 않는다")
	void findReservablePerformances_Fail_WhenClosed() {
		// given: 이미 예매가 마감된 공연 데이터 1개 저장
		final LocalDateTime now = LocalDateTime.now();
		final Performance closedPerf = performanceRepository.save(createPerformance("오페라의 유령"));
		scheduleRepository.save(createSchedule(closedPerf, stadium, now.minusDays(20), now.minusDays(1), LocalDate.of(2025, 10, 1).atStartOfDay()));

		// When
		final Page<PerformanceSummaryDto> resultPage = performanceRepository.findReservablePerformances(pageable);

		// Then
		assertThat(resultPage.getTotalElements()).isZero();
		assertThat(resultPage.getContent()).isEmpty();
	}

	// Performance 테스트 데이터 생성을 위한 헬퍼 메소드
	private Performance createPerformance(String name) {
		return Performance.builder()
			.id(UUID.randomUUID())
			.name(name)
			.type(PerformanceType.동요)
			.genre(PerformanceGenre.뮤지컬)
			.adultOnly(false)
			.build();
	}

	// Schedule 테스트 데이터 생성을 위한 헬퍼 메소드
	private Schedule createSchedule(Performance performance, Stadium stadium, LocalDateTime open, LocalDateTime close, LocalDateTime perfDate) {
		return Schedule.builder()
			.id(UUID.randomUUID())
			.performance(performance)
			.stadium(stadium)
			.ticketOpenTime(open)
			.ticketCloseTime(close)
			.performanceDatetime(perfDate)
			.build();
	}
}