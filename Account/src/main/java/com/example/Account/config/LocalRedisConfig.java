
package com.example.Account.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import redis.embedded.RedisServer;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
@Configuration
public class LocalRedisConfig {
    @Value("${spring.redis.port:6379}")
    private int redisPort;

    private RedisServer redisServer;

    @PostConstruct
    public void startRedis() throws IOException{
        redisServer = new RedisServer(redisPort);
        try{
            redisServer.start();
        }
        catch (Exception e){
            System.out.println(e);
        }

    }

    @PreDestroy
    public void stopRedis() {
        if (redisServer != null) {
            redisServer.stop();
        }
    }
}

