# jdo_datanucleus
```
  📚 一、JDO 核心思想

  1.1 什么是 JDO？

  JDO (Java Data Objects) 是一个 Java 持久化标准规范，类似于 JPA，但更加灵活。

  核心理念：
  - POJO 持久化 - 普通 Java 对象即可持久化，无需继承特殊类
  - 透明持久化 - 开发者专注业务逻辑，持久化细节由框架处理
  - 查询语言独立 - 支持 JDOQL、SQL 等多种查询方式
  - 数据存储无关 - 不仅支持关系型数据库，还支持 NoSQL、文件系统等

  1.2 DataNucleus 是什么？

  DataNucleus 是 JDO 规范的一个实现（类似于 Hibernate 是 JPA 的实现）。

  ---
  🔧 二、使用 JDO DataNucleus 的完整步骤

  步骤 1️⃣ ：Maven 依赖配置

  <dependencies>
      <!-- JDO API - 标准接口 -->
      <dependency>
          <groupId>javax.jdo</groupId>
          <artifactId>jdo-api</artifactId>
          <version>3.2.1</version>
      </dependency>

      <!-- DataNucleus Core - 核心实现 -->
      <dependency>
          <groupId>org.datanucleus</groupId>
          <artifactId>datanucleus-core</artifactId>
          <version>6.0.7</version>
      </dependency>

      <!-- DataNucleus JDO API 实现 -->
      <dependency>
          <groupId>org.datanucleus</groupId>
          <artifactId>datanucleus-api-jdo</artifactId>
          <version>6.0.7</version>
      </dependency>

      <!-- DataNucleus RDBMS - 关系数据库支持 -->
      <dependency>
          <groupId>org.datanucleus</groupId>
          <artifactId>datanucleus-rdbms</artifactId>
          <version>6.0.7</version>
      </dependency>

      <!-- 数据库驱动 -->
      <dependency>
          <groupId>com.mysql</groupId>
          <artifactId>mysql-connector-j</artifactId>
          <version>8.0.33</version>
      </dependency>
  </dependencies>

  步骤 2️⃣ ：配置字节码增强插件（关键！）

  <plugin>
      <groupId>org.datanucleus</groupId>
      <artifactId>datanucleus-maven-plugin</artifactId>
      <version>6.0.0-release</version>
      <configuration>
          <api>JDO</api>
          <persistenceUnitName>MyPersistenceUnit</persistenceUnitName>
          <verbose>true</verbose>
      </configuration>
      <executions>
          <execution>
              <phase>process-classes</phase>
              <goals>
                  <goal>enhance</goal>
              </goals>
          </execution>
      </executions>
  </plugin>

  ⚠️ 为什么需要字节码增强？
  - DataNucleus 通过字节码增强为实体类添加持久化能力
  - 增强后的类会包含额外的字段和方法（如 jdoStateManager、jdoFlags 等）
  - 这是 JDO 工作的前提条件

  步骤 3️⃣ ：创建持久化配置文件 (persistence.xml)

  路径：src/main/resources/META-INF/persistence.xml

  <persistence-unit name="MyPersistenceUnit">
      <provider>org.datanucleus.api.jdo.JDOPersistenceManagerFactory</provider>

      <!-- 注册实体类 -->
      <class>com.example.jdo.User</class>

      <properties>
          <!-- 数据库连接 -->
          <property name="javax.jdo.option.ConnectionURL"
                    value="jdbc:mysql://localhost:3306/jdo_test"/>
          <property name="javax.jdo.option.ConnectionUserName" value="root"/>
          <property name="javax.jdo.option.ConnectionPassword" value="root"/>
          <property name="javax.jdo.option.ConnectionDriverName"
                    value="com.mysql.cj.jdbc.Driver"/>

          <!-- 自动建表 -->
          <property name="datanucleus.schema.autoCreateAll" value="true"/>
      </properties>
  </persistence-unit>

  步骤 4️⃣ ：定义实体类

  @PersistenceCapable(table = "users", detachable = "true")
  public class User implements Serializable {

      @PrimaryKey
      @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
      @Column(name = "id")
      private Long id;

      @Persistent
      @Column(name = "username", length = 50)
      private String username;

      // getters/setters...
  }

  步骤 5️⃣ ：执行字节码增强

  mvn clean compile datanucleus:enhance

  步骤 6️⃣ ：编写 DAO 进行 CRUD 操作

  public class UserDAO {
      private PersistenceManagerFactory pmf;

      public UserDAO() {
          // 获取 PersistenceManagerFactory
          pmf = JDOHelper.getPersistenceManagerFactory("MyPersistenceUnit");
      }

      public void insertUser(User user) {
          PersistenceManager pm = pmf.getPersistenceManager();
          Transaction tx = pm.currentTransaction();
          try {
              tx.begin();
              pm.makePersistent(user);  // 持久化对象
              tx.commit();
          } finally {
              if (tx.isActive()) tx.rollback();
              pm.close();
          }
      }
  }

  ---
  🏷️ 三、核心注解详解

  3.1 类级别注解

  @PersistenceCapable

  标记一个类为持久化类

  @PersistenceCapable(
      table = "users",           // 表名
      detachable = "true",       // 是否支持分离（重要！）
      identityType = IdentityType.APPLICATION  // 主键类型
  )

  detachable = "true" 的作用：
  - 允许对象脱离 PersistenceManager 的管理
  - 对象可以在不同层之间传递（如从 DAO 层返回到 Service 层）
  - 如果不设置，对象只能在 PersistenceManager 的生命周期内使用

  3.2 主键注解

  ⭐ 方式1：Application Identity（推荐）

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long id;

  主键生成策略：
  - IDENTITY - 数据库自增（MySQL AUTO_INCREMENT）
  - SEQUENCE - 使用数据库序列（Oracle）
  - INCREMENT - DataNucleus 自己维护最大值
  - UUID - 自动生成 UUID

  ❌ 方式2：Datastore Identity（不要和 Application Identity 混用）

  @PersistenceCapable
  @DatastoreIdentity(strategy = IdGeneratorStrategy.IDENTITY)
  public class User {
      // 不要手动定义 id 字段！
  }

  ⚠️ 重要陷阱：
  // ❌ 错误：同时使用两种策略会导致列冲突
  @DatastoreIdentity(strategy = IdGeneratorStrategy.IDENTITY, column = "id")
  public class User {
      @Persistent
      @Column(name = "id")  // ❌ 重复定义 id 列！
      private Long id;
  }

  3.3 字段注解

  @Persistent

  标记字段为持久化字段

  @Persistent(
      valueStrategy = IdGeneratorStrategy.IDENTITY,  // 主键生成策略
      defaultFetchGroup = "true"  // 是否在默认加载组
  )
  private String username;

  @Column

  定义列属性

  @Column(
      name = "username",        // 列名
      length = 50,              // 长度
      allowsNull = "false",     // 是否允许 NULL
      jdbcType = "VARCHAR"      // JDBC 类型
  )
  private String username;

  @NotPersistent

  排除字段不持久化

  @NotPersistent
  private transient String tempData;  // 不会存储到数据库

  ---
  🔄 四、PersistenceManager 生命周期

  4.1 核心概念

  PersistenceManagerFactory (PMF)  ← 整个应用共享，线程安全
      ↓
  PersistenceManager (PM)          ← 每次操作创建，非线程安全
      ↓
  Transaction                       ← 事务管理

  4.2 标准使用模式

  PersistenceManager pm = pmf.getPersistenceManager();
  Transaction tx = pm.currentTransaction();
  try {
      tx.begin();

      // 执行持久化操作
      pm.makePersistent(user);

      tx.commit();
  } catch (Exception e) {
      if (tx.isActive()) {
          tx.rollback();  // 回滚
      }
      throw e;
  } finally {
      pm.close();  // ⚠️ 必须关闭！
  }

  4.3 对象状态

  JDO 对象有以下状态：

  1. Transient（瞬态） - 新创建的对象，未被 PM 管理
  2. Persistent（持久态） - 被 PM 管理，与数据库同步
  3. Detached（分离态） - 曾经持久化，但 PM 已关闭
  4. Hollow（空壳） - 只有主键，字段未加载

  User user = new User();              // Transient

  pm.makePersistent(user);             // Persistent
  tx.commit();

  pm.close();                          // Detached

  User detached = pm.detachCopy(user); // 显式分离

  ---
  🔍 五、查询方式

  5.1 JDOQL（JDO Query Language）

  // 方式1：字符串查询
  Query<User> query = pm.newQuery(User.class, "username == :name");
  query.setParameters("张三");
  List<User> results = query.executeList();

  // 方式2：类型安全查询
  Query<User> query = pm.newQuery(User.class);
  query.setFilter("age > 18");
  query.setOrdering("age desc");
  List<User> results = query.executeList();

  5.2 SQL 查询

  Query query = pm.newQuery("javax.jdo.query.SQL",
      "SELECT * FROM users WHERE age > ?");
  query.setClass(User.class);
  List<User> results = (List<User>) query.execute(18);

  ---
  ⚠️ 六、关键注意事项

  6.1 必须执行字节码增强

  # 每次修改实体类后必须重新增强
  mvn compile datanucleus:enhance

  检查是否增强成功：
  javap -v target/classes/com/example/jdo/User.class | grep "jdo"
  # 应该看到 jdoStateManager、jdoFlags 等字段

  6.2 PersistenceManager 不是线程安全的

  // ❌ 错误：多个线程共享 PM
  private PersistenceManager pm = pmf.getPersistenceManager();

  // ✅ 正确：每次操作创建新的 PM
  public void insertUser(User user) {
      PersistenceManager pm = pmf.getPersistenceManager();
      try {
          // ...
      } finally {
          pm.close();
      }
  }

  6.3 分离对象（Detached Objects）

  // ❌ 错误：对象脱离 PM 后无法访问
  pm.close();
  user.getUsername();  // 可能抛出异常

  // ✅ 正确：使用 detachCopy
  User detached = pm.detachCopy(user);
  pm.close();
  detached.getUsername();  // 正常访问

  6.4 事务管理

  // ✅ 标准模式
  tx.begin();
  try {
      // 操作
      tx.commit();
  } catch (Exception e) {
      if (tx.isActive()) {
          tx.rollback();
      }
      throw e;
  }

  6.5 懒加载问题

  @Persistent(defaultFetchGroup = "false")  // 懒加载
  private List<Order> orders;

  // ❌ PM 关闭后访问会出错
  pm.close();
  user.getOrders();  // LazyInitializationException

  // ✅ 在 PM 开启时访问或使用 FetchPlan
  pm.getFetchPlan().addGroup("orders");

  ---
  📋 七、常见配置选项

  <!-- 自动建表 -->
  <property name="datanucleus.schema.autoCreateAll" value="true"/>

  <!-- 验证表结构 -->
  <property name="datanucleus.schema.validateTables" value="false"/>

  <!-- 显示 SQL -->
  <property name="datanucleus.storeManagerType" value="rdbms"/>

  <!-- 标识符命名策略 -->
  <property name="datanucleus.identifier.case" value="LowerCase"/>

  <!-- 连接池 -->
  <property name="datanucleus.ConnectionPoolingType" value="DBCP"/>

  ---
  🎯 八、最佳实践总结

  1. ✅ 使用 Application Identity 而不是 Datastore Identity
  2. ✅ 设置 detachable="true" 允许对象分离
  3. ✅ 每次修改实体类后重新增强
  4. ✅ 总是在 finally 中关闭 PersistenceManager
  5. ✅ 使用 try-catch 管理事务
  6. ✅ 一个 PMF 对应整个应用，PM 每次创建
  7. ✅ 查询后使用 detachCopy 分离对象
  8. ✅ 开发时开启 SQL 日志便于调试

  ---
  🆚 九、JDO vs JPA 对比

  ┌────────────┬─────────────────────────┬────────────────────┐
  │    特性    │           JDO           │        JPA         │
  ├────────────┼─────────────────────────┼────────────────────┤
  │ 数据存储   │ 支持 RDBMS、NoSQL、文件 │ 主要针对 RDBMS     │
  ├────────────┼─────────────────────────┼────────────────────┤
  │ 查询语言   │ JDOQL、SQL              │ JPQL、Criteria API │
  ├────────────┼─────────────────────────┼────────────────────┤
  │ 主键策略   │ Application/Datastore   │ 仅 Application     │
  ├────────────┼─────────────────────────┼────────────────────┤
  │ 字节码增强 │ 必须                    │ 可选（某些实现）   │
  ├────────────┼─────────────────────────┼────────────────────┤
  │ 规范维护   │ Apache JDO              │ Jakarta EE         │
  ├────────────┼─────────────────────────┼────────────────────┤
  │ 流行度     │ 较小众                  │ 主流               │
  └────────────┴─────────────────────────┴────────────────────┘

  希望这个详细的讲解能帮你理解 JDO DataNucleus！有任何疑问随时问我 😊

✻ Cooked for 1m 12s

❯ 解释一下PersistenceManagerFactory和JDOHelper.getPersistenceManagerFactory的功能和用法

⏺ 好的！让我详细解释 PersistenceManagerFactory 和 JDOHelper.getPersistenceManagerFactory 的功能和用法。

  📦 一、PersistenceManagerFactory 是什么？

  1.1 核心概念

  PersistenceManagerFactory (PMF) 是 JDO 中最核心的工厂类，类似于 JPA 中的 EntityManagerFactory。

  定位：
  Application
      ↓
  PersistenceManagerFactory (PMF)  ← 工厂，创建 PM
      ↓
  PersistenceManager (PM)          ← 工作单元，执行 CRUD
      ↓
  Database

  核心特点：
  - ✅ 线程安全 - 可以在多线程环境中共享
  - ✅ 重量级对象 - 创建成本高，应该复用
  - ✅ 单例模式 - 整个应用只需要一个实例
  - ✅ 连接池管理 - 内部管理数据库连接池
  - ✅ 元数据缓存 - 缓存实体类的映射信息

  1.2 生命周期

  // 应用启动时创建（只创建一次）
  PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory(...);

  // 应用运行期间反复使用
  PersistenceManager pm1 = pmf.getPersistenceManager();
  PersistenceManager pm2 = pmf.getPersistenceManager();
  PersistenceManager pm3 = pmf.getPersistenceManager();
  // ...

  // 应用关闭时销毁
  pmf.close();

  ---
  🔧 二、JDOHelper.getPersistenceManagerFactory() 用法

  JDOHelper 是 JDO 的辅助工具类，提供了多种创建 PMF 的方式。

  2.1 方式一：从 persistence.xml 加载（最常用）

  // 通过持久化单元名称加载
  PersistenceManagerFactory pmf =
      JDOHelper.getPersistenceManagerFactory("MyPersistenceUnit");

  对应的 persistence.xml：
  <persistence xmlns="http://xmlns.jcp.org/xml/ns/persistence" version="2.2">
      <persistence-unit name="MyPersistenceUnit">
          <provider>org.datanucleus.api.jdo.JDOPersistenceManagerFactory</provider>

          <class>com.example.jdo.User</class>

          <properties>
              <property name="javax.jdo.option.ConnectionURL"
                        value="jdbc:mysql://localhost:3306/jdo_test"/>
              <property name="javax.jdo.option.ConnectionUserName" value="root"/>
              <property name="javax.jdo.option.ConnectionPassword" value="root"/>
              <property name="javax.jdo.option.ConnectionDriverName"
                        value="com.mysql.cj.jdbc.Driver"/>
              <property name="datanucleus.schema.autoCreateAll" value="true"/>
          </properties>
      </persistence-unit>
  </persistence>

  工作原理：
  1. JDOHelper 在 classpath 中查找 META-INF/persistence.xml
  2. 根据 name 参数找到对应的 <persistence-unit>
  3. 读取配置创建 PMF 实例
  4. 缓存 PMF，下次调用返回同一个实例

  2.2 方式二：从 jdoconfig.xml 加载

  // 使用默认配置文件
  PersistenceManagerFactory pmf =
      JDOHelper.getPersistenceManagerFactory();

  对应的 jdoconfig.xml：（路径：META-INF/jdoconfig.xml）
  <?xml version="1.0" encoding="UTF-8"?>
  <jdoconfig xmlns="http://java.sun.com/xml/ns/jdo/jdoconfig"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://java.sun.com/xml/ns/jdo/jdoconfig
             http://java.sun.com/xml/ns/jdo/jdoconfig_3_0.xsd">

      <persistence-manager-factory name="MyPMF">
          <property name="javax.jdo.option.ConnectionURL"
                    value="jdbc:mysql://localhost:3306/jdo_test"/>
          <property name="javax.jdo.option.ConnectionUserName" value="root"/>
          <property name="javax.jdo.option.ConnectionPassword" value="root"/>
          <property name="javax.jdo.option.ConnectionDriverName"
                    value="com.mysql.cj.jdbc.Driver"/>
          <property name="datanucleus.schema.autoCreateAll" value="true"/>
      </persistence-manager-factory>
  </jdoconfig>

  // 指定 PMF 名称
  PersistenceManagerFactory pmf =
      JDOHelper.getPersistenceManagerFactory("MyPMF");

  2.3 方式三：从 Properties 对象创建

  Properties props = new Properties();
  props.setProperty("javax.jdo.option.ConnectionURL",
                    "jdbc:mysql://localhost:3306/jdo_test");
  props.setProperty("javax.jdo.option.ConnectionUserName", "root");
  props.setProperty("javax.jdo.option.ConnectionPassword", "root");
  props.setProperty("javax.jdo.option.ConnectionDriverName",
                    "com.mysql.cj.jdbc.Driver");
  props.setProperty("javax.jdo.PersistenceManagerFactoryClass",
                    "org.datanucleus.api.jdo.JDOPersistenceManagerFactory");
  props.setProperty("datanucleus.schema.autoCreateAll", "true");

  PersistenceManagerFactory pmf =
      JDOHelper.getPersistenceManagerFactory(props);

  适用场景：
  - 动态配置（从配置中心读取）
  - 测试环境
  - 需要运行时修改配置

  2.4 方式四：从 Map 创建

  Map<String, Object> props = new HashMap<>();
  props.put("javax.jdo.option.ConnectionURL",
            "jdbc:mysql://localhost:3306/jdo_test");
  props.put("javax.jdo.option.ConnectionUserName", "root");
  props.put("javax.jdo.option.ConnectionPassword", "root");
  props.put("javax.jdo.option.ConnectionDriverName",
            "com.mysql.cj.jdbc.Driver");
  props.put("javax.jdo.PersistenceManagerFactoryClass",
            "org.datanucleus.api.jdo.JDOPersistenceManagerFactory");

  PersistenceManagerFactory pmf =
      JDOHelper.getPersistenceManagerFactory(props);

  2.5 方式五：从 jdo.properties 文件加载

  文件路径： META-INF/jdo.properties

  javax.jdo.option.ConnectionURL=jdbc:mysql://localhost:3306/jdo_test
  javax.jdo.option.ConnectionUserName=root
  javax.jdo.option.ConnectionPassword=root
  javax.jdo.option.ConnectionDriverName=com.mysql.cj.jdbc.Driver
  javax.jdo.PersistenceManagerFactoryClass=org.datanucleus.api.jdo.JDOPersistenceManagerFactory
  datanucleus.schema.autoCreateAll=true

  // 自动加载 jdo.properties
  PersistenceManagerFactory pmf =
      JDOHelper.getPersistenceManagerFactory();

  ---
  🎯 三、PersistenceManagerFactory 核心功能

  3.1 创建 PersistenceManager

  // 最常用的方法
  PersistenceManager pm = pmf.getPersistenceManager();

  try {
      // 执行数据库操作
  } finally {
      pm.close();  // 必须关闭
  }

  3.2 获取和修改配置

  // 获取数据源信息
  DataStoreCache cache = pmf.getDataStoreCache();

  // 获取查询缓存
  JDOQLTypedQuery<User> query = pmf.newJDOQLTypedQuery(User.class);

  // 获取连接URL（只读）
  String url = (String) pmf.getProperties()
      .get("javax.jdo.option.ConnectionURL");

  3.3 管理二级缓存

  // 获取数据存储缓存
  DataStoreCache dsCache = pmf.getDataStoreCache();

  // 清空缓存
  dsCache.evictAll();

  // 清空特定类的缓存
  dsCache.evictAll(User.class);

  // 清空特定对象的缓存
  dsCache.evict(userId);

  3.4 检查状态和关闭

  // 检查是否已关闭
  boolean closed = pmf.isClosed();

  // 获取持久化单元名称
  String name = pmf.getPersistenceUnitName();

  // 关闭工厂（释放资源）
  pmf.close();

  ---
  📝 四、完整示例：单例模式管理 PMF

  4.1 简单单例（推荐用于小项目）

  package com.example.jdo;

  import javax.jdo.JDOHelper;
  import javax.jdo.PersistenceManagerFactory;

  public class PMFSingleton {

      private static final PersistenceManagerFactory INSTANCE;

      static {
          // 应用启动时初始化
          INSTANCE = JDOHelper.getPersistenceManagerFactory("MyPersistenceUnit");

          // 注册关闭钩子
          Runtime.getRuntime().addShutdownHook(new Thread(() -> {
              if (!INSTANCE.isClosed()) {
                  INSTANCE.close();
                  System.out.println("PersistenceManagerFactory 已关闭");
              }
          }));
      }

      private PMFSingleton() {
          // 私有构造函数
      }

      public static PersistenceManagerFactory getInstance() {
          return INSTANCE;
      }
  }

  使用方式：
  public class UserDAO {
      private PersistenceManagerFactory pmf = PMFSingleton.getInstance();

      public void insertUser(User user) {
          PersistenceManager pm = pmf.getPersistenceManager();
          try {
              // ...
          } finally {
              pm.close();
          }
      }
  }

  4.2 懒加载单例（推荐用于大项目）

  public class PMFManager {

      private static volatile PersistenceManagerFactory instance;
      private static final Object lock = new Object();

      private PMFManager() {}

      public static PersistenceManagerFactory getInstance() {
          if (instance == null) {
              synchronized (lock) {
                  if (instance == null) {
                      instance = JDOHelper.getPersistenceManagerFactory(
                          "MyPersistenceUnit"
                      );

                      // 注册关闭钩子
                      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                          shutdown();
                      }));
                  }
              }
          }
          return instance;
      }

      public static void shutdown() {
          if (instance != null && !instance.isClosed()) {
              instance.close();
              System.out.println("PMF 已关闭");
          }
      }
  }

  4.3 支持多数据源的 PMF 管理器

  public class PMFRegistry {

      private static final Map<String, PersistenceManagerFactory> registry
          = new ConcurrentHashMap<>();

      public static PersistenceManagerFactory get(String unitName) {
          return registry.computeIfAbsent(unitName,
              JDOHelper::getPersistenceManagerFactory);
      }

      public static void closeAll() {
          registry.values().forEach(pmf -> {
              if (!pmf.isClosed()) {
                  pmf.close();
              }
          });
          registry.clear();
      }
  }

  使用方式：
  // 主数据库
  PersistenceManagerFactory mainPMF = PMFRegistry.get("MainDB");

  // 备份数据库
  PersistenceManagerFactory backupPMF = PMFRegistry.get("BackupDB");

  ---
  ⚙️ 五、重要配置属性

  5.1 必需属性

  # 数据库连接
  javax.jdo.option.ConnectionURL=jdbc:mysql://localhost:3306/mydb
  javax.jdo.option.ConnectionUserName=root
  javax.jdo.option.ConnectionPassword=password
  javax.jdo.option.ConnectionDriverName=com.mysql.cj.jdbc.Driver

  # PMF 实现类
  javax.jdo.PersistenceManagerFactoryClass=org.datanucleus.api.jdo.JDOPersistenceManagerFactory

  5.2 常用可选属性

  # 自动建表
  datanucleus.schema.autoCreateAll=true

  # 连接池类型（None/DBCP/C3P0/HikariCP/Tomcat）
  datanucleus.ConnectionPoolingType=DBCP

  # 连接池大小
  datanucleus.connectionPool.maxPoolSize=10
  datanucleus.connectionPool.minPoolSize=2

  # 是否忽略缓存
  datanucleus.IgnoreCache=false

  # 事务类型（JTA/RESOURCE_LOCAL）
  javax.jdo.option.TransactionType=RESOURCE_LOCAL

  # 只读模式
  javax.jdo.option.ReadOnly=false

  # 多线程模式
  javax.jdo.option.Multithreaded=true

  # 标识符命名策略
  datanucleus.identifier.case=LowerCase

  # 显示SQL
  datanucleus.GenerateSQL.Debug=true

  ---
  ⚠️ 六、注意事项和最佳实践

  6.1 ✅ DO - 应该做的

  1. 单例模式管理 PMF

  // ✅ 正确：整个应用共享一个 PMF
  private static final PersistenceManagerFactory pmf =
      JDOHelper.getPersistenceManagerFactory("MyPersistenceUnit");

  2. 应用关闭时释放资源

  // ✅ 正确：注册关闭钩子
  Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      pmf.close();
  }));

  3. 检查 PMF 状态

  // ✅ 正确：使用前检查
  if (!pmf.isClosed()) {
      PersistenceManager pm = pmf.getPersistenceManager();
      // ...
  }

  6.2 ❌ DON'T - 不应该做的

  1. 重复创建 PMF

  // ❌ 错误：每次都创建新的 PMF（性能杀手！）
  public void insertUser(User user) {
      PersistenceManagerFactory pmf =
          JDOHelper.getPersistenceManagerFactory("MyPersistenceUnit");
      PersistenceManager pm = pmf.getPersistenceManager();
      // ...
  }

  问题：
  - PMF 创建成本极高（解析元数据、建立连接池）
  - 导致内存泄漏
  - 严重影响性能

  2. 不关闭 PMF

  // ❌ 错误：应用关闭时不释放 PMF
  // 导致连接池未关闭、资源泄漏

  3. 多线程共享 PersistenceManager

  // ❌ 错误：PM 不是线程安全的
  private PersistenceManager pm = pmf.getPersistenceManager();

  public void method1() {
      pm.makePersistent(user1);  // 线程1
  }

  public void method2() {
      pm.makePersistent(user2);  // 线程2 - 冲突！
  }

  正确做法：
  // ✅ 正确：每个操作创建新的 PM
  public void insertUser(User user) {
      PersistenceManager pm = pmf.getPersistenceManager();
      try {
          // ...
      } finally {
          pm.close();
      }
  }

  ---
  🔍 七、PMF vs PM 对比

  ┌──────────┬───────────────────────────┬─────────────────────┐
  │   特性   │ PersistenceManagerFactory │ PersistenceManager  │
  ├──────────┼───────────────────────────┼─────────────────────┤
  │ 作用     │ 工厂，创建 PM             │ 工作单元，执行 CRUD │
  ├──────────┼───────────────────────────┼─────────────────────┤
  │ 线程安全 │ ✅ 是                     │ ❌ 否               │
  ├──────────┼───────────────────────────┼─────────────────────┤
  │ 生命周期 │ 整个应用                  │ 单次操作            │
  ├──────────┼───────────────────────────┼─────────────────────┤
  │ 创建成本 │ 高（重量级）              │ 低（轻量级）        │
  ├──────────┼───────────────────────────┼─────────────────────┤
  │ 共享方式 │ 全局单例                  │ 每次新建            │
  ├──────────┼───────────────────────────┼─────────────────────┤
  │ 关闭时机 │ 应用退出                  │ 操作完成            │
  └──────────┴───────────────────────────┴─────────────────────┘

  ---
  📊 八、实际应用架构示例

  8.1 典型三层架构

  // 1. 持久化层（单例 PMF）
  public class PersistenceContext {
      private static final PersistenceManagerFactory PMF =
          JDOHelper.getPersistenceManagerFactory("MyPersistenceUnit");

      public static PersistenceManagerFactory getPMF() {
          return PMF;
      }
  }

  // 2. DAO 层
  public class UserDAO {
      private PersistenceManagerFactory pmf = PersistenceContext.getPMF();

      public User findById(Long id) {
          PersistenceManager pm = pmf.getPersistenceManager();
          try {
              return pm.getObjectById(User.class, id);
          } finally {
              pm.close();
          }
      }
  }

  // 3. Service 层
  public class UserService {
      private UserDAO userDAO = new UserDAO();

      public User getUser(Long id) {
          return userDAO.findById(id);
      }
  }

  8.2 Spring 集成示例

  @Configuration
  public class JDOConfig {

      @Bean(destroyMethod = "close")
      public PersistenceManagerFactory persistenceManagerFactory() {
          return JDOHelper.getPersistenceManagerFactory("MyPersistenceUnit");
      }

      @Bean
      @Scope("prototype")  // 每次注入创建新的 PM
      public PersistenceManager persistenceManager(
              PersistenceManagerFactory pmf) {
          return pmf.getPersistenceManager();
      }
  }

  ---
  🎓 九、总结

  核心要点：

  1. PersistenceManagerFactory
    - 工厂类，负责创建 PersistenceManager
    - 线程安全，应该全局共享（单例）
    - 重量级对象，创建成本高
    - 管理连接池、缓存、元数据
  2. JDOHelper.getPersistenceManagerFactory()
    - 提供多种方式创建 PMF
    - 推荐使用 persistence.xml 配置
    - 内部会缓存 PMF 实例
    - 支持动态配置
  3. 最佳实践
    - ✅ 使用单例模式管理 PMF
    - ✅ 应用启动时创建，关闭时销毁
    - ✅ PM 每次操作创建，用完立即关闭
    - ❌ 不要重复创建 PMF
    - ❌ 不要多线程共享 PM
```
