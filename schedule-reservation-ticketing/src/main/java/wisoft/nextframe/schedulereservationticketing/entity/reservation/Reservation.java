package wisoft.nextframe.schedulereservationticketing.entity.reservation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "reservations")
public class Reservation {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "schedule_id", nullable = false)
	private Schedule schedule;

	@Column(name = "total_price", nullable = false)
	private Integer totalPrice;

	@Builder.Default
	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(name = "status", nullable = false, columnDefinition = "reservation_status")
	private ReservationStatus status = ReservationStatus.CREATED;

	@CreationTimestamp
	@Column(name = "reserved_at", nullable = false, updatable = false)
	private LocalDateTime reservedAt;

	@Builder.Default
	@OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ReservationSeat> reservationSeats = new ArrayList<>();

	public static Reservation create(User user, Schedule schedule, int totalPrice) {
		return Reservation.builder()
			.user(user)
			.schedule(schedule)
			.totalPrice(totalPrice)
			.build();
	}

	// 연관 관계 편의 메서드
	public void addReservationSeats(List<SeatDefinition> seatDefinitions) {
		for (SeatDefinition seatDefinition : seatDefinitions) {
			final ReservationSeat reservationSeat = new ReservationSeat(this, seatDefinition);
			this.reservationSeats.add(reservationSeat);
		}
	}
}
