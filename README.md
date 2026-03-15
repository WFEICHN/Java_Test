# jdo_datanucleus
## want's jdo 
```
JDO (Java Data Objects) 是一个 Java 持久化标准规范，类似于 JPA，但更加灵活。
```
## how to run
- 需要提前创建好mysql数据库并配置用户名密码
1. 清除之前的编译
2. 编译
3. enhance
4. 验证enhance
5. 执行
```
mvn clean
mvn compile
mvn datanucleus:enhance
mvn datanucleus:enhance -X
run
```
