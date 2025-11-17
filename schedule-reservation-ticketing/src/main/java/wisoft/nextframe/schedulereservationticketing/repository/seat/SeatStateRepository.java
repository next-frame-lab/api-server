package wisoft.nextframe.schedulereservationticketing.repository.seat;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import wisoft.nextframe.schedulereservationticketing.entity.seat.SeatState;
import wisoft.nextframe.schedulereservationticketing.entity.seat.SeatStateId;

public interface SeatStateRepository extends JpaRepository<SeatState, SeatStateId> {

	/**
	 * @param scheduleId 스케줄 ID
	 * @param seatIds 좌석 ID 목록
	 * @return 조회된 SeatState 엔티티 목록
	 */
	@Query("SELECT ss FROM SeatState ss WHERE ss.id.scheduleId = :scheduleId AND ss.id.seatId IN :seatIds")
	List<SeatState> findByScheduleIdAndSeatIds(
		@Param("scheduleId") UUID scheduleId,
		@Param("seatIds") List<UUID> seatIds
	);

	/**
	 * 특정 스케줄 ID에 대해 잠겨 있는(isLocked=true) 모든 좌석 상태를 조회합니다.
	 * @param scheduleId 스케줄의 UUID
	 * @return 잠겨 있는 SeatState 엔티티 목록
	 */
	List<SeatState> findByScheduleIdAndIsLockedTrue(UUID scheduleId);
}