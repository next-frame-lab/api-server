package wisoft.nextframe.schedulereservationticketing.repository.performance;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import wisoft.nextframe.schedulereservationticketing.dto.performancedetail.response.SeatSectionPriceResponse;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformancePricing;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformancePricingId;

public interface PerformancePricingRepository extends JpaRepository<PerformancePricing, PerformancePricingId> {

	/**
	 * 특정 공연과 공연장의 조합에 대한 공통 좌석 가격 정보를 조회합니다.
	 * DISTINCT를 사용하여 여러 스케줄에 동일한 가격이 있어도 중복을 제거합니다.
	 *
	 * @param performanceId 조회할 공연의 ID
	 * @param stadiumId 조회할 공연장의 ID
	 * @return 좌석 등급과 가격 정보가 담긴 DTO 리스트
	 */
	@Query("""
		    SELECT DISTINCT new wisoft.nextframe.schedulereservationticketing.dto.performancedetail.response.SeatSectionPriceResponse(
		        ss.section,
		        pp.price
		    )
		    FROM PerformancePricing pp
		    JOIN pp.schedule s
		    JOIN pp.stadiumSection ss
		    WHERE s.performance.id = :performanceId AND s.stadium.id = :stadiumId
		    ORDER BY pp.price DESC
		""")
	List<SeatSectionPriceResponse> findCommonPricingByPerformanceAndStadium(
		@Param("performanceId") UUID performanceId,
		@Param("stadiumId") UUID stadiumId
	);

	@Query("SELECT pp FROM PerformancePricing pp " +
		"WHERE pp.schedule.id = :scheduleId AND pp.stadiumSection.id IN :sectionIds")
	List<PerformancePricing> findByScheduleIdAndSectionIds(
		@Param("scheduleId") UUID scheduleId,
		@Param("sectionIds") List<UUID> sectionIds
	);
}
