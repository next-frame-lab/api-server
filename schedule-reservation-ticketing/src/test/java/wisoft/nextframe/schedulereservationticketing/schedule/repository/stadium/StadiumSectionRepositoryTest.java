package wisoft.nextframe.schedulereservationticketing.schedule.repository.stadium;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import jakarta.transaction.Transactional;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.Stadium;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.StadiumSection;
import wisoft.nextframe.schedulereservationticketing.repository.stadium.StadiumRepository;
import wisoft.nextframe.schedulereservationticketing.repository.stadium.StadiumSectionRepository;

@SpringBootTest
@Transactional
class StadiumSectionRepositoryTest {

	@Autowired
	private StadiumSectionRepository stadiumSectionRepository;
	@Autowired
	private StadiumRepository stadiumRepository;

	private Stadium savedStadium;


	@BeforeEach
	void setUp() {
		final Stadium stadium = Stadium.builder().id(UUID.randomUUID()).name("잠실 야구장").address("서울 송파구 올림픽로 25").build();
		savedStadium = stadiumRepository.save(stadium);
	}

	@Test
	@DisplayName("새로운 경기장 구역을 저장하고 ID로 조회하면 성공한다.")
	void saveAndFindById_test() {
		// given
		final UUID sectionId = UUID.randomUUID();
		final StadiumSection newSection = StadiumSection.builder()
			.id(sectionId)
			.stadium(savedStadium)
			.section("A")
			.build();

		// when
		stadiumSectionRepository.save(newSection);
		final Optional<StadiumSection> foundSectionOptional = stadiumSectionRepository.findById(sectionId);

		// then
		assertThat(foundSectionOptional).isPresent();

		StadiumSection foundStadium = foundSectionOptional.get();
		assertThat(foundStadium.getId()).isEqualTo(sectionId);
		assertThat(foundStadium.getSection()).isEqualTo("A");
		assertThat(foundStadium.getStadium().getId()).isEqualTo(savedStadium.getId());
	}

	@Test
	@DisplayName("동일한 경기장에 중복된 구역 이름을 저장하면 예외가 발생한다.")
	void uniqueConstraint_violation_test() {
		// given
		StadiumSection section1 = StadiumSection.builder()
			.id(UUID.randomUUID())
			.stadium(savedStadium)
			.section("A")
			.build();
		stadiumSectionRepository.save(section1);

		StadiumSection section2_duplicate = StadiumSection.builder()
			.id(UUID.randomUUID())
			.stadium(savedStadium)
			.section("A") // 중복된 구역 이름
			.build();

		// when and then
		assertThatThrownBy(() -> stadiumSectionRepository.saveAndFlush(section2_duplicate))
			.isInstanceOf(DataIntegrityViolationException.class);
	}
}