package wisoft.nextframe.user.infra;

import wisoft.nextframe.common.mapper.EntityMapper;
import wisoft.nextframe.user.domain.User;
import wisoft.nextframe.user.domain.UserId;

public class UserMapper implements EntityMapper<User, UserEntity> {
	@Override
	public User toDomain(UserEntity entity) {
		return User.reconstruct(
			UserId.of(entity.getId()),
			entity.getName(),
			entity.getBirthDate(),
			entity.getPhoneNumber(),
			entity.getEmail(),
			entity.getFaceId()
		);
	}

	@Override
	public UserEntity toEntity(User domain) {
		return UserEntity.builder()
			.id(domain.getId().getValue())
			.name(domain.getName())
			.birthDate(domain.getBirthDate())
			.phoneNumber(domain.getPhoneNumber())
			.email(domain.getEmail())
			.faceId(domain.getFaceId())
			.build();
	}
}
