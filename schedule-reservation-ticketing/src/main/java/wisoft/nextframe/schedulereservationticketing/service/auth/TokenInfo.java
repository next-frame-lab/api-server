package wisoft.nextframe.schedulereservationticketing.service.auth;

import java.time.LocalDateTime;

/**
 * JWT의 값과 만료 시간 정보를 함께 전달하기 위한 DTO
 * * @param tokenValue  실제 JWT 문자열
 * @param expiresAt   토큰의 만료 시간
 */
public record TokenInfo(
	String tokenValue,
	LocalDateTime expiresAt
) {
}
