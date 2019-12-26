package com.fangyuan.redis.config;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.autoconfigure.data.redis.JedisClientConfigurationBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;

import java.time.Duration;

/**
 * @author fangyuan
 */
@Configuration
class RedisCustomizerConfiguration {

    @Bean
    public MyJedisClientConfigurationBuilderCustomizer builderCustomizers(){

        return new MyJedisClientConfigurationBuilderCustomizer();
    }

    @Bean
    public MyJedisClientConfigurationBuilderCustomizerTwo builderCustomizersTow(){

        return new MyJedisClientConfigurationBuilderCustomizerTwo();
    }


    public class MyJedisClientConfigurationBuilderCustomizer implements JedisClientConfigurationBuilderCustomizer {

        @Override
        public void customize(JedisClientConfiguration.JedisClientConfigurationBuilder clientConfigurationBuilder) {


            JedisClientConfiguration jedisClientConfiguration =  clientConfigurationBuilder.build();

            //获取连接池信息 并做定制化设置
            GenericObjectPoolConfig genericObjectPoolConfig =  jedisClientConfiguration.getPoolConfig().get();

            if("test".equals(jedisClientConfiguration.getClientName().get())){

                //此两个参数在RedisProperties中统一为timeout 所以默认connectTimeout与readTimeout相同
                //可以通过JedisClientConfigurationBuilderCustomizer进行细致化配置
                clientConfigurationBuilder.connectTimeout(Duration.ofSeconds(10));
                clientConfigurationBuilder.readTimeout(Duration.ofSeconds(10));
                clientConfigurationBuilder.usePooling();
            }

        }
    }

    public class MyJedisClientConfigurationBuilderCustomizerTwo implements JedisClientConfigurationBuilderCustomizer {

        @Override
        public void customize(JedisClientConfiguration.JedisClientConfigurationBuilder clientConfigurationBuilder) {

            JedisClientConfiguration jedisClientConfiguration =  clientConfigurationBuilder.build();

            //获取连接池信息 并做定制化设置
            GenericObjectPoolConfig genericObjectPoolConfig =  jedisClientConfiguration.getPoolConfig().get();

            if("test1".equals(jedisClientConfiguration.getClientName().get())) {

                //此两个参数在RedisProperties中统一为timeout 所以默认connectTimeout与readTimeout相同
                //可以通过JedisClientConfigurationBuilderCustomizer进行细致化配置
                clientConfigurationBuilder.connectTimeout(Duration.ofSeconds(6));

                clientConfigurationBuilder.readTimeout(Duration.ofSeconds(6));

                clientConfigurationBuilder.usePooling();
            }
        }
    }
}
