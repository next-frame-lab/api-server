package wisoft.nextframe.schedulereservationticketing.repository.performance;

import static wisoft.nextframe.schedulereservationticketing.entity.performance.QPerformance.*;
import static wisoft.nextframe.schedulereservationticketing.entity.performance.QPerformanceStatistic.*;
import static wisoft.nextframe.schedulereservationticketing.entity.schedule.QSchedule.*;
import static wisoft.nextframe.schedulereservationticketing.entity.stadium.QStadium.*;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTemplate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.dto.performance.performancelist.response.PerformanceSummaryResponse;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.QSchedule;

/**
 * {@link PerformanceRepositoryCustom}의 QueryDSL 구현체.
 *
 * <p>복잡한 집계·서브쿼리가 필요한 공연 조회 쿼리를 타입 안전하게 처리합니다.
 * {@link PageableExecutionUtils}를 사용하여 불필요한 count 쿼리를 지연 실행합니다.</p>
 */
@Repository
@RequiredArgsConstructor
public class PerformanceRepositoryImpl implements PerformanceRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	/**
	 * 현재 예매 가능한 공연 목록을 DTO로 직접 조회합니다.
	 *
	 * <p>이 메소드는 다음 조건을 만족하는 공연을 찾습니다:</p>
	 * <ul>
	 *     <li>{@code EXISTS} 서브쿼리를 사용하여 현재 시간이 티켓 판매 기간에 포함되는 공연만 필터링합니다.</li>
	 *     <li>{@code GROUP BY}를 사용하여 각 공연(공연+공연장 기준)의 시작일과 종료일을
	 *         연결된 모든 일정의 가장 빠른 날짜({@code MIN})와 가장 늦은 날짜({@code MAX})로 계산합니다.</li>
	 * </ul>
	 *
	 * @param pageable 페이징 정보(페이지 번호, 페이지 크기)
	 * @return {@link PerformanceSummaryResponse}의 페이지 객체
	 */
	@Override
	public Page<PerformanceSummaryResponse> findReservablePerformances(final Pageable pageable) {
		final QSchedule subSchedule = new QSchedule("subSchedule");
		final LocalDateTime now = LocalDateTime.now();

		// 컨텐츠 쿼리: 예매 가능한 공연을 DTO로 프로젝션
		final List<PerformanceSummaryResponse> content = queryFactory
			.select(performanceSummaryProjection())
			.from(schedule)
			.join(schedule.performance, performance)
			.join(schedule.stadium, stadium)
			.where(ticketOnSale(subSchedule, now))
			.groupBy(
				performance.id,
				performance.name,
				performance.imageUrl,
				performance.type,
				performance.genre,
				stadium.name,
				performance.adultOnly
			)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		// count 쿼리: content의 GROUP BY(공연+공연장) 기준과 일치하도록 그룹 수를 집계
		// QueryDSL JPA는 FROM 절 서브쿼리를 지원하지 않으므로 그룹 결과를 조회 후 size()로 총 수를 계산
		return PageableExecutionUtils.getPage(content, pageable,
			() -> queryFactory
				.select(performance.id)
				.from(schedule)
				.join(schedule.performance, performance)
				.join(schedule.stadium, stadium)
				.where(ticketOnSale(subSchedule, now))
				.groupBy(performance.id, stadium.name)
				.fetch()
				.size()
		);
	}

	/**
	 * 조회수(hit)가 높은 인기 공연을 DTO로 직접 조회합니다.
	 *
	 * <p>이 메소드는 다음 조건을 만족하는 공연을 찾습니다:</p>
	 * <ul>
	 *     <li>{@code PerformanceStatistic} 테이블과 JOIN하여 조회수(hit) 정보를 가져옵니다.</li>
	 *     <li>{@code GROUP BY}를 사용하여 각 공연의 시작일과 종료일을 계산합니다.</li>
	 *     <li>{@code EXISTS} 서브쿼리를 사용하여 현재 시간이 티켓 판매 기간에 포함되는 공연만 필터링합니다.</li>
	 *     <li>{@code ORDER BY} 절을 통해 조회수로 내림차순 정렬하고,
	 *         동점일 경우 공연 시작일(가장 빠른 일정) 오름차순으로 2차 정렬합니다.</li>
	 * </ul>
	 *
	 * @param pageable 페이징 정보 (상위 10개를 가져오기 위해 {@code PageRequest.of(0, 10)}을 사용)
	 * @return {@link PerformanceSummaryResponse}의 페이지 객체
	 */
	@Override
	public Page<PerformanceSummaryResponse> findTop10Performances(final Pageable pageable) {
		final QSchedule subSchedule = new QSchedule("subSchedule");
		final LocalDateTime now = LocalDateTime.now();

		// 컨텐츠 쿼리: 조회수 기준 인기 공연을 DTO로 프로젝션
		final List<PerformanceSummaryResponse> content = queryFactory
			.select(performanceSummaryProjection())
			.from(schedule)
			.join(schedule.performance, performance)
			.join(schedule.stadium, stadium)
			// PerformanceStatistic과 연관관계가 없으므로 ON 절로 직접 조인
			.join(performanceStatistic).on(performanceStatistic.performance.eq(performance))
			.where(ticketOnSale(subSchedule, now))
			.groupBy(
				performance.id,
				performance.name,
				performance.imageUrl,
				performance.type,
				performance.genre,
				stadium.name,
				performance.adultOnly,
				performanceStatistic.hit
			)
			// 1차: 조회수 내림차순, 2차: 공연 시작일 오름차순
			.orderBy(
				performanceStatistic.hit.desc(),
				schedule.performanceDatetime.min().asc()
			)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		// count 쿼리: content의 GROUP BY(공연+공연장) 기준과 일치하도록 그룹 수를 집계
		// QueryDSL JPA는 FROM 절 서브쿼리를 지원하지 않으므로 그룹 결과를 조회 후 size()로 총 수를 계산
		return PageableExecutionUtils.getPage(content, pageable,
			() -> queryFactory
				.select(performance.id)
				.from(schedule)
				.join(schedule.performance, performance)
				.join(schedule.stadium, stadium)
				.join(performanceStatistic).on(performanceStatistic.performance.eq(performance))
				.where(ticketOnSale(subSchedule, now))
				.groupBy(performance.id, stadium.name)
				.fetch()
				.size()
		);
	}

	/**
	 * 티켓 판매 기간에 해당하는 일정이 하나라도 존재하는 공연만 필터링하는 EXISTS 조건을 생성합니다.
	 *
	 * @param subSchedule 서브쿼리용 QSchedule 별칭
	 * @param now         기준 시간
	 */
	private BooleanExpression ticketOnSale(final QSchedule subSchedule, final LocalDateTime now) {
		return JPAExpressions.selectOne()
			.from(subSchedule)
			.where(
				subSchedule.performance.id.eq(performance.id),
				subSchedule.ticketOpenTime.loe(now),
				subSchedule.ticketCloseTime.goe(now)
			)
			.exists();
	}

	/**
	 * 공연 목록 조회에 공통으로 사용되는 {@link PerformanceSummaryResponse} 프로젝션을 생성합니다.
	 *
	 * <p>{@code CAST(performanceDatetime AS date)}를 사용하여 시간 부분을 제거한 뒤
	 * {@code MIN/MAX}로 공연 시작일·종료일을 집계합니다.</p>
	 */
	private ConstructorExpression<PerformanceSummaryResponse> performanceSummaryProjection() {
		final DateTemplate<java.sql.Date> performanceDate =
			Expressions.dateTemplate(java.sql.Date.class, "CAST({0} AS date)", schedule.performanceDatetime);

		return Projections.constructor(PerformanceSummaryResponse.class,
			performance.id,
			performance.name,
			performance.imageUrl,
			performance.type,
			performance.genre,
			stadium.name,
			performanceDate.min(),
			performanceDate.max(),
			performance.adultOnly
		);
	}
}