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
import wisoft.nextframe.schedulereservationticketing.common.exception.ErrorCode;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.seat.SeatState;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.Stadium;
import wisoft.nextframe.schedulereservationticketing.exception.DomainException;
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

		// 2. 좌석을 조회함과 동시에 잠급니다.
		final List<SeatState> seatStates = seatStateRepository.findAndLockByScheduleIdAndSeatIds(this.id, seatIds);

		// 3. 요청한 모든 좌석이 존재하는지 확인합니다.
		if (seatStates.size() != seatIds.size()) {
			throw new DomainException(ErrorCode.SEAT_NOT_DEFINED);
		}

		// 4. 이미 잠겨 있는 좌석이 있는지 확인합니다.
		for (final SeatState seatState : seatStates) {
			if (seatState.getIsLocked()) {
				throw new DomainException(ErrorCode.SEAT_ALREADY_LOCKED);
			}
			// 5. 좌석 상태를 잠금으로 변경합니다.
			seatState.lock();
		}
	}
}