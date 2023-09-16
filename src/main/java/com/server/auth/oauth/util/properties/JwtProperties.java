package com.server.auth.oauth.util.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "spring.jwt")
public class JwtProperties {
    private String secret = "0ybdf9CNPzGoEMXNE8dfIHfrErsPdbPF+ckcR6D7fArDH5J6zLVnKjCKpreE/Frok5hB/UQNLwn4t2xSE324Sg==";
}