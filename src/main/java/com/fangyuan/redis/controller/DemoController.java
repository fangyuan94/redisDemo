package com.fangyuan.redis.controller;

import com.fangyuan.redis.bean.PersonInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author haizi
 */
@RestController
public class DemoController {

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisTemplate<String, PersonInfo> personInfoRedisTemplate;

    private AtomicInteger atomicInteger = new AtomicInteger(1);

    @RequestMapping("test")
    public Map<String,Object> test(){

        stringRedisTemplate.boundHashOps("key");

        Map<String,Object> map = new HashMap<>(3);
        map.put("success",true);
        map.put("data","1");
        return map;
    }
    @RequestMapping("addPersonInfo")
    public Map<String,Object> addPersonInfo(){

        String key = "personInfo";
        PersonInfo personInfo = new PersonInfo();
        personInfo.setUserId("1");
        personInfo.setAge(18);
        personInfo.setName("小花");
        personInfo.setSex("女");
        //操作string
        personInfoRedisTemplate.opsForValue().set(key+"_str",personInfo);
        //操作list
        personInfoRedisTemplate.opsForList().leftPush(key+"list",personInfo);
        //操作set
        personInfoRedisTemplate.opsForSet().add(key+"_set",personInfo);
        //操作有序set
        personInfoRedisTemplate.opsForZSet().add(key+"_ZSet",personInfo,100);
        //操作hash散列
        personInfoRedisTemplate.opsForHash().put(key+"_Map",personInfo.getUserId(),personInfo);

        Map<String,Object> map = new HashMap<>(3);
        map.put("success",true);
        map.put("data","1");
        return map;
    }

    @RequestMapping("getPersonInfo")
    public Map<String,Object> getPersonInfo(){

        String key = "personInfo";

        //操作string
        PersonInfo personInfo1 = personInfoRedisTemplate.opsForValue().get(key+"_str");
        //操作list
        PersonInfo personInfo2 = personInfoRedisTemplate.opsForList().leftPop(key+"list");
        //操作set
        PersonInfo personInfo3 =personInfoRedisTemplate.opsForSet().pop(key+"_set");
        //操作有序set
        Set<PersonInfo> personInfos = personInfoRedisTemplate.opsForZSet().range(key+"_ZSet",0,100);
        //操作hash散列
        PersonInfo personInfo5 = (PersonInfo) personInfoRedisTemplate.opsForHash().get(key+"_Map","1");

        Map<String,Object> map = new HashMap<>(3);
        map.put("str",personInfo1);
        map.put("list",personInfo2);
        map.put("set",personInfo3);
        map.put("zSet",personInfos);
        map.put("hash",personInfo5);
        return map;
    }

    /**
     * RedisTemplate 对于pipeline支持
     * 提供SessionCallback与RedisCallback两种 作用一样
     * @return
     */
    @RequestMapping("pipelineTest")
    public  Map<String,Object> pipelineTest(){

        //测试数据
       final List<PersonInfo> personInfosTest = new ArrayList<>();

        for (int i=0;i<50;i++){
            PersonInfo personInfo = new PersonInfo();
            personInfo.setUserId(""+i);
            personInfo.setAge(i);
            personInfo.setName("小花"+i);
            personInfo.setSex("女");
            personInfosTest.add(personInfo);
       }

        final String key = "personInfo_pipeline_";

        //SessionCallback 属于高级 代码书写非常友好
        personInfoRedisTemplate.executePipelined(new SessionCallback<Object>() {
            @Override
            public  Object execute(RedisOperations operations) throws DataAccessException {
                for (int i=0;i<personInfosTest.size();i++){

                    PersonInfo personInfo = personInfosTest.get(i);
                    int j = i%5;
                    //使用不同命令
                    if(j==0){
                        operations.opsForValue().set(key+"_str",personInfo);
                    }else if(j==1){
                        operations.opsForHash().put(key+"_Map",personInfo.getUserId(),personInfo);
                    }else if(j==2){
                        operations.opsForList().leftPush(key+"list",personInfo);
                    }else if(j==3){
                        operations.opsForZSet().add(key+"_ZSet",personInfo,100);
                    }else {
                        operations.opsForSet().add(key+"_set",personInfo);
                    }
                }
                return null;
            }
        },personInfoRedisTemplate.getValueSerializer());

        //RedisCallback偏底层些 处理byt[]类型
        List<Object> ts = personInfoRedisTemplate.executePipelined(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {

                for (int i=0;i<personInfosTest.size();i++){

                    PersonInfo personInfo = personInfosTest.get(i);
                    int j = i%5;

                    RedisSerializer stringSerializer = personInfoRedisTemplate.getKeySerializer();

                    RedisSerializer valueSerializer = personInfoRedisTemplate.getValueSerializer();
                    //使用不同命令
                    if(j==0){
                        connection.set(stringSerializer.serialize(key+"str"),valueSerializer.serialize(personInfo));
                    }else if(j==1){
                        connection.hSet(stringSerializer.serialize(key+"hash"),stringSerializer.serialize(personInfo.getUserId()),valueSerializer.serialize(personInfo));
                    }else if(j==2){
                        connection.lPush(stringSerializer.serialize(key+"list"),valueSerializer.serialize(personInfo));
                    }else if(j==3){
                        connection.zAdd(stringSerializer.serialize(key+"zSet"),100,valueSerializer.serialize(personInfo));
                    }else {
                        connection.sAdd(stringSerializer.serialize(key+"set"),valueSerializer.serialize(personInfo));
                    }
                }
                //返回结果必须为null
                return null;
            }
        },personInfoRedisTemplate.getValueSerializer());

        Map<String,Object> map = new HashMap<>(3);
        map.put("success",true);
        map.put("data",ts);

        return map;
    }

    /**
     * redis对于事务的支持
     * @return
     */
    @RequestMapping("transactionalTest")
    public  Map<String,Object> transactionalTest(){

        //这里模拟用户取钱的场景 初始银行是10000 用户是100 每次用户取1
        //这里通过压测工具模拟100个并发 5000个请求是否能保证数据的一致性
        final String bank_key = "transactional_bank"; //默认为10000
        final String person_key = "transactional_person";//默认为100
        List<String> watchs = new ArrayList<>();
        watchs.add(bank_key);
        watchs.add(person_key);
        //执行事务
        personInfoRedisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                List rs;
                do {
                    //监控需要变化key
                    operations.watch(watchs);
                    //对应multi命令
                    operations.multi();
                    operations.opsForValue().decrement(bank_key,1);
                    //需要执行命令
                    operations.opsForValue().increment(person_key,1);
                    //提交
                    rs = operations.exec();

                    System.out.println(rs);

                    //和CAS中自旋概念类似
                }while (rs!=null && rs.size()==0);

                return null;
            }
        });


        Map<String,Object> map = new HashMap<>(3);
        map.put("success",true);
        map.put("data",1);

        return map;
    }

    /**
     * redis对于Lua支持
     * @return
     */
    @RequestMapping("LuaTest")
    public  Map<String,Object> LuaTest(){

        //创建脚本
        DefaultRedisScript<String> defaultRedisScript = new DefaultRedisScript();

        //redis.call  redis执行命令 KEYS代表redis中key ARGV代表参数 return指需要返回数据
        String luaText = "redis.call('set',KEYS[1],ARGV[1]) return redis.call('get',KEYS[1])";
        //设置脚本内容
        defaultRedisScript.setScriptText(luaText);
        //也可以通过将lue写入文件方式进行调用易于维护
//        defaultRedisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/depositMoney.lua")));
        //设置脚本数据返回类型
        defaultRedisScript.setResultType(String.class);
        //执行命令
        //第一个代表的需要执行的脚本
        //第二对应着lua中的KEYS信息 KEYS下标从1开始
        //第三个是对应ARGV中指 ARGV下标从1开始
        String rs = stringRedisTemplate.execute(defaultRedisScript,Collections.singletonList("lua_test"),"测试lua数据");

        Map<String,Object> map = new HashMap<>(3);
        map.put("success",true);
        map.put("data",rs);

        return map;
    }

    /**
     * 测试
     * @return
     */
    @RequestMapping("LuaFileTest")
    public  Map<String,Object> LuaFileTest(){

        //创建脚本
        DefaultRedisScript<String> defaultRedisScript = new DefaultRedisScript();

        //也可以通过将lue写入文件方式进行调用易于维护
        defaultRedisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/depositMoney.lua")));
        //设置脚本数据返回类型
        defaultRedisScript.setResultType(String.class);
        //redis 需要操作key
        List<String> keys = new ArrayList<>();
        keys.add("lua_bank");
        keys.add("lua_person");
        //
        Object[] args = new Object[]{"10000","10000","1"} ;
        //执行命令
        //第一个代表的需要执行的脚本
        //第二对应着lua中的KEYS信息 KEYS下标从1开始
        //第三个是对应ARGV中指 ARGV下标从1开始
        String rs = stringRedisTemplate.execute(defaultRedisScript,keys,args);

        System.out.println("-------结果集-----"+rs);

        Map<String,Object> map = new HashMap<>(3);
        map.put("success",true);
        map.put("data",rs);

        return map;
    }

    /**
     * 高并发下秒杀商品代码实现
     * @param userId 这里是模拟代码 所以userId 以参数的形式
     * @return
     */
    @RequestMapping("seckillMerchandise")
    public  Map<String,Object> seckillMerchandise(@RequestParam("userId") String userId){

        //创建脚本
        DefaultRedisScript<Long> defaultRedisScript = new DefaultRedisScript();

        //也可以通过将lue写入文件方式进行调用易于维护
        defaultRedisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/seckillMerchandise.lua")));
        //设置脚本数据返回类型
        defaultRedisScript.setResultType(Long.class);
        //redis 需要操作key
        List<String> keys = new ArrayList<>();
        //记录商品总数 这里测试数据20个
        keys.add("seckill_merchandise_count"+"_spid");
        //记录抢购到的用户记录
        keys.add("seckill_merchandise_user"+"_spid");

        //用户id
        Object[] args = new Object[]{userId,System.currentTimeMillis()+""} ;
        //执行命令
        //第一个代表的需要执行的脚本
        //第二对应着lua中的KEYS信息 KEYS下标从1开始
        //第三个是对应ARGV中指 ARGV下标从1开始
        Long rs = stringRedisTemplate.execute(defaultRedisScript,keys,args);
        //根据不同结果进行不同处理 ---
        System.out.println("-------结果集-----:"+rs);

        Map<String,Object> map = new HashMap<>(3);
        map.put("success",true);
        map.put("data",rs);

        return map;
    }

    /**
     * 发送信息到mq
     * @return
     */
    @RequestMapping("produceMessage")
    public  Map<String,Object> produceMessage(){

        String channel = "redis_mq_test_1";

        int count = atomicInteger.incrementAndGet();

        if(count%2==0){
            channel = "redis_mq_test_2";
        }

        String message = "测试使用redis作为MQ发送数据__"+count;
        //发布信息
        stringRedisTemplate.convertAndSend(channel,message);

        Map<String,Object> map = new HashMap<>(3);
        map.put("success",true);
        map.put("data",1);

        return map;
    }



}
