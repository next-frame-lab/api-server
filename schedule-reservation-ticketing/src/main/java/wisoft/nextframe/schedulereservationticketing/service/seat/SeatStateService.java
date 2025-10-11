package wisoft.nextframe.schedulereservationticketing.service.seat;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.schedulereservationticketing.dto.seat.seatstate.SeatStateListResponse;
import wisoft.nextframe.schedulereservationticketing.entity.seat.SeatState;
import wisoft.nextframe.schedulereservationticketing.repository.seat.SeatStateRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeatStateService {
	
	private final SeatStateRepository seatStateRepository;

	public SeatStateListResponse getSeatStates(UUID scheduleId) {
		log.debug("DB에서 잠긴 좌석 조회 시작. scheduleId: {}", scheduleId);
		// 1. 공연 일정(scheduleId)에 해당하는 잠긴(예약된) 좌석 엔티티 목록을 조회합니다.
		final List<SeatState> seatStates = seatStateRepository.findByScheduleIdAndIsLockedTrue(scheduleId);
		log.debug("DB에서 잠긴 좌석 조회 완료. count: {}", seatStates.size());

		// 2. 데이터를 응답 DTO로 변환합니다.
		return SeatStateListResponse.from(seatStates);
	}
}
