package wisoft.nextframe.schedulereservationticketing.schedule.repository.seat;

import org.springframework.data.jpa.repository.JpaRepository;

import wisoft.nextframe.schedulereservationticketing.schedule.entity.seat.SeatState;
import wisoft.nextframe.schedulereservationticketing.schedule.entity.seat.SeatStateId;

public interface SeatStateRepository extends JpaRepository<SeatState, SeatStateId> {
}