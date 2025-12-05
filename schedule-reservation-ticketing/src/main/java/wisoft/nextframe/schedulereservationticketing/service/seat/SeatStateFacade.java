package wisoft.nextframe.schedulereservationticketing.service.seat;

import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.common.exception.DomainException;
import wisoft.nextframe.schedulereservationticketing.common.exception.ErrorCode;
import wisoft.nextframe.schedulereservationticketing.dto.seat.seatstate.SeatStateListResponse;
import wisoft.nextframe.schedulereservationticketing.repository.schedule.ScheduleRepository;

@Component
@RequiredArgsConstructor
public class SeatStateFacade {

	private final ScheduleRepository scheduleRepository;
	private final SeatStateService seatStateService;

	public SeatStateListResponse getSeatStates(UUID scheduleId) {
		scheduleRepository.findById(scheduleId)
			.orElseThrow(() -> new DomainException(ErrorCode.SCHEDULE_NOT_FOUND));

		return seatStateService.getSeatStates(scheduleId);
	}
}
