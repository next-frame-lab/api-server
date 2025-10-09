package wisoft.nextframe.schedulereservationticketing.repository.reservation;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.reservation.Reservation;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

	/**
	 * 사용자가 특정 공연을 예매했는지 확인합니다.
	 * (예매 상태가 'CONFIRMED'와 같이 확정된 경우만 조회하도록 조건을 추가할 수 있습니다.)
	 * @param user 사용자 엔티티
	 * @param performance 공연 엔티티
	 * @return 예매 내역 존재 여부
	 */
	@Query("SELECT COUNT(r) > 0 " +
		"FROM Reservation r " +
		"WHERE r.user = :user AND r.schedule.performance = :performance")
	boolean existsByUserAndPerformance(@Param("user") User user, @Param("performance") Performance performance);
}
