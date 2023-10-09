# 在 SpringBoot 环境使用

下文 `crane4j` 的版本号 `${last-version}` 即为当前项目最新版本 ![maven-central](https://img.shields.io/github/v/release/Createsequence/crane4j?include_prereleases)

## 1.安装

引入 SpringBoot 相关依赖，然后引入 `crane4j-spring-boot-starter` 即可：

~~~xml
<!-- crane4j 依赖 -->
<dependency>
    <groupId>cn.crane4j</groupId>
    <artifactId>crane4j-spring-boot-starter</artifactId>
    <version>${last-version}</version>
</dependency>

<!-- springboot 依赖 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-autoconfigure</artifactId>
    <version>2.3.5.RELEASE</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <version>2.3.5.RELEASE</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
    <version>2.3.5.RELEASE</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjweaver</artifactId>
    <version>1.9.6</version>
    <scope>test</scope>
</dependency>

<!-- 用于生成构造方法与 getter/setter 方法 -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>${last-version}</version>
</dependency>
~~~

## 2.启用配置

在**启动类**或者**配置类**添加 `@EnableCrane4j` 注解或 `@EnableCrane4jFramework` 注解即可启用自动配置：

~~~java
@EnableCrane4j
@Configuration
public class Crane4jConfiguration {
}
~~~

项目启动后，`crane4j` 相关组件将会注册到 spring 上下文中。

## 3.配置数据源

在开始填充对象之前，你需要提前准备好一些数据源，并将其注册到全局配置对象中。

在 cranej4 中，一个数据源对应一个数据源容器（`Container`），它们通过独一无二的命名空间 （`namespace`）进行区分。

我们可以基于一个 `Map` 集合创建数据源容器，并将其注册到全局配置中：

~~~java
// 从 spring 容器中获取全局配置
@Autowired
private Crane4jGlobalConfiguration configuration;

// 基于 Map 集合创建一个数据源容器
Map<Integer, String> map = new HashMap<>();
map.put(1, "a");
map.put(2, "b");
map.put(3, "c");
Container<Integer> container = Containers.forMap("test", map);

// 将数据源容器注册到全局配置中
configuration.registerContainer(container);
~~~

在后续通过命名空间 `test` 即可引用该数据源容器。

## 4.配置填充操作

接着，我们在需要填充的类属性上添加注解：

~~~java
@Data  // 使用 lombok 生成构造器、getter/setter 方法
@RequiredArgsConstructor
public static class Foo {
     // 根据 id 填充 name
    @Assemble(container = "test", props = @Mapping(ref = "name"))
    private final Integer id;
    private String name;
}
~~~

该配置表示，根据 id 值从容器中获取对应的数据源，并将其填充到 name 属性上。

## 5.触发填充

与非 spring 环境不同，在 spring 环境中，你可以选择手动填充或自动填充：

### 手动填充

~~~java
// 注入填充工具类
@Autowired
private OperateTemplate operateTemplate;

// 手动执行填充
List<Foo> foos = Arrays.asList(new Foo(1), new Foo(2), new Foo(3));
operateTemplate.execute(foos);
System.out.println(foos);
// [Foo(id=1, name="a"), Foo(id=2, name="b"), Foo(id=3, name="c")]
~~~

### 自动填充

~~~java
// 在方法上添加注解，表明需要自动填充其方法返回值
@Component
public static class Service {
    @AutoOperate(type = Foo.class) // 类型为 Foo，返回值可以是 Collection 集合、数值或单个对象
    public List<Foo> getFoos() {
        return Arrays.asList(new Foo(1), new Foo(2), new Foo(3));
    }
}

// 注入 Service，确保 AOP 成功拦截到该注解方法
@Autowired
private Service service;

// 自动填充方法返回值
List<Foo> foos = service.getFoos();
System.out.println(foos);
~~~

## 6.完整代码

~~~java
@EnableCrane4j // 启用配置
@Configuration
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {QuickStartWithSpringBootTest.Service.class})
public class QuickStartWithSpringBootTest {
    
    @Autowired
    private Crane4jGlobalConfiguration configuration;
    @Autowired
    private OperateTemplate operateTemplate;
    @Autowired
    private Service service;

    @Test
    public void run() {
        // 创建并注册数据源容器
        Map<Integer, String> map = new HashMap<>();
        map.put(1, "a");
        map.put(2, "b");
        map.put(3, "c");
        Container<Integer> container = Containers.forMap("test", map);
        configuration.registerContainer(container);

        // 手动填充
        List<Foo> foos = Arrays.asList(new Foo(1), new Foo(2), new Foo(3));
        operateTemplate.execute(foos);
        System.out.println(foos);
		
        // 自动填充
        foos = service.getFoos();
        System.out.println(foos);
    }

    @Component
    public static class Service {
        @AutoOperate(type = Foo.class)
        public List<Foo> getFoos() {
            return Arrays.asList(new Foo(1), new Foo(2), new Foo(3));
        }
    }

    @Data  // 使用 lombok 生成构造器、getter/setter 方法
    @RequiredArgsConstructor
    public static class Foo {
        // 根据 id 填充 name
        @Assemble(container = "test", props = @Mapping(ref = "name"))
        private final Integer id;
        private String name;
    }
}
~~~

