## 1.0.0 (2023-03-23)

这是 crane4j 的第一个正式版本，如果遇到问题可以在群内或 issues 中反馈，作者会尽快响应。

具体内容参见：[Milestone](https://github.com/opengoofy/crane4j/milestone/1)。

**Feature**

- [提供基于 Guava 的 LoadingCache 的缓存支持](https://github.com/opengoofy/crane4j/issues/24)
- [添加默认的组合注解扩展包](https://github.com/opengoofy/crane4j/issues/20)；
- [支持使用Spring的@Order注解对装配操作排序](https://github.com/opengoofy/crane4j/issues/17)；
- [支持在注解中通过 beanName 引用 Spring 上下文中的组件](https://github.com/opengoofy/crane4j/issues/14)；
- [添加扩展模块，支持基于 MybatisPlus 自动生成表查询数据源](https://github.com/opengoofy/crane4j/issues/8)；

**Refactor**

- [重构装配处理器，并统一为所有装配操作提供一对一、一对多、多对多映射支持](https://github.com/opengoofy/crane4j/issues/25)；
- [重构并完善缓存功能](https://github.com/opengoofy/crane4j/issues/23)；
- [添加容器工厂组件以隔离和丰富获取数据源容器的渠道](https://github.com/opengoofy/crane4j/issues/4)；
- [将注解分离至独立的 crane4j-annotation 模块](https://github.com/opengoofy/crane4j/issues/10)；

**Test**

- [补充 MultiKeyAssembleOperationHandler 的测试用例](https://github.com/opengoofy/crane4j/issues/1);
- [添加示例项目](https://github.com/opengoofy/crane4j/issues/15)；

**docs**

- [添加官方站点配置](https://github.com/opengoofy/crane4j/issues/18)；
- [代码注释国际化](https://github.com/opengoofy/crane4j/issues/13)；
- [提供 crane4j 的官方文档站点](https://github.com/opengoofy/crane4j/issues/2)；

**chore**

- [重命名 groupId 为 cn.crane4j](https://github.com/opengoofy/crane4j/issues/9)；
- [添加自动化 CI 流程](https://github.com/opengoofy/crane4j/issues/12)；

## 1.1.0 (2023-03-30)

这一个重构与增强版本，增强了一些新功能，并且调整了一部分 api 的使用方式。

具体内容参见：[Milestone](https://github.com/opengoofy/crane4j/milestone/2)。

**Feature**

- [core模块应该默认支持ognl表达式](https://github.com/opengoofy/crane4j/issues/24)；
- [简化`@Mapping`配置，可以在一个属性同时配置src和ref](https://github.com/opengoofy/crane4j/issues/30)；
- [字段映射支持以链式操作符获取或设置多级嵌套对象的属性](https://github.com/opengoofy/crane4j/issues/27)；
- [提供基于 Guava 的 LoadingCache 的缓存支持](https://github.com/opengoofy/crane4j/issues/24)；
- [支持通过 `@ContainerConstant` 注解的配置，反转基于常量类构建的容器键值](https://github.com/opengoofy/crane4j/issues/33)

**Refactor**

- [重构并完善缓存功能](https://github.com/opengoofy/crane4j/issues/23)；
- [将starter模块中的部分组件功能分离为核心模块中的独立组件](https://github.com/opengoofy/crane4j/issues/28)

**docs**

- [文档重构](https://github.com/opengoofy/crane4j/issues/31)；

## 1.2.0 (2023-04-09)

这一个重构版本，增强了一些新功能，并且调整了 MybatisPlus 扩展的引入方式，如果已经使用了 MybatisPlus 插件，则需要按文档重新引入。

具体内容参见：[Milestone](https://github.com/opengoofy/crane4j/milestone/3)。

**Feature**

- [支持直接填充 Map 类型的数据](https://github.com/opengoofy/crane4j/issues/21)；
- [提供 `@AssembleMp` 注解，为基于 MP 的数据源提供更好的支持](https://github.com/opengoofy/crane4j/issues/36)；
- [允许自定义注解，支持解析自定义的配置规则](https://github.com/opengoofy/crane4j/issues/22)；
- [MpBaseMapperContainerRegister支持懒加载](https://github.com/opengoofy/crane4j/issues/37)；

**Refactor**

- [将 `@Assemble` 和 `@Disassemble` 的解析逻辑分离到独立的注解处理器](https://github.com/opengoofy/crane4j/issues/39)；
- [重构项目结构，提供不同环境下的最小依赖](https://github.com/opengoofy/crane4j/issues/28)；

**Fix**

- [当Bean被spring代理时，调用方法数据源容器报错](https://github.com/opengoofy/crane4j/issues/38)；

## 1.3.0-ALPHA (2023-05-10)

这是 `1.3.0` 的预览版本，重构和增强了一些已有功能，并添加了一些新的功能。

其中，基于新特性[添加基于接口代理的填充方法](https://github.com/opengoofy/crane4j/issues/44)，crane4j 将更好的支持处理 `JSONObject` 或 `Map` 类型的非 `JavaBean`  对象。

具体内容参见：[Milestone](https://github.com/opengoofy/crane4j/milestone/4)。

**Feature**

- [提供 `@AssembleEnum` 注解，对枚举类型数据源提供更好的支持](https://github.com/opengoofy/crane4j/issues/35)；
- [提供一个不基于 ThreadLocal 的动态数据源容器提供者](https://github.com/opengoofy/crane4j/issues/61)；
- [代理填充方法支持设置临时容器](https://github.com/opengoofy/crane4j/issues/49)；
- [OperatorProxyFactory中代理方法的生成也应当支持多种策略](https://github.com/opengoofy/crane4j/issues/55)；
- [支持通过 Spring 依赖注入获取被 `@Operator` 注解的接口的代理对象](https://github.com/opengoofy/crane4j/issues/48)；
- [装配操作中的容器支持懒加载](https://github.com/opengoofy/crane4j/issues/50)；
- [添加基于接口代理的填充方法](https://github.com/opengoofy/crane4j/issues/44)；
- [提供一个默认的可配置容器注册者实现](https://github.com/opengoofy/crane4j/issues/41)；

**Refactor**

- [减少对 Hutool 的依赖](https://github.com/opengoofy/crane4j/issues/70)；
- [移除 OperationAnnotationResolver 级别的配置缓存](https://github.com/opengoofy/crane4j/issues/59)；
- [配置解析器应当支持所有的AnnotatedElement类型对象的注解配置](https://github.com/opengoofy/crane4j/issues/45)；

**Test**

- [提高测试覆盖率](https://github.com/opengoofy/crane4j/issues/67)；
- [MybatisPlus相关扩展的测试用例数据库更换为H2](https://github.com/opengoofy/crane4j/issues/72)；
- 修复了引入 `crane4j-spring-boot-starter` 时的一些自动装配问题；

**Doc**

- [补充文档，如何在非 Spring 环境和 Spring 环境使用 crane4j](https://github.com/opengoofy/crane4j/issues/34)；

## 2.0.0-ALPHA (2023-07-08)

这是 `2.0.0` 的预览版本，基于 `1.3.0-ALPHA` 升级而来。

项目进行了一次范围较大的重构，优化了大量的代码与逻辑，部分 API 可能不向下兼容。

具体内容参见：[Milestone](https://github.com/opengoofy/crane4j/milestone/4)。

**Feature**

- [优化 `@ContainerMethod` 注解在类上的配置方式](https://github.com/opengoofy/crane4j/issues/97)；
- [添加一个全局的排序器静态单例，用于同时支持 Spring 的 `@Order` 与 Crane4j 的 `Sorted` 接口](https://github.com/opengoofy/crane4j/issues/84)；
- [`ConstantContainer` 支持刷新缓存内容](https://github.com/opengoofy/crane4j/issues/76)；
- [代理填充方法支持设置临时容器](https://github.com/opengoofy/crane4j/issues/49)；
- [提供一个默认的可配置容器注册者实现](https://github.com/opengoofy/crane4j/issues/41)；

**Refactor**

- [2.0 升级重构计划](https://github.com/opengoofy/crane4j/issues/86)；
- [重构容器与装配操作配置的绑定过程](https://github.com/opengoofy/crane4j/issues/85)；
- [重构全局配置类的容器管理功能](https://github.com/opengoofy/crane4j/issues/81)；
- [装配操作可以指定数据源容器的加载策略](https://github.com/opengoofy/crane4j/issues/63)；
- [配置解析器应当支持所有的AnnotatedElement类型对象的注解配置](https://github.com/opengoofy/crane4j/issues/45)；
- 移除了 `AbstractCacheablePropertyOperator` ，缓存功能分离至独立的 `CacheablePropertyOperator` 装饰器；

**Doc**

- [补充文档，如何在非 Spring 环境和 Spring 环境使用 crane4j](https://github.com/opengoofy/crane4j/issues/34)；

## 2.0.0-BATE (2023-07-30)

这是 `2.0.0` 的第二个预览版本，在上一版本的基础上修复了一些 bug，并添加了一些新功能。

具体内容参见：[Milestone](https://github.com/opengoofy/crane4j/milestone/4)。

**Fix**

- [为基于 `ConstantContainer` 的枚举、常量容器提供独立的`Builder`](https://github.com/opengoofy/crane4j/issues/77)；
- [启用全局切面后，springboot 项目启动报错](https://github.com/opengoofy/crane4j/issues/113)；
- [在方法添加 `@AutoOperate` 后，若方法不指定填充对象类型，且返回值为空时报错](https://github.com/opengoofy/crane4j/issues/114)；

**Refactor**

- [在各个组件中由于参数/逻辑校验不通过而抛出异常时，异常需要更详细的信息](https://github.com/opengoofy/crane4j/issues/116)；
- [一些关键操作通过 debug 级别的日志输出执行时间](https://github.com/opengoofy/crane4j/issues/107)；

**Feature**

- [当在注解中不指定 key 属性时，允许将当前对象作为 key 值](https://github.com/opengoofy/crane4j/issues/100)；
- [`ReflectivePropertyOperator` 在没有找到 setter 或者 getter 方法时，应当支持直接基于属性进行读写](https://github.com/opengoofy/crane4j/issues/105)；

## 2.0.0 (2023-08-06)

这是 `2.0.0` 的正式版本，相对最近的一个正式版本 `1.2.0` 做了大幅度的重构，优化了很多旧功能，并且添加了较多新特性。

具体内容参见：[Milestone](https://github.com/opengoofy/crane4j/milestone/4)。

**Feature**

- [优化 `@ContainerMethod` 注解在类上的配置方式](https://github.com/opengoofy/crane4j/issues/97)；
- [添加一个全局的排序器静态单例，用于同时支持 Spring 的 `@Order` 与 Crane4j 的 `Sorted` 接口](https://github.com/opengoofy/crane4j/issues/84)；
- [`ConstantContainer` 支持刷新缓存内容](https://github.com/opengoofy/crane4j/issues/76)；
- [代理填充方法支持设置临时容器](https://github.com/opengoofy/crane4j/issues/49)；
- [提供一个默认的可配置容器注册者实现](https://github.com/opengoofy/crane4j/issues/41)；
- [当在注解中不指定 key 属性时，允许将当前对象作为 key 值](https://github.com/opengoofy/crane4j/issues/100)；
- [`ReflectivePropertyOperator` 在没有找到 setter 或者 getter 方法时，应当支持直接基于属性进行读写](https://github.com/opengoofy/crane4j/issues/105)；
- [支持“字典类型-字典项编码”模式的字典项填充](https://github.com/opengoofy/crane4j/issues/122)；

**Refactor**

- [2.0 升级重构计划](https://github.com/opengoofy/crane4j/issues/86)；
- [重构容器与装配操作配置的绑定过程](https://github.com/opengoofy/crane4j/issues/85)；
- [重构全局配置类的容器管理功能](https://github.com/opengoofy/crane4j/issues/81)；
- [装配操作可以指定数据源容器的加载策略](https://github.com/opengoofy/crane4j/issues/63)；
- [配置解析器应当支持所有的AnnotatedElement类型对象的注解配置](https://github.com/opengoofy/crane4j/issues/45)；
- 移除了 `AbstractCacheablePropertyOperator` ，缓存功能分离至独立的 `CacheablePropertyOperator` 装饰器；
- [在各个组件中由于参数/逻辑校验不通过而抛出异常时，异常需要更详细的信息](https://github.com/opengoofy/crane4j/issues/116)；
- [一些关键操作通过 debug 级别的日志输出执行时间](https://github.com/opengoofy/crane4j/issues/107)；

**Fix**

- [为基于 `ConstantContainer` 的枚举、常量容器提供独立的`Builder`](https://github.com/opengoofy/crane4j/issues/77)；
- [启用全局切面后，springboot 项目启动报错](https://github.com/opengoofy/crane4j/issues/113)；
- [在方法添加 `@AutoOperate` 后，若方法不指定填充对象类型，且返回值为空时报错](https://github.com/opengoofy/crane4j/issues/114)；
- [spring环境下，`ContainerProvider` 没有在启动后自动注册到 `Crane4jApplicationContext` 中](https://github.com/opengoofy/crane4j/issues/124)；

**Doc**

- [补充文档，如何在非 Spring 环境和 Spring 环境使用 crane4j](https://github.com/opengoofy/crane4j/issues/34)；

## 2.1.0 (2023-08-21)

这是一个正常迭代版本，相对上一版本，修复了一些问题，并调整了关于枚举和常量扫描相关的功能。

具体内容参见：[Milestone](https://github.com/opengoofy/crane4j/milestone/5)。

**Feature**

- [属性映射应当支持使用 null 覆盖已有的属性值](https://github.com/opengoofy/crane4j/issues/127)；
- [支持在启动类上的注解中扫描注解、常量类路径](https://github.com/opengoofy/crane4j/issues/58)；
- [可以选择将容器注册到哪些提供者](https://github.com/opengoofy/crane4j/issues/42)；
- [支持“字典类型-字典项编码”模式的字典项填充](https://github.com/opengoofy/crane4j/issues/122)；

**Fix**

- [`Crane4jInitializer` 启动顺序太靠后，导致在项目中 `ApplicationRunner` 启动时容器仍然未加载](https://github.com/opengoofy/crane4j/issues/142)；
- [`@AssembleEnum` 在不指定 `enumKey` 或者 `enumValue` 时会空指针](https://github.com/opengoofy/crane4j/issues/136)；
- [在启动类添加 `@EnableCrane4j` 注解后，启动应用报错 “No ServletContext set”](https://github.com/opengoofy/crane4j/issues/126)；

**Refactor**

+ [`@AssembleEnum` 应该默认遵循该枚举类上的 `@ContainerEnum` 的配置](https://github.com/opengoofy/crane4j/issues/137)；

**Doc**

- [优化 README 中的快速开始](https://github.com/opengoofy/crane4j/issues/134)；

## 2.2.0 (2023-09-25)

这是一个正常迭代版本，主要修复了一些问题，并添加了少量新功能。

具体内容参见：[Milestone](https://github.com/opengoofy/crane4j/milestone/6)。

**Feature**

- [为 `PropertyOperator` 提供基于方法句柄 `MethodHandler` 的实现](https://github.com/opengoofy/crane4j/issues/132)；
- [支持 `@AssembleMethod` 注解，用于快速声明一个基于方法容器的装配操作](https://github.com/opengoofy/crane4j/issues/96)；
- [添加 jackson 插件，在序列化和反序列化过程中填充 json 数据](https://github.com/opengoofy/crane4j/issues/6)；
- [枚举容器和方法容器支持设置重复的 key 值](https://gitee.com/opengoofy/crane4j/issues/I832VQ)；

**Fix**

- [并发环境下获取容器可能导致类转换异常 “cn.crane4j.core.container.MethodInvokerContainer cannot be cast to cn.crane4j.core.container.ContainerDefinition”](https://github.com/opengoofy/crane4j/issues/162)；
- [`@Assemble` 注解应当支持在类上声明，且支持重复声明](https://gitee.com/opengoofy/crane4j/issues/I82R6E)；
- [AsyncBeanOperationExecutor 避免重复请求相同的数据源](https://gitee.com/opengoofy/crane4j/issues/I82EBG)；
- [在不同类的同名方法上添加 `@AutoOperate` 后，会导致填充字段错乱](https://gitee.com/opengoofy/crane4j/issues/I82EAC)；
- [升级2.1.0版本后，警告“Unable to find property mapping strategy [], use default strategy [OverwriteNotNullMappingStrategy]”](https://gitee.com/opengoofy/crane4j/issues/I7X36D)；

**Refactor**

+ [添加容器适配器管理器，并重构 `DynamicContainerOperatorProxyMethodFactory` 中适配器部分代码](https://github.com/opengoofy/crane4j/issues/149)；
+ [优化 `PropertyMappingStrategy` 的管理方式](https://github.com/opengoofy/crane4j/issues/144)；

## 2.3.0 (2023-10-18)

这是一个正常迭代版本，主要修复了一些问题，并添加了少量新功能。

具体内容参见：[Milestone](https://github.com/opengoofy/crane4j/milestone/7)。

**Feature**

+ [支持直接通过 `Crane4jGlobalConfiguration` 获取 `OperateTemplate`](https://github.com/opengoofy/crane4j/issues/172)；
+ [支持全量获取数量固定的数据源容器](https://github.com/opengoofy/crane4j/issues/119)；
+ [当目标对象的key值与数据源对象的key值类型不一致时，可以指定将目标对象的key值转为对应类型](https://github.com/opengoofy/crane4j/issues/153)；

**Fix**

- [配置了 `crane4j.mybatis-plus.auto-register-mapper = false` 后，在项目启动后依然会自动注册 Mapper 接口](https://github.com/opengoofy/crane4j/issues/168)；

**Refactor**

+ [重构 `AutoOperateAnnotatedElementResolver`，使基于 `@AutoOperate` 的自动装配功能更加灵活](https://github.com/opengoofy/crane4j/issues/166)；

**Doc**

+ [文档优化计划](https://github.com/opengoofy/crane4j/issues/143)；

## 2.3.1 (2023-12-10)

这是一个 Bug 修复版本，请尽快升级。

**Fix**

- [调用Operator实例的Object基本方法会发生死循环直到栈溢出](https://gitee.com/opengoofy/crane4j/issues/I8MZOK)；

## 2.4.0 (2024-01-15)

这是一个功能迭代版本，重构并大幅度增强了缓存功能，并修复了一些问题。

具体内容参见：[Milestone](https://github.com/opengoofy/crane4j/milestone/8)。

**Feature**

- [Operator 接口支持基于 `@AutoOperate` 注解的自动填充](https://github.com/opengoofy/crane4j/issues/185)；
- [支持 redis 缓存](https://github.com/opengoofy/crane4j/issues/3)；
- [`@AssembleEnum` 直接直接内嵌一个 `@ContainerEnum` 注解用于配置](https://github.com/opengoofy/crane4j/issues/176)；
- [将启用框架的方式从注解改为 SPI](https://gitee.com/opengoofy/crane4j/issues/I8VXTE)；
- [方法缓存希望可以像 Spring 那样，通过注解配置到期时间和刷新](https://gitee.com/opengoofy/crane4j/issues/I8UZSO)；
- [基于 `@ContainerMethod` 的方法容器，希望返回值可以支持 `String` 或基础数据类型及其包装类](https://gitee.com/opengoofy/crane4j/issues/I8UZH4)；
- [声明装配操作时是否可以根据条件判断是否应用此次操作？](https://gitee.com/opengoofy/crane4j/issues/I8W0SN)；

## 2.5.0 (2024-02-01)

这是一个功能迭代版本，修复了 crane4j 与 SpringBoot 集成的一些问题，完善了文档与代码注释，并优化了一些功能的使用方式。

具体内容参见：[Milestone](https://github.com/opengoofy/crane4j/milestone/9)。

**Feature**

- [异步执行器支持指定支持指定批量大小，将基于同一数据源的操作分为多个小任务](https://github.com/opengoofy/crane4j/issues/195)；
- [提供回调接口或组件，用于在具体的填充过程支持一些自定的操作](https://github.com/opengoofy/crane4j/issues/79)；
- [当需要值到键的映射时，希望可以不需要在注解中指定 props 属性](https://github.com/opengoofy/crane4j/issues/190)；
- [验证或者支持在更高的 LTS 版本运行](https://github.com/opengoofy/crane4j/issues/11)；
- [支持直接在 @ContainerMethod 注解中指定缓存配置](https://github.com/opengoofy/crane4j/issues/220)；

**Refactor**

- [将MappingType枚举中的 `NONE` 与 `MAPPED` 选项分别替换为 `ORDER_OF_KEYS` 和 `NO_MAPPING`](https://github.com/opengoofy/crane4j/issues/197)；

**Fix**

- [与 SpringBoot 项目集成后，启动出现 No ServletContext set 异常](https://gitee.com/opengoofy/crane4j/issues/I8XRVT)；
- [声明装的配操作支持在实际执行时，根据情况动态决定是否应用](https://gitee.com/opengoofy/crane4j/issues/I8W0SN)；
- [当 Bean 被代理后，类上的 `@ContainerCache` 注解无法被识别](https://github.com/opengoofy/crane4j/issues/199)；

## 2.6.0 (2024-02-23)

这是一个新特性版本，添加了对条件操作的支持，并额外支持了常量和键值两种数据源容器的选项式配置。

具体内容参见：[Milestone](https://github.com/opengoofy/crane4j/milestone/10)。

**Feature**

- [支持 `@AssembleConstant` 注解，用于快速声明一个基于常量类的装配操作](https://github.com/opengoofy/crane4j/issues/135)；
- [支持使用实体类中特定方法返回值作为key值](https://github.com/opengoofy/crane4j/issues/93)；
- [`@ContainerCache` 注解支持在 Spring 配置类的工厂方法中使用](https://github.com/opengoofy/crane4j/issues/210)；
- [声明装的配操作支持在实际执行时，根据情况动态决定是否应用](https://gitee.com/opengoofy/crane4j/issues/I8W0SN)；
- [`@Assemble` 注解支持通过类似 Spring 的 `@Condition` 的方式动态决定是否要对特定对象应用操作](https://github.com/opengoofy/crane4j/issues/133)；
- [支持为每个操作都添加一个id作为唯一标识](https://github.com/opengoofy/crane4j/issues/201)；
- [支持根据策略回填指定字段值](https://github.com/opengoofy/crane4j/issues/130)；

**Refactor**

- [将 `KeyTriggerOperation` 下的实现类的创建方式改为通过 Builder 创建](https://github.com/opengoofy/crane4j/issues/206)；

**Fix**

- [`ContainerLifecycleProcessor` 无法处理被 Spring 管理的容器](https://github.com/opengoofy/crane4j/issues/211)；
- [自动填充时，若目标类没有配置任何装配操作则执行会报错](https://github.com/opengoofy/crane4j/issues/204)；