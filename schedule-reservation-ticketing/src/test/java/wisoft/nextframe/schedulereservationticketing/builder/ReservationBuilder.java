package wisoft.nextframe.schedulereservationticketing.builder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.entity.reservation.Reservation;
import wisoft.nextframe.schedulereservationticketing.entity.reservation.ReservationSeat;
import wisoft.nextframe.schedulereservationticketing.entity.reservation.ReservationStatus;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReservationBuilder {

	private UUID id = null;
	private User user;
	private Schedule schedule;
	private Integer totalPrice = 50000;
	private ReservationStatus status = ReservationStatus.CONFIRMED;
	private LocalDateTime reservedAt = LocalDateTime.now();
	private List<ReservationSeat> reservationSeats = new ArrayList<>();

	public static ReservationBuilder builder() {
		return new ReservationBuilder();
	}

	public ReservationBuilder withId(UUID id) {
		this.id = id;
		return this;
	}

	public ReservationBuilder withUser(User user) {
		this.user = user;
		return this;
	}

	public ReservationBuilder withSchedule(Schedule schedule) {
		this.schedule = schedule;
		return this;
	}

	public ReservationBuilder withTotalPrice(Integer totalPrice) {
		this.totalPrice = totalPrice;
		return this;
	}

	public ReservationBuilder withStatus(ReservationStatus status) {
		this.status = status;
		return this;
	}

	public Reservation build() {
		if (user == null || schedule == null) {
			throw new IllegalStateException("User and Schedule are required for ReservationBuilder.");
		}

		return new Reservation(id, user, schedule, totalPrice, status, reservedAt, reservationSeats);
	}
}
