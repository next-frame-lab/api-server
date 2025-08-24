package wisoft.nextframe.schedulereservationticketing.repository.performance;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformancePricing;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformancePricingId;

public interface PerformancePricingRepository extends JpaRepository<PerformancePricing, PerformancePricingId> {

	/*
	todo: 데이터 모델 수정 필요
	- 현재 이 메서드를 호출하면, 특정 공연장이나 일정(Schedule)에 관계없이 해당 공연(Performance)에 연결된 모든 가격 정보가 한꺼번에 조회된다.
	  -> 이 문제를 해결하려면, 가격 정보가 Performance가 아닌 Schedule에 직접 연결되도록 데이터 모델을 변경해야 함.
	 */
	List<PerformancePricing> findByPerformanceId(UUID performanceId);

	/**
	 * 특정 공연과 여러 섹션 ID에 해당하는 가격 정보를 모두 조회합니다.
	 * @param performanceId 공연 ID
	 * @param sectionIds 스타디움 섹션 ID 목록
	 * @return 가격 정보 목록
	 */
	List<PerformancePricing> findById_PerformanceIdAndId_StadiumSectionIdIn(UUID performanceId, List<UUID> sectionIds);
}
