package wisoft.nextframe.schedulereservationticketing.repository.stadium;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;

public interface SeatDefinitionRepository extends JpaRepository<SeatDefinition, UUID> {

	@Query(value = """
				select sd from SeatDefinition sd
				join fetch sd.stadiumSection ss
				order by ss.section asc, sd.rowNo asc, sd.columnNo asc 
		""")
	List<SeatDefinition> findAllByStadiumIdWithSorting(@Param("stadiumId") UUID stadiumId);

	/**
	 * 여러 좌석 ID에 해당하는 좌석 정보를 모두 조회합니다. (N+1 문제 방지를 위해 fetch join 사용)
	 * @param seatIds 좌석 ID 목록
	 * @return 좌석 정보 목록
	 */
	@Query("select sd from SeatDefinition sd join fetch sd.stadiumSection where sd.id in :seatIds")
	List<SeatDefinition> findWithStadiumSectionByIdIn(@Param("seatIds") List<UUID> seatIds);
}