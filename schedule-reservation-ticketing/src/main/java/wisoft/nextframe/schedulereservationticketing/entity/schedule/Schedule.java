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
import wisoft.nextframe.schedulereservationticketing.common.exception.DomainException;
import wisoft.nextframe.schedulereservationticketing.common.exception.ErrorCode;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.seat.SeatState;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.Stadium;

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

	public void lockSeatsForReservation(List<SeatState> seatStates, int seatSize) {
		// 1. 좌석이 존재하는지 확인
		if (seatStates.size() != seatSize) {
			throw new DomainException(ErrorCode.SEAT_NOT_DEFINED);
		}

		// 2. 좌석 잠금 처리(SeatState 엔티티)
		for (final SeatState seatState : seatStates) {
			seatState.lock();
		}
	}
}