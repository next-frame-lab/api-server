package wisoft.nextframe.schedulereservationticketing.repository.performance;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
import wisoft.nextframe.schedulereservationticketing.config.db.QueryDslConfig;
import wisoft.nextframe.schedulereservationticketing.dto.performance.performancelist.response.PerformanceSummaryResponse;
import wisoft.nextframe.schedulereservationticketing.dto.performance.search.request.PerformanceSearchCondition;
import wisoft.nextframe.schedulereservationticketing.dto.performance.search.request.PerformanceSearchSort;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.Stadium;
import wisoft.nextframe.schedulereservationticketing.repository.schedule.ScheduleRepository;
import wisoft.nextframe.schedulereservationticketing.repository.stadium.StadiumRepository;

@DataJpaTest
@ActiveProfiles("test")
@Import({TestContainersConfig.class, QueryDslConfig.class, QueryDslPerformanceSearchAdapter.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class QueryDslPerformanceSearchAdapterTest {

	@Autowired
	private QueryDslPerformanceSearchAdapter searchAdapter;

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
	@DisplayName("키워드 검색 테스트")
	class KeywordSearchTest {

		@Test
		@DisplayName("키워드로 공연명을 검색하면 일치하는 공연만 조회된다")
		void search_withKeyword_returnsMatchingPerformances() {
			// given
			createReservablePerformance("햄릿");
			createReservablePerformance("오페라의 유령");
			createReservablePerformance("맥베스");

			PerformanceSearchCondition condition = PerformanceSearchCondition.of("햄릿", PerformanceSearchSort.LATEST, pageable);

			// when
			Page<PerformanceSummaryResponse> result = searchAdapter.search(condition);

			// then
			assertThat(result.getContent()).hasSize(1);
			assertThat(result.getContent().getFirst().name()).isEqualTo("햄릿");
		}

		@Test
		@DisplayName("키워드가 공연명의 일부와 일치하면 조회된다 (부분 일치)")
		void search_withPartialKeyword_returnsMatchingPerformances() {
			// given
			createReservablePerformance("오페라의 유령");
			createReservablePerformance("오페라 갈라쇼");
			createReservablePerformance("맥베스");

			PerformanceSearchCondition condition = PerformanceSearchCondition.of("오페라", PerformanceSearchSort.LATEST, pageable);

			// when
			Page<PerformanceSummaryResponse> result = searchAdapter.search(condition);

			// then
			assertThat(result.getContent()).hasSize(2);
			assertThat(result.getContent())
				.extracting(PerformanceSummaryResponse::name)
				.containsExactlyInAnyOrder("오페라의 유령", "오페라 갈라쇼");
		}

		@Test
		@DisplayName("키워드 검색은 대소문자를 구분하지 않는다")
		void search_caseInsensitive() {
			// given
			createReservablePerformance("Hamlet");

			PerformanceSearchCondition condition = PerformanceSearchCondition.of("hamlet", PerformanceSearchSort.LATEST, pageable);

			// when
			Page<PerformanceSummaryResponse> result = searchAdapter.search(condition);

			// then
			assertThat(result.getContent()).hasSize(1);
			assertThat(result.getContent().getFirst().name()).isEqualTo("Hamlet");
		}

		@Test
		@DisplayName("키워드가 null이면 전체 예매 가능한 공연이 조회된다")
		void search_withNullKeyword_returnsAllReservablePerformances() {
			// given
			createReservablePerformance("햄릿");
			createReservablePerformance("오페라의 유령");
			createReservablePerformance("맥베스");

			PerformanceSearchCondition condition = PerformanceSearchCondition.of(null, PerformanceSearchSort.LATEST, pageable);

			// when
			Page<PerformanceSummaryResponse> result = searchAdapter.search(condition);

			// then
			assertThat(result.getContent()).hasSize(3);
		}

		@Test
		@DisplayName("일치하는 공연이 없으면 빈 결과를 반환한다")
		void search_noMatch_returnsEmptyPage() {
			// given
			createReservablePerformance("햄릿");

			PerformanceSearchCondition condition = PerformanceSearchCondition.of("존재하지않는공연", PerformanceSearchSort.LATEST, pageable);

			// when
			Page<PerformanceSummaryResponse> result = searchAdapter.search(condition);

			// then
			assertThat(result.getContent()).isEmpty();
			assertThat(result.getTotalElements()).isZero();
		}
	}

	@Nested
	@DisplayName("예매 필터 테스트")
	class ReservationFilterTest {

		@Test
		@DisplayName("예매 기간이 아닌 공연은 검색 결과에서 제외된다")
		void search_excludesNonReservablePerformances() {
			// given
			// 예매 가능한 공연
			createReservablePerformance("예매 가능 공연");

			// 예매 시작 전 공연
			Performance notYetOpen = performanceRepository.save(
				PerformanceBuilder.builder().withName("예매 시작 전 공연").build()
			);
			scheduleRepository.save(ScheduleBuilder.builder()
				.withPerformance(notYetOpen)
				.withStadium(stadium)
				.withTicketOpenTime(now.plusDays(10))
				.withTicketCloseTime(now.plusDays(30))
				.build());

			// 예매 마감된 공연
			Performance closed = performanceRepository.save(
				PerformanceBuilder.builder().withName("예매 마감 공연").build()
			);
			scheduleRepository.save(ScheduleBuilder.builder()
				.withPerformance(closed)
				.withStadium(stadium)
				.withTicketOpenTime(now.minusDays(30))
				.withTicketCloseTime(now.minusDays(1))
				.build());

			PerformanceSearchCondition condition = PerformanceSearchCondition.of(null, PerformanceSearchSort.LATEST, pageable);

			// when
			Page<PerformanceSummaryResponse> result = searchAdapter.search(condition);

			// then
			assertThat(result.getContent()).hasSize(1);
			assertThat(result.getContent().getFirst().name()).isEqualTo("예매 가능 공연");
		}
	}

	@Nested
	@DisplayName("정렬 테스트")
	class SortTest {

		@Test
		@DisplayName("LATEST 정렬: 공연 시작일이 최신인 순서로 조회된다")
		void search_sortByLatest() {
			// given
			createReservablePerformanceWithDate("오래된 공연", now.plusDays(5));
			createReservablePerformanceWithDate("최신 공연", now.plusDays(30));
			createReservablePerformanceWithDate("중간 공연", now.plusDays(15));

			PerformanceSearchCondition condition = PerformanceSearchCondition.of(null, PerformanceSearchSort.LATEST, pageable);

			// when
			Page<PerformanceSummaryResponse> result = searchAdapter.search(condition);

			// then
			assertThat(result.getContent()).hasSize(3);
			assertThat(result.getContent())
				.extracting(PerformanceSummaryResponse::name)
				.containsExactly("최신 공연", "중간 공연", "오래된 공연");
		}

		@Test
		@DisplayName("DATE_ASC 정렬: 공연 시작일 오름차순으로 조회된다")
		void search_sortByDateAsc() {
			// given
			createReservablePerformanceWithDate("늦은 공연", now.plusDays(30));
			createReservablePerformanceWithDate("빠른 공연", now.plusDays(5));
			createReservablePerformanceWithDate("중간 공연", now.plusDays(15));

			PerformanceSearchCondition condition = PerformanceSearchCondition.of(null, PerformanceSearchSort.DATE_ASC, pageable);

			// when
			Page<PerformanceSummaryResponse> result = searchAdapter.search(condition);

			// then
			assertThat(result.getContent())
				.extracting(PerformanceSummaryResponse::name)
				.containsExactly("빠른 공연", "중간 공연", "늦은 공연");
		}

		@Test
		@DisplayName("DATE_DESC 정렬: 공연 시작일 내림차순으로 조회된다")
		void search_sortByDateDesc() {
			// given
			createReservablePerformanceWithDate("빠른 공연", now.plusDays(5));
			createReservablePerformanceWithDate("늦은 공연", now.plusDays(30));
			createReservablePerformanceWithDate("중간 공연", now.plusDays(15));

			PerformanceSearchCondition condition = PerformanceSearchCondition.of(null, PerformanceSearchSort.DATE_DESC, pageable);

			// when
			Page<PerformanceSummaryResponse> result = searchAdapter.search(condition);

			// then
			assertThat(result.getContent())
				.extracting(PerformanceSummaryResponse::name)
				.containsExactly("늦은 공연", "중간 공연", "빠른 공연");
		}

		@Test
		@DisplayName("HIT_DESC 정렬: 조회수 내림차순으로 조회된다")
		void search_sortByHitDesc() {
			// given
			createReservablePerformanceWithHit("인기 공연", 1000);
			createReservablePerformanceWithHit("보통 공연", 500);
			createReservablePerformanceWithHit("비인기 공연", 100);

			PerformanceSearchCondition condition = PerformanceSearchCondition.of(null, PerformanceSearchSort.HIT_DESC, pageable);

			// when
			Page<PerformanceSummaryResponse> result = searchAdapter.search(condition);

			// then
			assertThat(result.getContent())
				.extracting(PerformanceSummaryResponse::name)
				.containsExactly("인기 공연", "보통 공연", "비인기 공연");
		}

		@Test
		@DisplayName("STAR_DESC 정렬: 평균 별점 내림차순으로 조회된다")
		void search_sortByStarDesc() {
			// given
			createReservablePerformanceWithStar("별점 높은 공연", new BigDecimal("4.5"));
			createReservablePerformanceWithStar("별점 중간 공연", new BigDecimal("3.0"));
			createReservablePerformanceWithStar("별점 낮은 공연", new BigDecimal("1.5"));

			PerformanceSearchCondition condition = PerformanceSearchCondition.of(null, PerformanceSearchSort.STAR_DESC, pageable);

			// when
			Page<PerformanceSummaryResponse> result = searchAdapter.search(condition);

			// then
			assertThat(result.getContent())
				.extracting(PerformanceSummaryResponse::name)
				.containsExactly("별점 높은 공연", "별점 중간 공연", "별점 낮은 공연");
		}

		@Test
		@DisplayName("HIT_DESC 정렬: 조회수 동점 시 시작일 오름차순으로 2차 정렬된다")
		void search_sortByHitDesc_tieBreakByDateAsc() {
			// given
			createReservablePerformanceWithHitAndDate("늦은 동점 공연", 500, now.plusDays(20));
			createReservablePerformanceWithHitAndDate("빠른 동점 공연", 500, now.plusDays(5));
			createReservablePerformanceWithHitAndDate("1위 공연", 1000, now.plusDays(10));

			PerformanceSearchCondition condition = PerformanceSearchCondition.of(null, PerformanceSearchSort.HIT_DESC, pageable);

			// when
			Page<PerformanceSummaryResponse> result = searchAdapter.search(condition);

			// then
			assertThat(result.getContent())
				.extracting(PerformanceSummaryResponse::name)
				.containsExactly("1위 공연", "빠른 동점 공연", "늦은 동점 공연");
		}
	}

	@Nested
	@DisplayName("페이지네이션 테스트")
	class PaginationTest {

		@Test
		@DisplayName("페이지 크기에 맞게 결과가 분할된다")
		void search_paginationWorks() {
			// given
			for (int i = 0; i < 5; i++) {
				createReservablePerformance("공연 " + i);
			}

			Pageable firstPage = PageRequest.of(0, 2);
			PerformanceSearchCondition condition = PerformanceSearchCondition.of(null, PerformanceSearchSort.LATEST, firstPage);

			// when
			Page<PerformanceSummaryResponse> result = searchAdapter.search(condition);

			// then
			assertThat(result.getContent()).hasSize(2);
			assertThat(result.getTotalElements()).isEqualTo(5);
			assertThat(result.getTotalPages()).isEqualTo(3);
			assertThat(result.hasNext()).isTrue();
		}

		@Test
		@DisplayName("두 번째 페이지를 정확히 조회한다")
		void search_secondPage() {
			// given
			for (int i = 0; i < 5; i++) {
				createReservablePerformanceWithDate("공연 " + i, now.plusDays(30 - i));
			}

			Pageable secondPage = PageRequest.of(1, 2);
			PerformanceSearchCondition condition = PerformanceSearchCondition.of(null, PerformanceSearchSort.LATEST, secondPage);

			// when
			Page<PerformanceSummaryResponse> result = searchAdapter.search(condition);

			// then
			assertThat(result.getContent()).hasSize(2);
			assertThat(result.getNumber()).isEqualTo(1);
			assertThat(result.hasPrevious()).isTrue();
			assertThat(result.hasNext()).isTrue();
		}
	}

	@Nested
	@DisplayName("키워드 + 정렬 복합 테스트")
	class CombinedSearchTest {

		@Test
		@DisplayName("키워드 검색과 조회수 정렬을 동시에 적용한다")
		void search_withKeywordAndHitSort() {
			// given
			createReservablePerformanceWithHit("뮤지컬 캣츠", 100);
			createReservablePerformanceWithHit("뮤지컬 위키드", 500);
			createReservablePerformanceWithHit("연극 햄릿", 1000);  // 키워드 불일치

			PerformanceSearchCondition condition = PerformanceSearchCondition.of("뮤지컬", PerformanceSearchSort.HIT_DESC, pageable);

			// when
			Page<PerformanceSummaryResponse> result = searchAdapter.search(condition);

			// then
			assertThat(result.getContent()).hasSize(2);
			assertThat(result.getContent())
				.extracting(PerformanceSummaryResponse::name)
				.containsExactly("뮤지컬 위키드", "뮤지컬 캣츠");
		}
	}

	// === 헬퍼 메서드 ===

	private Performance createReservablePerformance(String name) {
		return createReservablePerformanceWithDate(name, now.plusDays(20));
	}

	private Performance createReservablePerformanceWithDate(String name, LocalDateTime performanceDatetime) {
		Performance performance = performanceRepository.save(
			PerformanceBuilder.builder().withName(name).build()
		);

		scheduleRepository.save(ScheduleBuilder.builder()
			.withPerformance(performance)
			.withStadium(stadium)
			.withPerformanceDatetime(performanceDatetime)
			.build());

		return performance;
	}

	private void createReservablePerformanceWithHit(String name, int hit) {
		createReservablePerformanceWithHitAndDate(name, hit, now.plusDays(20));
	}

	private void createReservablePerformanceWithHitAndDate(String name, int hit, LocalDateTime performanceDatetime) {
		Performance performance = createReservablePerformanceWithDate(name, performanceDatetime);

		performanceStatisticRepository.save(
			PerformanceStatisticBuilder.builder()
				.withPerformance(performance)
				.withHit(hit)
				.build()
		);
	}

	private void createReservablePerformanceWithStar(String name, BigDecimal averageStar) {
		Performance performance = createReservablePerformance(name);

		performanceStatisticRepository.save(
			PerformanceStatisticBuilder.builder()
				.withPerformance(performance)
				.withAverageStar(averageStar)
				.build()
		);
	}
}
