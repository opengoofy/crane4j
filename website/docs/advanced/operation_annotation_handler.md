# 注解处理器

在`crane4j`中，你可以通过各种注解去声明操作配置，比如 `@Assemble`、`@Disassemble`、`@AssembleEnum` 和 `@AssembleMp`，我们称这用于声明操作的注解为**操作注解**。它们底层实际上依赖于解析器中对应的操作注解处理器 `OperationAnnotationHandler` 实现。

`crane4j` 在这部分功能使用了非常典型的责任链模式。在开始解析配置前，我们向配置解析器 `TypeHierarchyBeanOperationParser` 注册一系列注解处理器，而每个处理器都用于处理某个特定的注解。

当我们将一个需要解析的 `AnnotatedElement` 传递给`Parser`时，`Parser` 将创建一个 `BeanOperations` 配置对象，并驱动它在处理器链上流转。每个解析器根据规则将 `AnnotatedElement` 上的特定注解解析为对应的装配或拆卸配置。

下图展示了解析器的工作流程：

![](http://img.xiajibagao.top/%E6%97%A0%E6%A0%87%E9%A2%98-2023-06-04-1303.png)

`crane4j`目前提供了六个内置的操作注解处理器：

| 处理器                              | 注解                | 对应操作类型                    |
| ----------------------------------- | ------------------- | ------------------------------- |
| `DisassembleAnnotationHandler`      | `@Disassemble`      | 拆卸操作 `DisassembleOperation` |
| `AssembleAnnotationHandler`         | `@Assemble`         | 装配操作 `AssembleOperation`    |
| `AssembleEnumAnnotationHandler`     | `@AssembleEnum`     | 装配操作 `AssembleOperation`    |
| `AssembleConstantAnnotationHandler` | `@AssembleConstant` | 装配操作 `AssembleOperation`    |
| `AssembleMethodAnnotationHandler`   | `@AssembleMethod`   | 装配操作 `AssembleOperation`    |
| `AssembleMpAnnotationHandler`       | `@AssembleMp`       | 装配操作 `AssembleOperation`    |

![image-20240222110522868](C:\document\workspace\local\crane4j\website\docs\advanced\image-20240222110522868.png)

你可以实现 `OperationAnnotationHandler` 接口创建自定义的注解处理器，然后将其注册到配置解析器即可：

~~~java
Crane4jGlobalConfiguration configuration = SimpleCrane4jGlobalConfiguration.create();
TypeHierarchyBeanOperationParser parser = configuration.getBeanOperationParser(TypeHierarchyBeanOperationParser.class);
parser.addOperationAnnotationHandler(new CustomeOperationAnnotationHandler());
~~~

如果在 Spring 环境，那么你直接将其交给 Spring 容器管理即可，项目启动后 crane4j 会自动注册。