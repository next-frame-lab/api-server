package wisoft.nextframe.schedulereservationticketing.repository.performance;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;

public interface PerformanceRepository extends JpaRepository<Performance, UUID>, PerformanceRepositoryCustom {

	/**
	 * 공연 ID를 기반으로 성인 관람 여부(adultOnly)만 조회하는 메소드.
	 * @param id 공연 ID
	 * @return 성인 관람 여부 (true/false)
	 */
	@Query("SELECT p.adultOnly FROM Performance p WHERE p.id = :id")
	Optional<Boolean> findAdultOnlyById(@Param("id") UUID id);
}