## 扩展模块

此模块为 crane4j 结合第三方库提供的扩展功能，不同的包下的功能彼此独立。在已经引入 `crane4j-extension` 模块的前提下，用户需要自行引入对应的第三方库即可用使用对应的包下的扩展功能。

### **cn.crane4j.extension.spring**

用于提供将 crane4j 集成至 spring 框架，并且基于 spring 提供包括 SpEL 表达式引擎、自动填充切面、组合注解等扩展功能；

使用前需要先引入 `spring-context` 依赖。

### cn.crane4j.extension.mybatis.plus

用于将 mybatis-plus 集成至 crane4j，可以基于 mp 提供快速查询单表作为数据源的功能，

使用前需要先引入 `mybatis-plus-core` 依赖。