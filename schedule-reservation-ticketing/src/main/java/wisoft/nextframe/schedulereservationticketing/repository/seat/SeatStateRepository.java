package wisoft.nextframe.schedulereservationticketing.repository.seat;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import wisoft.nextframe.schedulereservationticketing.entity.seat.SeatState;
import wisoft.nextframe.schedulereservationticketing.entity.seat.SeatStateId;

public interface SeatStateRepository extends JpaRepository<SeatState, SeatStateId> {

	/**
	 * [수정됨] 주어진 스케줄과 좌석 ID 목록에 해당하는 SeatState 엔티티를 비관적 쓰기 락을 걸어 조회합니다.
	 * 이 메서드는 트랜잭션 내에서 호출되어야 합니다.
	 * @param scheduleId 스케줄 ID
	 * @param seatIds 좌석 ID 목록
	 * @return 잠금이 적용된 SeatState 엔티티 목록
	 */
	@Lock(LockModeType.PESSIMISTIC_FORCE_INCREMENT)
	@Query("SELECT ss FROM SeatState ss WHERE ss.id.scheduleId = :scheduleId AND ss.id.seatId IN :seatIds")
	List<SeatState> findAndLockByScheduleIdAndSeatIds(
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