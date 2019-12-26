package com.fangyuan.redis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * @author fangyuan
 */
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
})
public class Application {

    public static void main(String[] args) {

        SpringApplication.run(Application.class, args);
    }

}
