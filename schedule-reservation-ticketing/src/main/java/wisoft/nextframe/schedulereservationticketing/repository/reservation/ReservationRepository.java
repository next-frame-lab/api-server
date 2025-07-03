package wisoft.nextframe.schedulereservationticketing.repository.reservation;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import wisoft.nextframe.schedulereservationticketing.entity.reservation.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
}
