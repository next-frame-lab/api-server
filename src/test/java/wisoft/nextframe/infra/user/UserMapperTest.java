package wisoft.nextframe.infra.user;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import wisoft.nextframe.domain.user.User;
import wisoft.nextframe.util.UserEntityFixture;

class UserMapperTest {

	private UserMapper mapper;

	@BeforeEach
	void setUp() {
		mapper = new UserMapper();
	}

	@Test
	@DisplayName("엔티티->도메인 매핑 성공")
	void should_map_entity_to_domain_correctly() {
		// given
		UserEntity entity = UserEntityFixture.sampleEntity();

		// when
		User user = mapper.toDomain(entity);

		// then
		User expected = UserEntityFixture.sampleDomain();
		assertThat(user.getId()).isEqualTo(expected.getId());
		assertThat(user.getName()).isEqualTo(expected.getName());
		assertThat(user.getBirthDate()).isEqualTo(expected.getBirthDate());
		assertThat(user.getPhoneNumber()).isEqualTo(expected.getPhoneNumber());
		assertThat(user.getEmail()).isEqualTo(expected.getEmail());
		assertThat(user.getFaceId()).isEqualTo(expected.getFaceId());
	}

	@Test
	@DisplayName("도메인->엔티티 매핑 성공")
	void should_map_domain_to_entity_correctly() {
		// given
		User domain = UserEntityFixture.sampleDomain();

		// when
		UserEntity entity = mapper.toEntity(domain);

		// then
		UserEntity expected = UserEntityFixture.sampleEntity();
		assertThat(entity.getId()).isEqualTo(expected.getId());
		assertThat(entity.getName()).isEqualTo(expected.getName());
		assertThat(entity.getBirthDate()).isEqualTo(expected.getBirthDate());
		assertThat(entity.getPhoneNumber()).isEqualTo(expected.getPhoneNumber());
		assertThat(entity.getEmail()).isEqualTo(expected.getEmail());
		assertThat(entity.getFaceId()).isEqualTo(expected.getFaceId());
	}

}