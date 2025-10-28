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
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.schedulereservationticketing.service.auth.TokenInfo;

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
		log.debug("Access Token 생성. userId: {}", userId);
		return generateToken(userId, accessTokenExpireTime).tokenValue();
	}

	public TokenInfo generateRefreshToken(UUID userId) {
		log.debug("Refresh Token 생성. userId: {}", userId);
		return generateToken(userId, refreshTokenExpireTime);
	}

	private TokenInfo generateToken(UUID userId, long expireTime) {
		final Date now = new Date();
		final Date expiryDate = new Date(now.getTime() + expireTime);

		// 1. 토큰 문자열 생성
		final String tokenValue = Jwts.builder()
			.setSubject(userId.toString())
			.setIssuedAt(now)
			.setExpiration(expiryDate)
			.signWith(key, SignatureAlgorithm.HS512)
			.compact();

		// 2. 만료 시간을 LocalDateTime으로 변환
		final LocalDateTime expiresAt = expiryDate.toInstant()
			.atZone(ZoneId.systemDefault())
			.toLocalDateTime();

		// 3. 두 정보를 TokenInfo 객체에 담아 반환
		return new TokenInfo(tokenValue, expiresAt);
	}

	// JWT 토큰을 파싱하여 만료 시간을 LocalDateTime 객체로 반환하는 메서드
	public LocalDateTime getExpirationDateFromToken(String token) {
		log.debug("토큰에서 만료 시간 추출 시도.");
		// 토큰의 payload 부분(Claims)을 디코딩하여 가져옵니다.
		final Claims claims = parseClaims(token);

		// Claims에서 만료 시간(Date 객체)을 가져와 LocalDateTime으로 변환합니다.
		return claims.getExpiration().toInstant()
			.atZone(ZoneId.systemDefault())
			.toLocalDateTime();
	}

	// JWT 토큰에서 사용자 ID (UUID)를 추출하는 메서드
	public UUID getUserIdFromToken(String token) {
		log.debug("토큰에서 사용자 ID 추출 시도.");
		Claims claims = parseClaims(token);
		// Subject에 저장된 UUID 문자열을 다시 UUID 객체로 변환하여 반환
		return UUID.fromString(claims.getSubject());
	}

	// 토큰 파싱 헬퍼 메서드
	private Claims parseClaims(String token) {
			return Jwts.parserBuilder()
				.setSigningKey(key)
				.build()
				.parseClaimsJws(token)
				.getBody();
	}
}
