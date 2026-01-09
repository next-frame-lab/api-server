package wisoft.nextframe.schedulereservationticketing.config.db;

import static org.springframework.data.redis.serializer.RedisSerializationContext.*;
import static wisoft.nextframe.schedulereservationticketing.config.db.RedisConsts.KEY_PREFIX;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import wisoft.nextframe.schedulereservationticketing.dto.seat.seatstate.SeatStateListResponse;

@Configuration
public class RedisCacheConfig {

	/**
	 * CacheManager Bean 등록
	 * Spring의 캐시 추상화(@Cacheable 등)가 Redis를 사용하도록 설정합니다.
	 */
	@Bean
	public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());

		GenericJackson2JsonRedisSerializer redisSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

		// seatStates 전용 타입 고정 Serializer
		Jackson2JsonRedisSerializer<SeatStateListResponse> seatStatesSerializer =
			new Jackson2JsonRedisSerializer<>(
				objectMapper,
				SeatStateListResponse.class
			);

		// 1. Redis 캐시 기본 설정
		RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
			// 캐시 데이터의 기본 유효 시간(TTL)을 5분으로 설정
			.entryTtl(Duration.ofMinutes(5))
			.serializeKeysWith(SerializationPair.fromSerializer(new StringRedisSerializer()))
			.serializeValuesWith(SerializationPair.fromSerializer(redisSerializer))
			// 모든 캐시 Key 앞에 접두사 붙임
			.prefixCacheNameWith(KEY_PREFIX);

		// 2. 캐시 이름별 커스텀 TTL 설정
		Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
		// 공연 목록: 변경이 적으므로 10분 (기본보다 김)
		cacheConfigurations.put("performanceList", defaultConfig.entryTtl(Duration.ofMinutes(10)));
		// 인기 공연(Top 10): 집계 데이터이므로 1시간 (오래 유지)
		cacheConfigurations.put("top10Performances", defaultConfig.entryTtl(Duration.ofHours(1)));
		// 좌석 상태: 실시간성이 중요하므로 1분 (짧게 유지)
		cacheConfigurations.put(
			"seatStates",
			defaultConfig
				.entryTtl(Duration.ofMinutes(1))
				.serializeValuesWith(
					SerializationPair.fromSerializer(seatStatesSerializer)
				)
		);


		// 3. RedisCacheManager 생성 및 반환
		return RedisCacheManager.builder(redisConnectionFactory)
			.cacheDefaults(defaultConfig) // 기본 설정 적용
			.withInitialCacheConfigurations(cacheConfigurations) // 개별 설정 적용
			.build();
	}
}
