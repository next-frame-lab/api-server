package wisoft.nextframe.schedulereservationticketing.config.jwt;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtTokenProvider {

	private final Key key;
	private final long accessTokenExpireTime;
	private final long refreshTokenExpireTime;

	public JwtTokenProvider(JwtProperties jwtProperties) {
		byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.secretKey());
		this.key = Keys.hmacShaKeyFor(keyBytes);
		this.accessTokenExpireTime = jwtProperties.accessTokenExpireTime();
		this.refreshTokenExpireTime = jwtProperties.refreshTokenExpireTime();
	}

	public String generateAccessToken(UUID userId) {
		return generateToken(userId, accessTokenExpireTime);
	}

	public String generateRefreshToken(UUID userId) {
		return generateToken(userId, refreshTokenExpireTime);
	}

	private String generateToken(UUID userId, long expireTime) {
		final Date now = new Date();
		final Date expiryDate = new Date(now.getTime() + expireTime);

		return Jwts.builder()
			.setSubject(userId.toString()) // 토큰의 주체로 사용자 ID(UUID)를 문자열로 사용
			.setIssuedAt(now)
			.setExpiration(expiryDate)
			.signWith(key, SignatureAlgorithm.HS512)
			.compact();
	}

	/**
	 * JWT 토큰을 파싱하여 만료 시간을 LocalDateTime 객체로 반환하는 메서드
	 */
	public LocalDateTime getExpirationDateFromToken(String token) {
		// 토큰의 payload 부분(Claims)을 디코딩하여 가져옵니다.
		final Claims claims = Jwts.parserBuilder()
			.setSigningKey(key) // 토큰 생성 시 사용한 것과 동일한 secretKey
			.build()
			.parseClaimsJws(token)
			.getBody();

		// Claims에서 만료 시간(Date 객체)을 가져와 LocalDateTime으로 변환합니다.
		return claims.getExpiration().toInstant()
			.atZone(ZoneId.systemDefault())
			.toLocalDateTime();
	}

	/**
	 * JWT 토큰의 유효성을 검증하는 메서드
	 */
	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
			return true;
		} catch (SignatureException e) {
			// 서명 오류
			log.warn("Invalid JWT signature: {}", e.getMessage());
		} catch (io.jsonwebtoken.MalformedJwtException e) {
			// 형식 오류
			log.warn("Invalid JWT token: {}", e.getMessage());
		} catch (io.jsonwebtoken.ExpiredJwtException e) {
			// 만료 오류
			log.warn("JWT token is expired: {}", e.getMessage());
		} catch (io.jsonwebtoken.UnsupportedJwtException e) {
			// 지원되지 않는 토큰
			log.warn("JWT token is unsupported: {}", e.getMessage());
		} catch (IllegalArgumentException e) {
			// 클레임이 비어있는 경우
			log.warn("JWT claims string is empty: {}", e.getMessage());
		}
		return false;
	}

	/**
	 * JWT 토큰에서 사용자 ID (UUID)를 추출하는 메서드
	 */
	public UUID getUserIdFromToken(String token) {
		Claims claims = Jwts.parserBuilder()
			.setSigningKey(key)
			.build()
			.parseClaimsJws(token)
			.getBody();
		// Subject에 저장된 UUID 문자열을 다시 UUID 객체로 변환하여 반환
		return UUID.fromString(claims.getSubject());
	}
}
