package wisoft.nextframe.schedulereservationticketing.service.auth;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;
import wisoft.nextframe.schedulereservationticketing.repository.performance.PerformanceRepository;
import wisoft.nextframe.schedulereservationticketing.repository.user.UserRepository;

@Service("dynamicAuthService")
@RequiredArgsConstructor
public class DynamicAuthService {

	private final PerformanceRepository performanceRepository;
	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public boolean canViewPerformanceDetail(UUID performanceId, Authentication authentication) {
		// 1. 공연이 성인 공연인지 확인
		final boolean isAdultOnly = performanceRepository.findAdultOnlyById(performanceId)
			.orElse(false);

		// 2. 성인 전용 공연이 아니면 -> 누구나 접근 가능 (true)
		if (!isAdultOnly) {
			return true;
		}

		// 3. 인증 정보가 없거나, Principal이 UUID 타입이 아니면(즉, 익명 사용자) -> 접근 불가 (false)
		if (authentication == null || !(authentication.getPrincipal() instanceof UUID)) {
			return false;
		}

		// 4. 이제 안심하고 UUID로 캐스팅합니다.
		final UUID userId = (UUID)authentication.getPrincipal();
		final User user = userRepository.findById(userId)
			.orElseThrow(() -> new EntityNotFoundException("인증 정보에 해당하는 사용자를 찾을 수 없습니다: " + userId));

		return user.isAdult();
	}
}
