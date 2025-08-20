package wisoft.nextframe.schedulereservationticketing.entity.reservation;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.web.bind.annotation.BindParam;

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
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
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

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, columnDefinition = "reservation_status")
	private ReservationStatus status = ReservationStatus.CREATED;

	@CreationTimestamp
	@Column(name = "reserved_at", nullable = false, updatable = false)
	private LocalDateTime reservedAt;
}
