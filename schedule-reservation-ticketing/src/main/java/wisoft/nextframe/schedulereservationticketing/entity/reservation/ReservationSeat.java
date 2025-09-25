package wisoft.nextframe.schedulereservationticketing.entity.reservation;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "reservation_seats",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uq_reservation_seats_schedule_seat",
			columnNames = {"schedule_id", "seat_id"}
		)
	}
)
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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "schedule_id", nullable = false)
	private Schedule schedule;

	public ReservationSeat(Reservation reservation, SeatDefinition seatDefinition) {
		this.reservation = reservation;
		this.seatDefinition = seatDefinition;
		this.schedule = reservation.getSchedule();
		this.id = new ReservationSeatId(reservation.getId(), seatDefinition.getId());
	}
}
