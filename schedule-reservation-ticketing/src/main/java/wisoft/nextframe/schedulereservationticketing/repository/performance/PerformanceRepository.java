package wisoft.nextframe.schedulereservationticketing.repository.performance;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import wisoft.nextframe.schedulereservationticketing.dto.performance.performancelist.response.PerformanceSummaryResponse;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;

public interface PerformanceRepository extends JpaRepository<Performance, UUID> {

	/**
	 * 현재 예매 가능한 공연 목록을 DTO로 직접 조회합니다.
	 *
	 * 이 메소드는 다음 조건을 만족하는 공연을 찾습니다.
	 * - {@code EXISTS} 서브쿼리를 사용하여 현재 시간이 티켓 판매 기간에 포함되는 공연만 필터링합니다.
	 * - {@code GROUP BY}를 사용하여 각 공연(공연+공연장 기준)의 시작일과 종료일을
	 * 연결된 모든 일정의 가장 빠른 날짜(MIN)와 가장 늦은 날짜(MAX)로 계산합니다.
	 *
	 * @param pageable 페이징 정보(페이지 번호, 페이지 크기)
	 * @return PerformanceSummaryResponse의 페이지 객체
	 */
	@Query(value = """
		    SELECT new wisoft.nextframe.schedulereservationticketing.dto.performance.performancelist.response.PerformanceSummaryResponse(
		        p.id,
		        p.name,
		        p.imageUrl,
		        p.type,
		        p.genre,
		        st.name,
		        MIN(CAST(sc.performanceDatetime AS date)),
		        MAX(CAST(sc.performanceDatetime AS date)),
		        p.adultOnly
		    )
		    FROM Schedule sc
				JOIN sc.performance p
				JOIN sc.stadium st
				WHERE EXISTS (
						SELECT 1
						FROM Schedule s_sub
						WHERE s_sub.performance.id = p.id
						AND s_sub.ticketOpenTime <= CURRENT_TIMESTAMP
						AND s_sub.ticketCloseTime >= CURRENT_TIMESTAMP
				)
				GROUP BY p.id, p.name, p.imageUrl, p.type, p.genre, st.name, p.adultOnly
		""")
	Page<PerformanceSummaryResponse> findReservablePerformances(Pageable pageable);
}