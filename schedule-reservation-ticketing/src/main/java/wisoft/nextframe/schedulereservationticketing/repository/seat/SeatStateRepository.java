package wisoft.nextframe.schedulereservationticketing.repository.seat;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import wisoft.nextframe.schedulereservationticketing.entity.seat.SeatState;
import wisoft.nextframe.schedulereservationticketing.entity.seat.SeatStateId;

public interface SeatStateRepository extends JpaRepository<SeatState, SeatStateId> {

	/**
	 * 주어진 스케줄의 좌석 ID 목록에 대해 잠긴(isLocked = true) 좌석이 하나라도 있는지 확인합니다.
	 * @param scheduleId 스케줄 ID
	 * @param seatIds 좌석 ID 목록
	 * @return 잠긴 좌석이 있으면 true, 없으면 false
	 */
	@Query("""
			select exists (
				select 1
					from SeatState ss
					where ss.id.scheduleId = :scheduleId
					and ss.id.seatId in :seatIds
					and ss.isLocked = true
				)
	""")
	boolean existsByScheduleIdSeatIsLocked(@Param("scheduleId") UUID scheduleId, @Param("seatIds") List<UUID> seatIds);

	/**
	 * 주어진 스케줄과 좌석 ID 목록에 해당하는 좌석들을 잠금 상태(isLocked = true)로 변경합니다.
	 * @param scheduleId 스케줄 ID
	 * @param seatIds 좌석 ID 목록
	 * @return 변경된 좌석 수
	 */
	@Modifying
	@Query("""
				update SeatState ss
				set ss.isLocked = true
				where ss.id.scheduleId = :scheduleId
				and ss.id.seatId in :seatIds
		""")
	int lockSeats(@Param("scheduleId") UUID scheduleId, @Param("seatIds") List<UUID> seatIds);

	/**
	 * 특정 스케줄 ID에 대해 잠겨 있는(isLocked=true) 모든 좌석 상태를 조회합니다.
	 * @param scheduleId 스케줄의 UUID
	 * @return 잠겨 있는 SeatState 엔티티 목록
	 */
	List<SeatState> findByScheduleIdAndIsLockedTrue(UUID scheduleId);
}