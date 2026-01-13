package wisoft.nextframe.schedulereservationticketing.repository.user;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import wisoft.nextframe.schedulereservationticketing.entity.user.RefreshToken;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

	Optional<RefreshToken> findByUser(User user);

	@Query("SELECT r FROM RefreshToken r JOIN FETCH r.user WHERE r.user.id = :userId")
	Optional<RefreshToken> findByUserIdWithUser(@Param("userId") UUID userId);

	@Modifying
	@Query("DELETE FROM RefreshToken r WHERE r.user.id = :userId")
	void deleteByUserId(UUID userId);
}
