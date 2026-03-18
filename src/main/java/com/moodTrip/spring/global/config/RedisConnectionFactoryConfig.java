package com.moodTrip.spring.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class RedisConnectionFactoryConfig {

    @Value("${spring.data.redis.cluster.nodes:}")
    private String clusterNodes;

    @Value("${spring.data.redis.host:localhost}")
    private String host;

    @Value("${spring.data.redis.port:6379}")
    private int port;

    @Value("${spring.data.redis.ssl.enabled:false}")
    private boolean sslEnabled;

    @Value("${spring.data.redis.password:}")
    private String password;

    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        LettuceClientConfiguration.LettuceClientConfigurationBuilder client =
                LettuceClientConfiguration.builder()
                        .commandTimeout(Duration.ofSeconds(5));
        if (sslEnabled) {
            client.useSsl();
        }

        if (StringUtils.hasText(clusterNodes)) {
            List<String> nodes = Arrays.stream(clusterNodes.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList());
            RedisClusterConfiguration cluster = new RedisClusterConfiguration(nodes);
            if (StringUtils.hasText(password)) {
                cluster.setPassword(RedisPassword.of(password));
            }
            return new LettuceConnectionFactory(cluster, client.build());
        }

        RedisStandaloneConfiguration standalone = new RedisStandaloneConfiguration(host, port);
        if (StringUtils.hasText(password)) {
            standalone.setPassword(RedisPassword.of(password));
        }
        return new LettuceConnectionFactory(standalone, client.build());
    }
}
