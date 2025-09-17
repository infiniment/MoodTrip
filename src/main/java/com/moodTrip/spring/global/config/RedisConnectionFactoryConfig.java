package com.moodTrip.spring.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.time.Duration;
import java.util.Collections;

@Configuration
public class RedisConnectionFactoryConfig {

    // yml/ENV에 이미 넣은 값과 매칭
    @Value("${spring.redis.cluster.nodes:clustercfg.moodtrip-redis.cjw2ml.apn2.cache.amazonaws.com:6379}")
    private String clusterNodes;

    @Value("${spring.redis.ssl.enabled:true}")
    private boolean sslEnabled;

    // (ElastiCache AUTH 토큰을 쓰면 값 채움, 아니면 빈값)
    @Value("${spring.redis.password:}")
    private String password;

    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        RedisClusterConfiguration cluster =
                new RedisClusterConfiguration(Collections.singletonList(clusterNodes));

        if (password != null && !password.isEmpty()) {
            cluster.setPassword(RedisPassword.of(password));
        }

        LettuceClientConfiguration.LettuceClientConfigurationBuilder client =
                LettuceClientConfiguration.builder()
                        .commandTimeout(Duration.ofSeconds(5));
        if (sslEnabled) {
            client.useSsl();
        }
        return new LettuceConnectionFactory(cluster, client.build());
    }
}
