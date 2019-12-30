package com.fangyuan.redis.listener;


/**
 * 定义被MessageListenerAdapter被代理执行类
 * @author fangyuan
 */
public class Recevetor {

    /**
     * 接受message 此方法的参数只能为两个参数message,topic) 或者是一个参数(message)
     * @param message
     */
    public void receve(String message,String channel){

        System.out.println("通过MessageListenerAdapter代理方式从"+channel+"中---收到消息----："+message);
    }
}
