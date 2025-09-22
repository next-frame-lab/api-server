package wisoft.nextframe.schedulereservationticketing.config.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
	String secretKey,
	long accessTokenExpireTime,
	long refreshTokenExpireTime
) {
}
