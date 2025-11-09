package wisoft.nextframe.schedulereservationticketing.entity.seat;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.common.exception.DomainException;
import wisoft.nextframe.schedulereservationticketing.common.exception.ErrorCode;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;

@Getter
@Builder
@AllArgsConstructor
@Entity
@Table(name = "seat_states")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SeatState {

	@EmbeddedId
	private SeatStateId id;

	@MapsId("scheduleId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "schedule_id", nullable = false)
	private Schedule schedule;

	@MapsId("seatId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "seat_id", nullable = false)
	private SeatDefinition seat;

	@Column(name = "is_locked", nullable = false)
	private Boolean isLocked;

	@Version
	@Column(name = "version")
	private Long version;

	public void lock() {
		if (this.isLocked) {
			throw new DomainException(ErrorCode.SEAT_ALREADY_LOCKED);
		}
		this.isLocked = true;
	}
}