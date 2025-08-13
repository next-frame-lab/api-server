package wisoft.nextframe.schedulereservationticketing.repository.seat;

import org.springframework.data.jpa.repository.JpaRepository;

import wisoft.nextframe.schedulereservationticketing.entity.seat.SeatState;
import wisoft.nextframe.schedulereservationticketing.entity.seat.SeatStateId;

public interface SeatStateRepository extends JpaRepository<SeatState, SeatStateId> {
}