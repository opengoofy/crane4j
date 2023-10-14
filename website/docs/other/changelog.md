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