package wisoft.nextframe.schedulereservationticketing.entity.reservation;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "reservation_seats")
public class ReservationSeat {

	@EmbeddedId
	private ReservationSeatId id;

	@Setter
	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("reservationId")
	@JoinColumn(name = "reservation_id")
	private Reservation reservation;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("seatId")
	@JoinColumn(name = "seat_id")
	private SeatDefinition seatDefinition;

	public ReservationSeat(Reservation reservation, SeatDefinition seatDefinition) {
		this.reservation = reservation;
		this.seatDefinition = seatDefinition;
		this.id = new ReservationSeatId(reservation.getId(), seatDefinition.getId());
	}
}
