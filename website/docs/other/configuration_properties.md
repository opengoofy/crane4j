# 配置文件

在 Spring 环境中，你可以基于配置文件对 crane4j 的一些可选项进行配置。

## 1.反射

### 1.1.是否启用字节码反射

`crane4j` 通过默认引入了基于字节码的反射增强库 [ReflectAsm](https://github.com/EsotericSoftware/reflectasm) ，用户可以通过  `enable-asm-reflect` 开启反射增强功能：

~~~yml
crane4j:
 # 启用字节码增强
 enable-asm-reflect: true
~~~

默认为 `false`，开启后可以一定程度上提升字段映射的性能，不过对应的可能会带来额外的内存消耗。

:::warning

由于兼容性问题，该配置只在 java8 的版本有效。

:::

### 1.2.是否支持处理Map对象

是否支持对 `Map` 对象进行属性映射，默认为 `true`：

~~~yml
crane4j:
 enable-map-operate: true
~~~

如果你的项目里面没有通过 `crane4j` 直接处理 `Map` 或者`JSONObject` 的需求，可以关闭它。

### 1.3.是否支持链式操作符

是否支持 `xx.xx.xx` 这样的链式操作符，默认为 `true`：

~~~yml
crane4j:
 enable-chain-operate: true
~~~

## 2.容器

### 2.1.扫描常量容器

`crane4j` 支持将常量类也作为数据源适配为容器，因此提供了 `container-enum-packages` 配置，用于扫描一个或多个包路径下的枚举，在应用启动后自动注册为容器：

```yml
crane4j:
 # 扫描常量包路径
 container-constant-packages: cn.demo.constant.*
```

关于容器部分，参见[常量容器](./../basic/container/constant_container.md)一节。

### 2.2.扫描枚举容器

`crane4j` 支持将枚举也作为数据源适配为容器，因此提供了 `container-enum-packages` 配置，用于扫描一个或多个包路径下的枚举，在应用启动后自动注册为容器：

```yml
crane4j:
 # 扫描枚举包路径
 container-enum-packages: cn.demo.constant.enums.*
 # 是否只加载被@ContainerEnum注解的枚举
 only-load-annotated-enum: true
```

关于容器部分，参见[枚举容器](./../basic/container/enum_container.md)一节。

### 2.3.扫描方法容器

`crane4j` 支持将被 Spring 管理的 `bean` 中带有 `@ContainerMethod` 方法也适配为容器：

~~~yaml
crane4j:
 enable-method-container: true
~~~

默认为 `true`。

### 2.4.容器缓存配置

用户可以通过 `cache-containers` 配置为指定的数据源容器添加缓存功能：

~~~yml
crane4j:
 # 声明哪些数据源需要包装为缓存
 cache-containers:
  shared-cache: testContainer
~~~

上述示例表示，在项目启动后，通过 `CacheManager` 为命名空间为 `testContainer` 的容器挂载缓存空间 `shared-cache`。

## 3.自动填充

`crane4j` 默认支持自动方法返回值与方法入参，用户也可以通过配置自定义是否关闭该功能：

~~~yml
crane4j:  
 # 是否启用参数自动填充
 enable-method-argument-auto-operate: false
 # 是否启用返回值自动填充
 enable-method-result-auto-operate: false
~~~

关于自动填充，参见 [自动填充](./../execute/4.2.自动填充.md) 一节。

## 4.操作配置预解析

由于操作配置对象 `BeanOperation` 皆由对应的 `Class` 解析而来，因此若解析器具备缓存功能，可以通过 `operate-entity-packages` 配置实体类包路径，在执行器进行预解析，从而在后续调用时略过配置解析步骤，加快执行速度：

~~~yml
crane4j:
 # 操作配置预解析
 operate-entity-packages: cn.crane4j.springboot.config.*
~~~
