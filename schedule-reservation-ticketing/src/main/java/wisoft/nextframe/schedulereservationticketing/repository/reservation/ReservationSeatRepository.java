package wisoft.nextframe.schedulereservationticketing.repository.reservation;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import wisoft.nextframe.schedulereservationticketing.entity.reservation.ReservationSeat;

public interface ReservationSeatRepository extends JpaRepository<ReservationSeat, UUID> {
}
