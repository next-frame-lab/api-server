package wisoft.nextframe.schedulereservationticketing.service.stadium;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.dto.seatdefinition.SeatDefinitionListResponse;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;
import wisoft.nextframe.schedulereservationticketing.repository.stadium.SeatDefinitionRepository;
import wisoft.nextframe.schedulereservationticketing.repository.stadium.StadiumRepository;

@Service
@RequiredArgsConstructor
public class StadiumService {

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
		// 1. 공연장 존재 여부 확인
		stadiumRepository.findById(stadiumId).orElseThrow(EntityNotFoundException::new);

		// 2. 공연장의 좌석 데이터 조회
		final List<SeatDefinition> seatDefinitions = seatDefinitionRepository.findAllByStadiumIdWithSorting(stadiumId);

		// 3. 조회된 엔티티 목록을 응답 DTO로 변환하여 반환
		return SeatDefinitionListResponse.from(seatDefinitions);
	}
}
