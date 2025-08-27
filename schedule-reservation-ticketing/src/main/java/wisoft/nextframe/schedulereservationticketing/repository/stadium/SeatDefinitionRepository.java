package wisoft.nextframe.schedulereservationticketing.repository.stadium;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;

public interface SeatDefinitionRepository extends JpaRepository<SeatDefinition, UUID> {

	@Query("SELECT sd FROM SeatDefinition sd " +
		"JOIN FETCH sd.stadiumSection ss " +
		"WHERE ss.stadium.id = :stadiumId " +
		"ORDER BY ss.section ASC, sd.rowNo ASC, sd.columnNo ASC")
	List<SeatDefinition> findAllByStadiumIdWithSorting(@Param("stadiumId") UUID stadiumId);

	/**
	 * 여러 좌석 ID에 해당하는 좌석 정보(section, row, column)를 모두 조회합니다. (N+1 문제 방지를 위해 fetch join 사용)
	 * @param seatIds 좌석 ID 목록
	 * @return 좌석 정보 목록
	 */
	@Query("SELECT sd FROM SeatDefinition sd JOIN FETCH sd.stadiumSection WHERE sd.id IN :seatIds")
	List<SeatDefinition> findWithStadiumSectionByIdIn(@Param("seatIds") List<UUID> seatIds);

}