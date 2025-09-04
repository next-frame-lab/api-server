package wisoft.nextframe.schedulereservationticketing.config.jwt;


import java.security.Key;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
}
