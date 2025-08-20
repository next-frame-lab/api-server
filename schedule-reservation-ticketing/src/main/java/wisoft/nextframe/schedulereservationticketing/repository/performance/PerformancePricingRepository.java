package wisoft.nextframe.schedulereservationticketing.repository.performance;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformancePricing;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformancePricingId;

public interface PerformancePricingRepository extends JpaRepository<PerformancePricing, PerformancePricingId> {

	List<PerformancePricing> findByPerformanceId(UUID performanceId);

	/**
	 * 특정 공연과 여러 섹션 ID에 해당하는 가격 정보를 모두 조회합니다.
	 * @param performanceId 공연 ID
	 * @param sectionIds 스타디움 섹션 ID 목록
	 * @return 가격 정보 목록
	 */
	List<PerformancePricing> findById_PerformanceIdAndId_StadiumSectionIdIn(UUID performanceId, List<UUID> sectionIds);
}
