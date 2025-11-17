package wisoft.nextframe.schedulereservationticketing.service.seat;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import wisoft.nextframe.schedulereservationticketing.builder.SeatDefinitionBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.StadiumBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.StadiumSectionBuilder;
import wisoft.nextframe.schedulereservationticketing.common.exception.DomainException;
import wisoft.nextframe.schedulereservationticketing.common.exception.ErrorCode;
import wisoft.nextframe.schedulereservationticketing.dto.seat.seatdefinition.SeatDefinitionListResponse;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.Stadium;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.StadiumSection;
import wisoft.nextframe.schedulereservationticketing.repository.stadium.SeatDefinitionRepository;
import wisoft.nextframe.schedulereservationticketing.repository.stadium.StadiumRepository;

@ExtendWith(MockitoExtension.class)
class SeatDefinitionServiceTest {

	@InjectMocks
	private SeatDefinitionService seatDefinitionService;

	@Mock
	private StadiumRepository stadiumRepository;
	@Mock
	private SeatDefinitionRepository seatDefinitionRepository;

	@Nested
	class getSeatDefinitionsTest {

		@Test
		@DisplayName("공연장에 존재하는 좌석 정보를 정확히 조회한다")
		void getSeatDefinitions_Success() {
			// given
			final Stadium stadium = StadiumBuilder.builder().build();

			given(stadiumRepository.findById(stadium.getId()))
				.willReturn(Optional.of(stadium));

			final StadiumSection section = StadiumSectionBuilder.builder().withStadium(stadium).build();

			final SeatDefinition seat1 = SeatDefinitionBuilder.builder().withStadiumSection(section).build();
			final SeatDefinition seat2 = SeatDefinitionBuilder.builder().withStadiumSection(section).build();
			given(seatDefinitionRepository.findAllByStadiumIdWithSorting(stadium.getId()))
				.willReturn(List.of(seat1, seat2));

			// when
			final SeatDefinitionListResponse result = seatDefinitionService.getSeatDefinitions(stadium.getId());

			// then
			assertThat(result).isNotNull();
			assertThat(result.seats()).hasSize(2);

			verify(seatDefinitionRepository).findAllByStadiumIdWithSorting(stadium.getId());
		}

		@Test
		@DisplayName("존재하지 않은 공연장 ID로 조회 시 예외가 발생한다")
		void getSeatDefinitions_fail_stadiumNotFound() {
			// given
			final UUID stadiumId = UUID.randomUUID();

			// Mocking: 공연장을 찾을 수 없음
			given(stadiumRepository.findById(stadiumId))
				.willReturn(Optional.empty());

			// when and then
			assertThatThrownBy(() -> seatDefinitionService.getSeatDefinitions(stadiumId))
				.isInstanceOf(DomainException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.STADIUM_NOT_FOUND);
		}
	}
}