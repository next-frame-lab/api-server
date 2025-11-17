package wisoft.nextframe.schedulereservationticketing.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import wisoft.nextframe.schedulereservationticketing.entity.user.SocialAccount;

import java.util.Optional;
import java.util.UUID;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, UUID> {
    Optional<SocialAccount> findByProviderAndProviderUserId(String provider, String providerUserId);
}
