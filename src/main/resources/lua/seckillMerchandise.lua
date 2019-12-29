--秒杀商品lua代码
--返回状态:
-- 0 当前商品已售空
-- -1 该用户已经抢购了该商品了
-- -2 代表存储秒杀商品总数的key不存在 非法数据无法处理或者抢购时间已结束
-- 1代表正常返回
--用户key
local userId = ARGV[1]
--存储秒杀商品总数的key 该值在设定抢购开始时写入缓存 并设置过期时间
local seckillMerchandiseCountKey = tostring(KEYS[1])
--目前商品剩余总数
local count =  redis.call('GET',seckillMerchandiseCountKey)

if count == false then
    --代表存储秒杀商品总数的key不存在 非法数据无法处理或者抢购时间已结束
   return -2
end
--转换为数值类型
count = tonumber(count)
if count  <= 0 then
    --代表当前商品已售空
    return 0
end

--记录已抢够成功用户列表 用于判断用户是否抢购
--因为秒杀活动不是针对于一种商品 所以这里以商品id+一些唯一标示组合为key value为hash散列
local seckillMerchandiseUserKey = tostring(KEYS[2])

local flag = redis.call('HGET',seckillMerchandiseUserKey,userId)
if flag == true then
    --该用户已经抢购了该商品了
    return -1
end
--该用户抢购了该商品了 需要对数据进行变更
--商品 -1
redis.call('DECRBY',seckillMerchandiseCountKey,1)
--将用户写入 并记录抢购成功时间
redis.call('HSET',seckillMerchandiseUserKey,userId,ARGV[2])
return 1

