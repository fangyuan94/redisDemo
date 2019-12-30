package com.fangyuan.redis.config;

import com.fangyuan.redis.listener.ConsumerMessageListener;
import com.fangyuan.redis.listener.Recevetor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 用于初始化依赖类
 * @author fangyuan
 */
@Configuration
public class RedisListenerConfiguration {

    /**
     * MessageListenerAdapter此类需要被注入到spring中执行
     * 因为MessageListenerAdapter实现了InitializingBean接口 执行afterPropertiesSet()
     * @return
     */
    @Bean
    public MessageListenerAdapter messageListenerAdapter(){
        //将代理类注入到MessageListenerAdapter 代理执行
        return new MessageListenerAdapter(new Recevetor(),"receve");
    }

    /**
     * 创建监听容器 用于管理监听器
     * @param redisConnectionFactory
     * @return
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            StringRedisSerializer stringRedisSerializer,
            MessageListenerAdapter messageListenerAdapter,
            ObjectProvider<RedisConnectionFactory> redisConnectionFactory){

        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        //设置基础配置
        redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory.getObject());
        redisMessageListenerContainer.setBeanName("redisMessageListenerContainer");
        redisMessageListenerContainer.setTopicSerializer(stringRedisSerializer);
        //创建多监听者 监听不同topic
        //使用自定义监听方式
        MessageListener messageListener =  new ConsumerMessageListener();
        redisMessageListenerContainer.addMessageListener(messageListener,new  ChannelTopic("redis_mq_test_1"));
        //使用适配器方式(通过代理执行设计模式+反射来做到)
        messageListenerAdapter.setStringSerializer(stringRedisSerializer);
        messageListenerAdapter.setSerializer(stringRedisSerializer);
        redisMessageListenerContainer.addMessageListener(messageListenerAdapter,new ChannelTopic("redis_mq_test_2"));

        return redisMessageListenerContainer;
    }
}
