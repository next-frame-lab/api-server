package wisoft.nextframe.schedulereservationticketing.service.reservation;

import java.util.List;

import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;

/**
 * 예매 처리에 필요한 모든 엔티티를 담는 데이터 클래스
 */
public record ReservationContext(
	User user,
	Schedule schedule,
	Performance performance,
	List<SeatDefinition> seats
) {
}
