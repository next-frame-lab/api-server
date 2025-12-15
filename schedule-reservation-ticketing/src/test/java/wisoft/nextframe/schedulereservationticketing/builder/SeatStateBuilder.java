package wisoft.nextframe.schedulereservationticketing.builder;

import java.util.UUID;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.seat.SeatState;
import wisoft.nextframe.schedulereservationticketing.entity.seat.SeatStateId;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SeatStateBuilder {

	private UUID scheduleId;
	private UUID seatId;
	private Schedule schedule;
	private SeatDefinition seat;
 private Boolean isLocked = false;

	public static SeatStateBuilder builder() {
		return new SeatStateBuilder();
	}

 public SeatState build() {
        // 복합 키 생성
        SeatStateId id = SeatStateId.builder()
            .scheduleId(scheduleId)
            .seatId(seatId)
            .build();

        return new SeatState(id, schedule, seat, isLocked);
    }

	public SeatStateBuilder withScheduleId(UUID scheduleId) {
		this.scheduleId = scheduleId;
		return this;
	}

	public SeatStateBuilder withSeatId(UUID seatId) {
		this.seatId = seatId;
		return this;
	}

	public SeatStateBuilder withSchedule(Schedule schedule) {
		this.schedule = schedule;
		return this;
	}

	public SeatStateBuilder withSeat(SeatDefinition seat) {
		this.seat = seat;
		return this;
	}

	public SeatStateBuilder withIsLocked(Boolean isLocked) {
		this.isLocked = isLocked;
		return this;
	}
}
