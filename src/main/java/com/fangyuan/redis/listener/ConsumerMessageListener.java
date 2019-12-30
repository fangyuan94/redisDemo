package com.fangyuan.redis.listener;

import lombok.AllArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 监听消息
 * @author fangyuan
 */
public class ConsumerMessageListener implements MessageListener {

    private StringRedisSerializer stringRedisSerializer;

    public  ConsumerMessageListener(){
        stringRedisSerializer = new StringRedisSerializer();
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {

        String data = stringRedisSerializer.deserialize(message.getBody());

        String channel = stringRedisSerializer.deserialize(message.getChannel());
        System.out.println("从"+channel+"中---收到消息----："+data);

    }
}
