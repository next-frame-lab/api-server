package wisoft.nextframe.schedulereservationticketing.entity.schedule;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.Stadium;
import wisoft.nextframe.schedulereservationticketing.exception.reservation.SeatAlreadyLockedException;
import wisoft.nextframe.schedulereservationticketing.repository.seat.SeatStateRepository;

@Getter
@Builder
@AllArgsConstructor
@Entity
@Table(
	name = "schedules",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uq_schedules_performance_stadium_datetime",
			columnNames = {"performance_id", "stadium_id", "performance_datetime"}
		)
	}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Schedule {

	@Id
	@Column(name = "id", nullable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "performance_id", nullable = false)
	private Performance performance;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stadium_id", nullable = false)
	private Stadium stadium;

	@Column(name = "performance_datetime", nullable = false)
	private LocalDateTime performanceDatetime;

	@Column(name = "ticket_open_time")
	private LocalDateTime ticketOpenTime;

	@Column(name = "ticket_close_time")
	private LocalDateTime ticketCloseTime;

	public void lockSeatsForReservation(List<SeatDefinition> seats, SeatStateRepository seatStateRepository) {
		// 1. 좌석 ID 목록을 추출합니다.
		final List<UUID> seatIds = seats.stream()
			.map(SeatDefinition::getId)
			.toList();

		// 2. 좌석이 이미 잠겨있는지 검증합니다.
		if (seatStateRepository.existsByScheduleIdSeatIsLocked(this.id, seatIds)) {
			throw new SeatAlreadyLockedException("이미 예약되었거나 선택할 수 없는 좌석입니다.");
		}

		// 3. 좌석을 잠급니다.
		seatStateRepository.lockSeats(this.id, seatIds);
	}
}