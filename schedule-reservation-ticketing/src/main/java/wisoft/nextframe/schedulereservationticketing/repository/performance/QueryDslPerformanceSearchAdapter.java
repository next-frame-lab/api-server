package wisoft.nextframe.schedulereservationticketing.repository.performance;

import static wisoft.nextframe.schedulereservationticketing.entity.performance.QPerformance.*;
import static wisoft.nextframe.schedulereservationticketing.entity.performance.QPerformanceStatistic.*;
import static wisoft.nextframe.schedulereservationticketing.entity.schedule.QSchedule.*;
import static wisoft.nextframe.schedulereservationticketing.entity.stadium.QStadium.*;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Component;

import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTemplate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.dto.performance.performancelist.response.PerformanceSummaryResponse;
import wisoft.nextframe.schedulereservationticketing.dto.performance.search.request.PerformanceSearchCondition;
import wisoft.nextframe.schedulereservationticketing.dto.performance.search.request.PerformanceSearchSort;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.QSchedule;

@Component
@RequiredArgsConstructor
public class QueryDslPerformanceSearchAdapter implements PerformanceSearchPort {

	private final JPAQueryFactory queryFactory;

	@Override
	public Page<PerformanceSummaryResponse> search(final PerformanceSearchCondition condition) {
		final QSchedule subSchedule = new QSchedule("subSchedule");
		final LocalDateTime now = LocalDateTime.now();
		final PerformanceSearchSort sort = condition.sort();
		final boolean needStatisticJoin = needsStatisticJoin(sort);

		// 컨텐츠 쿼리
		JPAQuery<PerformanceSummaryResponse> contentQuery = queryFactory
			.select(performanceSummaryProjection())
			.from(schedule)
			.join(schedule.performance, performance)
			.join(schedule.stadium, stadium);

		if (needStatisticJoin) {
			contentQuery = contentQuery
				.join(performanceStatistic).on(performanceStatistic.performance.eq(performance));
		}

		contentQuery = contentQuery
			.where(
				ticketOnSale(subSchedule, now),
				nameContains(condition.keyword())
			)
			.groupBy(
				performance.id,
				performance.name,
				performance.imageUrl,
				performance.type,
				performance.genre,
				stadium.name,
				performance.adultOnly
			);

		if (sort == PerformanceSearchSort.HIT_DESC) {
			contentQuery = contentQuery.groupBy(performanceStatistic.hit);
		} else if (sort == PerformanceSearchSort.STAR_DESC) {
			contentQuery = contentQuery.groupBy(performanceStatistic.averageStar);
		}

		final List<PerformanceSummaryResponse> content = contentQuery
			.orderBy(resolveSort(sort))
			.offset(condition.pageable().getOffset())
			.limit(condition.pageable().getPageSize())
			.fetch();

		// count 쿼리
		return PageableExecutionUtils.getPage(content, condition.pageable(),
			() -> queryFactory
				.select(performance.id)
				.from(schedule)
				.join(schedule.performance, performance)
				.join(schedule.stadium, stadium)
				.where(
					ticketOnSale(subSchedule, now),
					nameContains(condition.keyword())
				)
				.groupBy(performance.id, stadium.name)
				.fetch()
				.size()
		);
	}

	private BooleanExpression nameContains(final String keyword) {
		if (keyword == null) {
			return null;
		}
		return performance.name.containsIgnoreCase(keyword);
	}

	private boolean needsStatisticJoin(final PerformanceSearchSort sort) {
		return sort == PerformanceSearchSort.HIT_DESC || sort == PerformanceSearchSort.STAR_DESC;
	}

	private OrderSpecifier<?>[] resolveSort(final PerformanceSearchSort sort) {
		return switch (sort) {
			case HIT_DESC -> new OrderSpecifier<?>[] {
				performanceStatistic.hit.desc(),
				schedule.performanceDatetime.min().asc()
			};
			case STAR_DESC -> new OrderSpecifier<?>[] {
				performanceStatistic.averageStar.desc(),
				schedule.performanceDatetime.min().asc()
			};
			case DATE_ASC -> new OrderSpecifier<?>[] {
				schedule.performanceDatetime.min().asc()
			};
			case DATE_DESC -> new OrderSpecifier<?>[] {
				schedule.performanceDatetime.min().desc()
			};
			case LATEST -> new OrderSpecifier<?>[] {
				schedule.performanceDatetime.min().desc()
			};
		};
	}

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
