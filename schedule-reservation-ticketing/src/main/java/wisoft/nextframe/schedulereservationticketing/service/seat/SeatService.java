package wisoft.nextframe.schedulereservationticketing.service.seat;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.dto.seat.LockedSeatListResponse;
import wisoft.nextframe.schedulereservationticketing.dto.seat.LockedSeatResponse;
import wisoft.nextframe.schedulereservationticketing.entity.seat.SeatState;
import wisoft.nextframe.schedulereservationticketing.repository.seat.SeatStateRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeatService {
	
	private final SeatStateRepository seatStateRepository;

	public LockedSeatListResponse getLockedSeats(UUID scheduleId) {
		
		// 1. scheduleId에 해당하는 스케줄의 잠긴 좌석 엔티티 목록을 조회합니다.
		final List<SeatState> lockedSeats = seatStateRepository.findByScheduleIdAndIsLockedTrue(scheduleId);
		
		// 2. LockedSeatResponse DTO 목록으로 변환합니다.
		final List<LockedSeatResponse> lockedSeatResponsesList = lockedSeats.stream()
			.map(seat -> LockedSeatResponse.builder()
				.id(seat.getSeat().getId())
				.isLocked(seat.getIsLocked())
				.build()
			).toList();

		// 3. 최종 DTO인 LockedSeatListResponse로 감싸서 반환합니다.
		return LockedSeatListResponse.builder()
			.seats(lockedSeatResponsesList)
			.build();
	}
}
