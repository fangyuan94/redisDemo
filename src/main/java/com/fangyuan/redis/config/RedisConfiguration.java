package com.fangyuan.redis.config;

import com.fangyuan.redis.bean.PersonInfo;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author fangyuan
 */
@Configuration
@AutoConfigureAfter(RedisCustomizerConfiguration.class)
public class RedisConfiguration {

    @Bean
    public RedisTemplate<String, PersonInfo> personInfoRedisTemplate(ObjectProvider<RedisConnectionFactory> redisConnectionFactory){

        RedisTemplate<String, PersonInfo> personInfoRedisTemplate = new RedisTemplate<String, PersonInfo>();

        personInfoRedisTemplate.setConnectionFactory(redisConnectionFactory.getObject());
        //Str序列化起
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        JdkSerializationRedisSerializer jdkSerializationRedisSerializer = new JdkSerializationRedisSerializer(PersonInfo.class.getClassLoader());
        //设置key value序列化器
        personInfoRedisTemplate.setKeySerializer(stringRedisSerializer);
        personInfoRedisTemplate.setValueSerializer(jdkSerializationRedisSerializer);
        personInfoRedisTemplate.setHashKeySerializer(stringRedisSerializer);
        personInfoRedisTemplate.setHashValueSerializer(jdkSerializationRedisSerializer);

        return personInfoRedisTemplate;
    }
}
