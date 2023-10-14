# FAQ

以下是一些常见问题与对应的解决方案。如果当你遇到问题时，可以先尝试在这里寻找解决方案。

## 1、填充不生效？

- 确认 `@AssembleXXX` 注解正确配置了 `container` 与 `prop` 属性；
- 确认操作涉及的属性存在，且都有相应的 `setter` 和 `getter` 方法；
- 确认目标对象对应的 key 属性值不为空；
- 确认指定的数据源容器确实有根据 key 值列表返回非空集合；
- 确认通过 `BeanOperationParser` 解析类后，得到的 `BeanOperations` 中的 `AssembleOperation` 列表中有该 key 属性对应的操作配置；

当确认上述步骤皆无问题后，你可以尝试在源码中 `cn.crane4j.core.executor.handler.AbstractAssembleOperationHandler` 类的 `doProcess` 方法中添加断点：

+ 如果未进入断点，则说明该操作配置未能生效，请重新确认上述原因；
+ 进入 `collectToEntities`：在这一步，确认你填充的对象是否都已经被收集到，且 key 值被正确的获取；
+ 进入 `getSourcesFromContainer`：在这一步，确认通过上述 key 值能够正确的从数据源获取到数据；
+ 进入 `getTheAssociatedSource`：在这一步，确认待填充的对象可以通过 key 值获得相应的数据源对象；
+ 进入 `completeMapping`：在这一步，确认 crane4j 是否按你的配置正确的将数据源对象的属性值映射到待填充的对象上；

如果仍然无法解决，可以在 issues 中或者相关交流群中反馈。

## 2、如何实现嵌套填充？

在需要嵌套填充的属性上添加 `@Disassemble` 注解即可，具体参见 [拆卸嵌套对象](./../operation/3.4.拆卸嵌套对象.md)。

## 3、如何实现级联填充？

- 在需要按顺序执行的属性上添加 `@Order` 注解（Spring 环境），或直接在 `@AssembleXXX` 注解的 `sort` 属性指定排序值，越小越先执行；
- 在指定操作顺序的前提下，使用有序的装配执行器 `OrderedBeanOperationExecutor` 完成对目标的填充操作；

具体参见 [操作排序](./../operation/3.6.操作排序.md)。

## 4、如何处理一对多的情况？

通过 `@AssembleXXX` 注解中的 `handler` 或 `handlerType` 属性指定装配处理器为一对多装配处理器 `OneToManyAssembleOperationHandler` 类型或名称（在 Spring 中即为 bean 名称）即可。

具体参见 [指定装配处理器](./../operation/3.3.指定装配处理器.md) 中一对多装配一节。

## 5、键字段可以是按分隔符拼接的字符串吗？

通过 `@AssembleXXX` 注解中的 `handler` 或 `handlerType` 属性指定装配处理器为一对多装配处理器 `ManyToManyAssembleOperationHandler` 的类型或名称（在 Spring 中即为 bean 名称）即可。

具体参见 [指定装配处理器](./../operation/3.3.指定装配处理器.md) 中多对多装配一节。

## 6、键字段可以是集合或者数组吗？

同上，通过 `@AssembleXXX` 注解中的 `handler` 或 `handlerType` 属性指定装配处理器为一对多装配处理器 `ManyToManyAssembleOperationHandler` 的类型或名称（在 Spring 中即为 bean 名称）即可。

具体参见 [指定装配处理器](./../operation/3.3.指定装配处理器.md) 中多对多装配一节。

## 7、为什么使用异步执行器的时候报错？

默认情况下，并没有注册异步操作执行器 `AsyncBeanOperationExecutor`，用户需要自行创建后再将其注册到全局配置中。

具体参见 [操作执行器](./../execute/4.3.操作执行器.md) 一节。

## 8、怎么刷新容器的数据 ？

- 如果容器是 `ConstantContainer` ，直接通过 `get` 方法获取缓存的 `Map` 集合后直接修改即可；
- 获取 `Crane4jGlobalConfiguration` 或 `ContainerManager` 后，通过 `registerContainer` 使用命名空间相同的容器对旧容器进行覆盖；

## 9、怎么忽略掉某些字段不进行填充？

- 使用 `@AssembleXXX` 注解的 `groups` 属性对指定操作进行分组；
- 在配置了分组的前提下，在使用 `OperateTemplate` 手动填充，或通过被 `@AutoOperate` 注解的方法进行自动填充时，指定仅执行/仅不执行特定分组的操作；

具体参见 [操作分组](./../operation/3.5.操作分组.md) 一节。

## 10、为什么 `@ContainerMethod` 注解不生效？

如果是非 Spring 环境，则需要手动的通过 `ContainerMethodAnnotationProcessor` 扫描指定类并向全局配置注册扫描获取的方法容器。

如果是 Spring 环境，请确保：

- 容器中存在 `BeanMethodContainerRegistrar` 后处理器；
- 被注解的方法所在类被 Spring 扫描，且容器中存在对应的 bean；
- 被注解的方法所在类在 `BeanMethodContainerRegistrar` 后处理器初始化后才加载；

## 11、为什么 `@AutoOperate` 注解不生效？

如果是非 Spring 环境，则需要手动的通过 `MethodArgumentAutoOperateSupport` 和 `MethodResultAutoOperateSupport` 拦截方法调用。

如果是 Spring 环境，请确保：

- 开启了 `SpringAOP` 功能；
- 容器中存在 `MethodResultAutoOperateAdvisor` 或 `MethodArgumentAutoOperateAdvisor` 通知器；
- 被注解的方法所在类被 Spring 扫描、容器中存在对应的 bean 且被 Spring 代理；

## 12、为什么引了 guava 和 hutool ？

不想要重复造轮子，有些组件直接使用成熟的开源库比自己再写一套更可靠。

此外，`crane4j` 仅在有限的地方使用了这些工具类库：

- `guava` ：使用了缓存组件 `Cache` 与用于构造 `WeakConcurrentMap` 的  `MapMaker`；
- `hutool`：使用了类型转换取组件 `Convert`，如果没用到 `HutoolConverterManager` 可以在依赖中排除；

## 13、支持 jdk9+ / springboot3 吗？

在 `jdk11` 与 `jdk17` 和相应版本 SpringBoot 中测试后可以正常运行。

## 14、容器可以做一些自定义的初始化/销毁操作吗？

实现 `Container.Lifecycle` 接口即可，具体参见 [容器的生命周期回调](./../advance/5.3.容器的生命周期回调.md)。

## 15、可以支持同时根据多个 key 字段填充数据吗？

可以，不过实现方式有点特殊，具体参照 [对象容器](./../container/2.9.对象容器.md)。