--银行存钱模式
local bank = redis.call('get',KEYS[1])
local person = redis.call('get',KEYS[2])
--返回若为null 结果为false
if  bank == false then
    bank = tonumber(ARGV[1])
end
if person == false then
   person = tonumber(ARGV[2])
end
local je = tonumber(ARGV[3])
bank = tonumber(bank) + je
person = tonumber(person) - je
redis.call('set',KEYS[1],bank)
redis.call('set',KEYS[2],person)
--使用..代表字符串连接符
local rs = tostring(bank) .."_"..tostring(person)
return rs
