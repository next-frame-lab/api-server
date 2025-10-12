package wisoft.nextframe.schedulereservationticketing.repository.performance;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import wisoft.nextframe.schedulereservationticketing.builder.PerformanceBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.ScheduleBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.StadiumBuilder;
import wisoft.nextframe.schedulereservationticketing.config.AbstractIntegrationTest;
import wisoft.nextframe.schedulereservationticketing.dto.performance.performancelist.response.PerformanceSummaryResponse;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformanceGenre;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformanceStatistic;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformanceType;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.Stadium;
import wisoft.nextframe.schedulereservationticketing.repository.schedule.ScheduleRepository;
import wisoft.nextframe.schedulereservationticketing.repository.stadium.StadiumRepository;

class PerformanceRepositoryTest extends AbstractIntegrationTest {

	@Autowired
	private PerformanceRepository performanceRepository;
	@Autowired
	private ScheduleRepository scheduleRepository;
	@Autowired
	private StadiumRepository stadiumRepository;
	@Autowired
	private PerformanceStatisticRepository performanceStatisticRepository;

	private Stadium stadium;
	private Pageable pageable;

	@BeforeEach
	void setUp() {
		stadium = stadiumRepository.save(new StadiumBuilder().build());
		pageable = PageRequest.of(0, 32);
	}

	@Test
	@DisplayName("성공: 예매 가능한 공연을 정확히 조회한다")
	void findReservablePerformances_Success() {
		// given
		final LocalDateTime now = LocalDateTime.now();

		// 예매 가능한 공연 데이터 1개 저장
		final Performance reservablePerf = performanceRepository.save(new PerformanceBuilder().withName("햄릿").build());
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
		assertThat(resultPage.getTotalElements()).isEqualTo(1);

		final PerformanceSummaryResponse performanceSummaryResponse = resultPage.getContent().getFirst();
		assertThat(performanceSummaryResponse.getName()).isEqualTo("햄릿");
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

	@Test
	@DisplayName("성공: 인기 공연 10개를 조회수와 시작일 순으로 정확히 조회한다")
	void findTop10Performances_Success() {
		// given
		final LocalDateTime now = LocalDateTime.now();
		// 테스트 데이터 생성 (조회수, 시작일, 종료일)
		// 1~10위 데이터
		createPerformance("1위 공연", 1000, now.plusDays(10), now.plusDays(20));
		createPerformance("2위 공연", 900, now.plusDays(10), now.plusDays(20));
		createPerformance("3위 공연", 800, now.plusDays(10), now.plusDays(20));
		// --- 동점자 데이터 (4, 5위) ---
		// 4위: 조회수는 같지만, 시작일이 더 빠름
		createPerformance("4위 공연 (동점-빠름)", 700, now.plusDays(5), now.plusDays(15));
		// 5위: 조회수는 같지만, 시작일이 더 늦음
		createPerformance("5위 공연 (동점-늦음)", 700, now.plusDays(8), now.plusDays(18));
		// ----------------------------
		createPerformance("6위 공연", 600, now.plusDays(10), now.plusDays(20));
		createPerformance("7위 공연", 500, now.plusDays(10), now.plusDays(20));
		createPerformance("8위 공연", 400, now.plusDays(10), now.plusDays(20));
		createPerformance("9위 공연", 300, now.plusDays(10), now.plusDays(20));
		createPerformance("10위 공연", 200, now.plusDays(10), now.plusDays(20));

		// --- 조회에서 제외되어야 할 데이터 ---
		// 11순위 (TOP 10에 포함되지 않음)
		createPerformance("11위 공연(미포함)", 100, now.plusDays(10), now.plusDays(20));
		// 조회수는 가장 높지만, 이미 종료된 공연 (미포함)
		createPerformance("종료된 인기공연(미포함)", 9999, now.minusDays(20), now.minusDays(10));

		Pageable topTenPageable = PageRequest.of(0, 10);

		// when
		final Page<PerformanceSummaryResponse> resultPage = performanceRepository.findTop10Performances(topTenPageable);

		// then
		// 1. 결과는 정확히 10개여야 한다.
		assertThat(resultPage.getContent()).hasSize(10);

		// 2. 제외되어야 할 공연들이 없는지 확인한다.
		assertThat(resultPage.getContent())
			.extracting(PerformanceSummaryResponse::getName)
			.doesNotContain("11위 공연(미포함)", "종료된 인기공연(미포함)");

		// 3. 정렬 순서가 정확한지 확인한다. (조회수 DESC, 시작일 ASC)
		assertThat(resultPage.getContent())
			.extracting(PerformanceSummaryResponse::getName)
			.containsExactly(
				"1위 공연",
				"2위 공연",
				"3위 공연",
				"4위 공연 (동점-빠름)", // 동점자 중 시작일이 빠른 공연
				"5위 공연 (동점-늦음)", // 동점자 중 시작일이 늦은 공연
				"6위 공연",
				"7위 공연",
				"8위 공연",
				"9위 공연",
				"10위 공연"
			);
	}

	private void createPerformance(String name, int hit, LocalDateTime startDate, LocalDateTime endDate) {
		Performance performance = performanceRepository.save(
			Performance.builder()
				.id(UUID.randomUUID())
				.name(name)
				.type(PerformanceType.CLASSIC)
				.genre(PerformanceGenre.PLAY)
				.adultOnly(false)
				.build()
		);
		performanceStatisticRepository.save(
			PerformanceStatistic.builder()
				.performance(performance) // 연관관계 설정
				.hit(hit)
				.build()
		);
		scheduleRepository.save(
			Schedule.builder()
				.id(UUID.randomUUID())
				.performance(performance)
				.stadium(stadium)
				.performanceDatetime(startDate)
				.build()
		);
		// endDate가 startDate와 다른 경우, 마지막 일정을 하나 더 추가
		if (!startDate.isEqual(endDate)) {
			scheduleRepository.save(
				Schedule.builder()
					.id(UUID.randomUUID())
					.performance(performance)
					.stadium(stadium)
					.performanceDatetime(endDate)
					.build()
			);
		}
	}
}