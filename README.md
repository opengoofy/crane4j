<img src="https://user-images.githubusercontent.com/49221670/221162632-95465432-f2df-4286-a53a-af59d70b1958.png" alt="image-20230220150040070" style="zoom: 80%;" />

![codecov](https://img.shields.io/badge/license-Apache--2.0-green) [![codecov](https://codecov.io/gh/opengoofy/crane4j/branch/dev/graph/badge.svg?token=CF2Q60Q0VH)](https://codecov.io/gh/opengoofy/crane4j) ![stars](https://img.shields.io/github/stars/Createsequence/crane4j) ![maven-central](https://img.shields.io/github/v/release/Createsequence/crane4j)

# Crane4j

强大又好用的数据填充框架，用少量注解搞定一切“根据 A 的 key 值拿到 B，再把 B 的属性映射到 A”的需求”

### 简介

在我们日常开发中，常常会遇到一些“根据 A 的 key 值拿到 B，再把 B 的属性映射到 A”的需求，比如典型的：

- 对象属性中存有字典 id，需要获取对应字典值并填充到对象中；
- 对象属性中存有外键，需要关联查询对应的数据库表实体，并获取其中的指定属性填充到对象中；
- 对象属性中存有枚举，需要将枚举中的指定属性填充到对象中；

这些繁琐的数据组装工作涉及五花八门的数据源，比如字典项、配置项、枚举常量，甚至是一些关联数据的查询，它们往往大量的、重复的出现，但是在不同的接口需求中又会有细微的区别，为了处理它们又需要写大量样板代码，实在让人心烦。

`crane4j` 旨在为了解决这种烦恼而生，它是一套基于注解的数据填充框架，仅需通过少量注解配置，即可优雅高效的填充不同数据源、不同类型、不同名称的字段，从此只需专注于核心业务。

### 特性

- **多样的数据源支持**：支持将枚举、普通键值对缓存，甚至实例方法或静态方法作为数据源，也支持通过简单的自定义扩展兼容更多类型的数据源，并且对所有类型数据源都提供缓存支持；
- **强大的字段映射能力**：通过注解即可完成不同类型字段之间映射自动转换，支持模板、排序、分组、自动填充嵌套对象等等功能，除 JDK 原生反射外还支持更快的字节码调用；
- **高度的可扩展性**：所有主要组件均可由用户自由替换，配合 Spring 的依赖注入可实现轻松优雅的完成自定义扩展。
- **丰富的扩展功能**：引入 Spring 后，还提供包括方法返回值与方法入参参数的自动填充，多线程填充，自定义组合注解以及表达式等扩展功能；

### 快速开始

项目文档 document：[GitHub](https://opengoofy.github.io/crane4j/#/)

示例项目 crane4j-example：[GitHub](https://github.com/opengoofy/crane4j/tree/dev/crane4j-example) / [Gitee](https://gitee.com/CreateSequence/crane4j/tree/dev/crane4j-example)

### 友情链接

- [[ hippo4j \]](https://gitee.com/agentart/hippo4j)：强大的动态线程池框架，附带监控报警功能；

### 参与贡献和技术支持

如果在使用中遇到了问题、发现了 bug ，又或者是有什么好点子，欢迎提出你的 issues ，或者[加入社区交流群](https://opengoofy.github.io/crane4j/#/other/%E8%81%94%E7%B3%BB%E4%BD%9C%E8%80%85.html) 讨论！

若无法访问连接，或者微信群二维码失效，也可以联系作者加群：

![联系作者](https://foruda.gitee.com/images/1678072903420592910/c0dbb802_5714667.png)