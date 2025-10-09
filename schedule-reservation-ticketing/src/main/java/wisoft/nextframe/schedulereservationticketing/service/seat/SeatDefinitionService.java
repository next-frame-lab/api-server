package wisoft.nextframe.schedulereservationticketing.service.seat;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.schedulereservationticketing.dto.seat.seatdefinition.SeatDefinitionListResponse;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;
import wisoft.nextframe.schedulereservationticketing.repository.stadium.SeatDefinitionRepository;
import wisoft.nextframe.schedulereservationticketing.repository.stadium.StadiumRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatDefinitionService {

	private final StadiumRepository stadiumRepository;
	private final SeatDefinitionRepository seatDefinitionRepository;

	/**
	 * 특정 공연장의 모든 좌석 정보를 조회합니다.
	 *
	 * @param stadiumId 공연장 ID
	 * @return 좌석 정보 DTO 리스트
	 * @throws EntityNotFoundException 해당 ID의 공연장이 존재하지 않을 경우 발생
	 */
	@Transactional(readOnly = true)
	public SeatDefinitionListResponse getSeatDefinitions(UUID stadiumId) {
		log.debug("공연장 좌석 정보 서비스 시작. stadiumId: {}", stadiumId);

		// 1. 공연장 존재 여부 확인
		stadiumRepository.findById(stadiumId)
			.orElseThrow(() -> {
				log.warn("존재하지 않는 공연장 조회 시도. stadiumId: {}", stadiumId);
				return new EntityNotFoundException("해당 공연장을 찾을 수 없습니다.");
			});
		log.debug("공연장 존재 확인 완료. stadiumId: {}", stadiumId);

		// 2. 공연장의 좌석 정보 조회
		final List<SeatDefinition> seatDefinitions = seatDefinitionRepository.findAllByStadiumIdWithSorting(stadiumId);
		log.debug("DB에서 좌석 정보 조회 완료. count: {}", seatDefinitions.size());

		// 3. 조회된 엔티티 목록을 응답 DTO로 변환하여 반환
		return SeatDefinitionListResponse.from(seatDefinitions);
	}
}
