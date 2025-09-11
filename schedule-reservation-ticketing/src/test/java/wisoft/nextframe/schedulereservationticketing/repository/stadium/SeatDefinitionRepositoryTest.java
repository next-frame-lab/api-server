package wisoft.nextframe.schedulereservationticketing.repository.stadium;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.transaction.Transactional;
import wisoft.nextframe.schedulereservationticketing.builder.StadiumBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.StadiumSectionBuilder;
import wisoft.nextframe.schedulereservationticketing.config.AbstractIntegrationTest;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.Stadium;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.StadiumSection;

class SeatDefinitionRepositoryTest extends AbstractIntegrationTest {

	@Autowired
	private SeatDefinitionRepository seatDefinitionRepository;
	@Autowired
	private StadiumRepository stadiumRepository;
	@Autowired
	private StadiumSectionRepository stadiumSectionRepository;

	private StadiumSection savedSection;

	@BeforeEach
	void setUp() {
		Stadium savedStadium = stadiumRepository.save(new StadiumBuilder().build());

		savedSection = stadiumSectionRepository.save(new StadiumSectionBuilder().withStadium(savedStadium).build());
	}

	@Test
	@DisplayName("성공: 새로운 좌석 정의를 저장하고 ID로 조회하면 성공한다")
	void saveAndFindById_Success() {
		// given
		UUID seatId = UUID.randomUUID();
		SeatDefinition newSeat = SeatDefinition.builder()
			.id(seatId)
			.stadiumSection(savedSection)
			.rowNo(10)
			.columnNo(15)
			.build();

		// when
		seatDefinitionRepository.save(newSeat);
		Optional<SeatDefinition> foundSeatOptional = seatDefinitionRepository.findById(seatId);

		// then
		assertThat(foundSeatOptional).isPresent();

		SeatDefinition foundSeat = foundSeatOptional.get();
		assertThat(foundSeat.getId()).isEqualTo(seatId);
		assertThat(foundSeat.getRowNo()).isEqualTo(10);
		assertThat(foundSeat.getColumnNo()).isEqualTo(15);
		assertThat(foundSeat.getStadiumSection().getId()).isEqualTo(savedSection.getId());
	}
}