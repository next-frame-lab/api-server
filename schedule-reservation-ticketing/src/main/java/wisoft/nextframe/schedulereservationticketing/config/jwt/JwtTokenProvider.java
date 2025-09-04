package wisoft.nextframe.schedulereservationticketing.config.jwt;


import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {

	private final Key key;
	private final long accessTokenExpireTime;
	private final long refreshTokenExpireTime;

	public JwtTokenProvider(
		@Value("${jwt.secret-key}") String secretKey,
		@Value("${jwt.access-token-expire-time}") long accessTokenExpireTime,
		@Value("${jwt.refresh-token-expire-time}") long refreshTokenExpireTime
	) {
		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		this.key = Keys.hmacShaKeyFor(keyBytes);
		this.accessTokenExpireTime = accessTokenExpireTime;
		this.refreshTokenExpireTime = refreshTokenExpireTime;
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
	 * [추가] JWT 토큰을 파싱하여 만료 시간을 LocalDateTime 객체로 반환하는 메서드
	 * @param token 만료 시간을 추출할 JWT 토큰
	 * @return 토큰의 만료 시간을 담은 LocalDateTime 객체
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
}
