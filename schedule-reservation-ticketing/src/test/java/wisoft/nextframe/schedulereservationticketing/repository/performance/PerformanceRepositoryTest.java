package wisoft.nextframe.schedulereservationticketing.repository.performance;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import wisoft.nextframe.schedulereservationticketing.builder.PerformanceBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.PerformanceStatisticBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.ScheduleBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.StadiumBuilder;
import wisoft.nextframe.schedulereservationticketing.config.TestContainersConfig;
import wisoft.nextframe.schedulereservationticketing.dto.performance.performancelist.response.PerformanceSummaryResponse;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.Stadium;
import wisoft.nextframe.schedulereservationticketing.repository.schedule.ScheduleRepository;
import wisoft.nextframe.schedulereservationticketing.repository.stadium.StadiumRepository;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestContainersConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PerformanceRepositoryTest {

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
	private final LocalDateTime now = LocalDateTime.now();

	@BeforeEach
	void setUp() {
		stadium = stadiumRepository.save(StadiumBuilder.builder().build());
		pageable = PageRequest.of(0, 32);
	}

	@Nested
	class findReservablePerformancesTest {

		@Test
		@DisplayName("예매 가능한 공연을 정확히 조회한다")
		void findReservablePerformances_Success() {
			// given
			// 공연 데이터 1개 저장
			final Performance performance = performanceRepository.save(PerformanceBuilder.builder().build());

			// 공연의 예매 가능한 일정 데이터 2개 저장
			scheduleRepository.save(ScheduleBuilder.builder()
				.withPerformance(performance)
				.withStadium(stadium)
				.withPerformanceDatetime(now.plusDays(30)) // 30일 뒤 공연
				.build()
			);
			scheduleRepository.save(ScheduleBuilder.builder()
				.withPerformance(performance)
				.withStadium(stadium)
				.withPerformanceDatetime(now.plusDays(50)) // 50일 뒤 공연
				.build()
			);

			// when
			final Page<PerformanceSummaryResponse> resultPage = performanceRepository.findReservablePerformances(pageable);

			// then
			assertThat(resultPage.getTotalElements()).isEqualTo(1);

			final PerformanceSummaryResponse performanceSummaryResponse = resultPage.getContent().getFirst();
			assertThat(performanceSummaryResponse.id()).isEqualTo(performance.getId());
			assertThat(performanceSummaryResponse.startDate()).isEqualTo(now.plusDays(30).toLocalDate());
			assertThat(performanceSummaryResponse.endDate()).isEqualTo(now.plusDays(50).toLocalDate());
		}

		@Test
		@DisplayName("예매 시작 전인 공연은 조회되지 않는다")
		void findReservablePerformances_Fail_WhenNotYetOpen() {
			// given
			// 공연 데이터 1개 저장
			final Performance performance = performanceRepository.save(PerformanceBuilder.builder().build());

			// 공연의 예매 시작 전인 일정 데이터 1개 저장
			scheduleRepository.save(ScheduleBuilder.builder()
				.withPerformance(performance)
				.withStadium(stadium)
				.withTicketOpenTime(now.plusDays(10)) // 예매 시작 전
				.build());

			// when
			final Page<PerformanceSummaryResponse> resultPage = performanceRepository.findReservablePerformances(pageable);

			// then
			final boolean found = resultPage.getContent().stream()
				.anyMatch(dto -> dto.id().equals(performance.getId()));

			assertThat(found).isFalse();
		}

		@Test
		@DisplayName("예매가 마감된 공연은 조회되지 않는다")
		void findReservablePerformances_Fail_WhenClosed() {
			// given
			// 공연 데이터 1개 저장
			final Performance performance = performanceRepository.save(PerformanceBuilder.builder().build());

			// 공연의 예매 마감된 일정 데이터 1개 저장
			scheduleRepository.save(ScheduleBuilder.builder()
				.withPerformance(performance)
				.withStadium(stadium)
				.withTicketCloseTime(now.minusDays(10)) // 예매 마감
				.build());

			// when
			final Page<PerformanceSummaryResponse> resultPage = performanceRepository.findReservablePerformances(pageable);

			// then
			final boolean found = resultPage.getContent().stream()
				.anyMatch(dto -> dto.id().equals(performance.getId()));

			assertThat(found).isFalse();
		}
	}

	@Nested
	class findTop10PerformancesTest {

		@Test
		@DisplayName("인기 공연 10개를 조회수와 시작일 순으로 정확히 조회한다")
		void findTop10Performances_Success() {
			// given
			// 테스트 데이터 생성 (공연명, 조회수, 시작일)
			// 1~10위 데이터
			createPerformance("1위 공연", 1000, now.plusDays(10));
			createPerformance("2위 공연", 900, now.plusDays(10));
			createPerformance("3위 공연", 800, now.plusDays(10));
			// --- 동점자 데이터 (4, 5위) ---
			// 4위: 조회수는 같지만, 시작일이 더 빠름
			createPerformance("4위 공연 (동점-빠름)", 700, now.plusDays(5));
			// 5위: 조회수는 같지만, 시작일이 더 늦음
			createPerformance("5위 공연 (동점-늦음)", 700, now.plusDays(8));
			// ----------------------------
			createPerformance("6위 공연", 600, now.plusDays(10));
			createPerformance("7위 공연", 500, now.plusDays(10));
			createPerformance("8위 공연", 400, now.plusDays(10));
			createPerformance("9위 공연", 300, now.plusDays(10));
			createPerformance("10위 공연", 200, now.plusDays(10));

			// --- 조회에서 제외되어야 할 데이터 ---
			// 11순위 (TOP 10에 포함되지 않음)
			createPerformance("11위 공연(미포함)", 100, now.plusDays(10));
			// 조회수는 가장 높지만, 이미 종료된 공연 (미포함)
			createPerformance("종료된 인기공연(미포함)", 9999, now.minusDays(20));

			final Pageable topTenPageable = PageRequest.of(0, 10);

			// when
			final Page<PerformanceSummaryResponse> resultPage = performanceRepository.findTop10Performances(topTenPageable);

			// then
			assertThat(resultPage.getContent()).hasSize(10);

			// 2. 제외되어야 할 공연들이 조회되지 않았는지 확인
			assertThat(resultPage.getContent())
				.extracting(PerformanceSummaryResponse::name)
				.doesNotContain("11위 공연(미포함)", "종료된 인기공연(미포함)");

			// 3. 정렬 순서가 정확한지 확인한다. (조회수 DESC, 시작일 ASC)
			assertThat(resultPage.getContent())
				.extracting(PerformanceSummaryResponse::name)
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

		private void createPerformance(String name, int hit, LocalDateTime startDate) {
			Performance performance = performanceRepository.save(
				PerformanceBuilder.builder()
					.withName(name)
					.build()
			);

			performanceStatisticRepository.save(
				PerformanceStatisticBuilder.builder()
					.withPerformance(performance)
					.withHit(hit)
					.build()
			);

			scheduleRepository.save(
				ScheduleBuilder.builder()
					.withPerformance(performance)
					.withStadium(stadium)
					.withPerformanceDatetime(startDate)
					.build()
			);
		}
	}

	@Nested
	class findAdultOnlyByIdTest {
		@Test
		@DisplayName("성인 공연인지 아닌지 정확히 조회한다")
		void findAdultOnlyById_variousCases() {
			// given
			Performance adultOnlyPerf = performanceRepository.save(PerformanceBuilder.builder().withAdultOnly(true).build());
			Performance nonAdultPerf = performanceRepository.save(PerformanceBuilder.builder().withAdultOnly(false).build());

			// when
			Optional<Boolean> adultOnlyOpt = performanceRepository.findAdultOnlyById(adultOnlyPerf.getId());
			Optional<Boolean> nonAdultOpt = performanceRepository.findAdultOnlyById(nonAdultPerf.getId());

			// then
			assertThat(adultOnlyOpt).isPresent();
			assertThat(adultOnlyOpt.get()).isTrue();

			assertThat(nonAdultOpt).isPresent();
			assertThat(nonAdultOpt.get()).isFalse();
		}
	}
}