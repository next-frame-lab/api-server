package wisoft.nextframe.schedulereservationticketing.repository.user;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import wisoft.nextframe.schedulereservationticketing.entity.user.User;

public interface UserRepository extends JpaRepository<User, UUID> {
}
