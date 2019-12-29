#!/bin/bash
###使用ab构建测试脚本
for (( i = 0; i < 100; i++ )); do
    ab -n 3 -c 1 'http://host.docker.internal:18670/seckillMerchandise?userId=userId_'+i
done