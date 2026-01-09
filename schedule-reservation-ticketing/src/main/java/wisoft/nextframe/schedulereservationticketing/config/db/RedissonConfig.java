package wisoft.nextframe.schedulereservationticketing.config.db;

import static wisoft.nextframe.schedulereservationticketing.config.db.RedisConsts.KEY_PREFIX;

import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.redisson.api.NameMapper;

@Configuration
public class RedissonConfig {

    @Bean
    public RedissonAutoConfigurationCustomizer redissonCustomizer() {
        return config -> {
            // 1. 키 이름 매핑 규칙 정의
            // Redisson이 Redis에 접근할 때 키 이름을 자동으로 변환해주는 로직
            NameMapper nameMapper = new NameMapper() {

                // 애플리케이션 -> Redis: 저장/조회 시 접두사 추가
                // 예: "myLock" -> "next-frame:myLock"
                @Override
                public String map(String name) {
                    return KEY_PREFIX + name;
                }

                // Redis -> 애플리케이션: 키 이름을 반환받을 때 접두사 제거
                // 예: "next-frame:myLock" -> "myLock"
                @Override
                public String unmap(String name) {
                    if (name.startsWith(KEY_PREFIX)) {
                        return name.substring(KEY_PREFIX.length());
                    }
                    return name;
                }
            };

            // 2. Redis 운영 모드(Single, Cluster, Sentinel)에 따라 NameMapper 적용
            if (config.isClusterConfig()) {
                // 클러스터 모드인 경우
                config.useClusterServers().setNameMapper(nameMapper);
            } else if (config.isSentinelConfig()) {
                // 센티널(고가용성) 모드인 경우
                config.useSentinelServers().setNameMapper(nameMapper);
            } else {
                // 싱글(단일 서버) 모드인 경우 (로컬 개발 환경 등)
                config.useSingleServer().setNameMapper(nameMapper);
            }
        };
    }
}
