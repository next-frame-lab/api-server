package wisoft.nextframe.schedulereservationticketing.repository.performance;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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
import wisoft.nextframe.schedulereservationticketing.builder.PerformanceBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.ScheduleBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.StadiumBuilder;
import wisoft.nextframe.schedulereservationticketing.dto.performance.performancelist.response.PerformanceSummaryResponse;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformanceGenre;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformanceType;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.Stadium;
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
		stadium = stadiumRepository.save(new StadiumBuilder().build());
		pageable = PageRequest.of(0, 32);
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
			.type(PerformanceType.CLASSIC)
			.genre(PerformanceGenre.MUSICAL)
			.adultOnly(false)
			.runningTime(runningTime)
			.imageUrl("http://example.com/phantom_of_the_opera.jpg")
			.description("파리 오페라 하우스를 배경으로 한 미스터리 로맨스")
			.build();

		// when
		performanceRepository.save(newPerformance);
		final Optional<Performance> foundPerformanceOptional = performanceRepository.findById(performanceId);

		// then
		assertThat(foundPerformanceOptional).isPresent();

		final Performance foundPerformance = foundPerformanceOptional.get();
		assertThat(foundPerformance.getId()).isEqualTo(performanceId);
		assertThat(foundPerformance.getName()).isEqualTo("오페라의 유령");
		assertThat(foundPerformance.getType()).isEqualTo(PerformanceType.CLASSIC);
		assertThat(foundPerformance.getGenre()).isEqualTo(PerformanceGenre.MUSICAL);
		assertThat(foundPerformance.getAdultOnly()).isFalse();
		assertThat(foundPerformance.getRunningTime()).isEqualTo(runningTime);
		assertThat(foundPerformance.getImageUrl()).isEqualTo("http://example.com/phantom_of_the_opera.jpg");
		assertThat(foundPerformance.getDescription()).isEqualTo("파리 오페라 하우스를 배경으로 한 미스터리 로맨스");
	}

	@Test
	@DisplayName("성공: 예매 가능한 공연을 정확히 조회한다")
	void findReservablePerformances_Success() {
		// given
		final LocalDateTime now = LocalDateTime.now();

		// 예매 가능한 공연 데이터 1개 저장(테스트만을 위한 고유한 공연 생성)
		final String uniqueName = "햄릿" + UUID.randomUUID();
		final Performance reservablePerf = performanceRepository.save(
			new PerformanceBuilder()
				.withName(uniqueName)
				.build());

		// 공연과 관련된 일정 데이터 2개 저장
		scheduleRepository.save(new ScheduleBuilder()
			.withPerformance(reservablePerf)
			.withStadium(stadium)
			.withPerformanceDatetime(now.plusDays(30))
			.withTicketOpenTime(now.minusDays(10))
			.withTicketCloseTime(now.plusDays(4))
			.build()
		);
		scheduleRepository.save(new ScheduleBuilder()
			.withPerformance(reservablePerf)
			.withStadium(stadium)
			.withPerformanceDatetime(now.plusDays(50))
			.withTicketOpenTime(now.minusDays(10))
			.withTicketCloseTime(now.plusDays(4))
			.build()
		);

		// when
		final Page<PerformanceSummaryResponse> resultPage = performanceRepository.findReservablePerformances(pageable);

		// then
		assertThat(resultPage.getContent()).isNotEmpty();

		final List<PerformanceSummaryResponse> foundedPerformancList = resultPage.getContent().stream()
			.filter(dto -> dto.getId().equals(reservablePerf.getId()))
			.toList();
		assertThat(foundedPerformancList).hasSize(1);

		final PerformanceSummaryResponse performanceSummaryResponse = foundedPerformancList.getFirst();
		assertThat(performanceSummaryResponse.getName()).isEqualTo(uniqueName);
		assertThat(performanceSummaryResponse.getStartDate()).isEqualTo(now.plusDays(30).toLocalDate());
		assertThat(performanceSummaryResponse.getEndDate()).isEqualTo(now.plusDays(50).toLocalDate());
	}

	@Test
	@DisplayName("실패: 예매 시작 전인 공연은 조회되지 않는다")
	void findReservablePerformances_Fail_WhenNotYetOpen() {
		// given
		final LocalDateTime now = LocalDateTime.now();
		// 아직 예매가 시작되지 않은 공연 데이터 저장(테스트만을 위한 고유한 공연 생성)
		final Performance notYetOpenPerf = performanceRepository.save(
			new PerformanceBuilder().withName("캣츠" + UUID.randomUUID()).build());

		// 공연과 관련된 일정 데이터 1개 저장
		scheduleRepository.save(new ScheduleBuilder()
			.withPerformance(notYetOpenPerf)
			.withStadium(stadium)
			.withTicketOpenTime(now.plusDays(1)) // 예매 시작이 미래
			.withTicketCloseTime(now.plusDays(20))
			.build());

		// When
		final Page<PerformanceSummaryResponse> resultPage = performanceRepository.findReservablePerformances(pageable);

		// Then
		final boolean found = resultPage.getContent().stream()
			.anyMatch(dto -> dto.getId().equals(notYetOpenPerf.getId()));

		assertThat(found).isFalse();
	}

	@Test
	@DisplayName("실패: 이미 예매가 마감된 공연은 조회되지 않는다")
	void findReservablePerformances_Fail_WhenClosed() {
		// given
		final LocalDateTime now = LocalDateTime.now();
		// 이미 예매가 마감된 공연 데이터 1개 저장(테스트만을 위한 고유한 공연 생성)
		final Performance closedPerf = performanceRepository.save(
			new PerformanceBuilder().withName("오페라의 유령" + UUID.randomUUID()).build());
		// 공연과 관련된 일정 데이터 1개 저장
		scheduleRepository.save(new ScheduleBuilder()
			.withPerformance(closedPerf)
			.withStadium(stadium)
			.withTicketOpenTime(now.minusDays(20))
			.withTicketCloseTime(now.minusDays(1)) // 예매 마감이 과거
			.build());

		// When
		final Page<PerformanceSummaryResponse> resultPage = performanceRepository.findReservablePerformances(pageable);

		// Then
		final boolean found = resultPage.getContent().stream()
			.anyMatch(dto -> dto.getId().equals(closedPerf.getId()));

		assertThat(found).isFalse();
	}
}