# 类型转换器

在 `crane4j` 中有不少需要通过反射调用有参方法的场景，包括且不限于：

- 在字段映射时调用的 `setter` 方法；
- 从方法数据源容器调用适配方法以获取数据源；
- 调用操作接口以填充对象的参数；

这些方法底层都依赖于类型转换器管理器  `ConverterManager` 来实现参数的自动转换。换句话说，如果方法的参数类型是 A，而输入的参数类型是 B，`ConverterManager` 将会自动尝试将 B 转换为 A 类型。

`ConverterManager` 目前提供了三套实现：

| 转换器                   | 说明                                                         | 使用情况               |
| ------------------------ | ------------------------------------------------------------ | ---------------------- |
| `SimpleConverterManager` | 直接通过 `(R)t` 这种方式强转，作用有限                       | 基本只在测试用例中使用 |
| `HutoolConverterManager` | 基于 Hutool 的 `Convert` 实现，参见 Hutool 参考文档中的 [自定义类型转换-ConverterRegistry](https://hutool.cn/docs/#/core/类型转换/自定义类型转换-ConverterRegistry?id=自定义类型转换-converterregistry) 一节 | 在非 Spring 环境中使用 |
| `SpringConverterManager` | 基于 Spring 的 `ConversionService` 实现，功能强大            | 在 Spring 环境下使用   |

你也可以实现 `ConverterManager` 接口，
