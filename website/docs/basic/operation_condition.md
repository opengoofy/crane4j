# 设置操作触发条件

有时候，我们会需要根据动态的条件，选择性的填充一批对象中的某一部分，在 2.6.0 及以上版本，crane4j 通过一套类似 spring 条件装配的机制对此提供了支持。

## 1.使用

### 1.1.配置条件注解

你可以直接在原配置的基础上添加注解，为操作**指定触发条件**：

~~~java
public class Foo {
    
    @ConditionOnExpression(value = "#target.name != 'user'") // 仅当 name 属性为 user 时才应用操作
    @Assemble(container = "foo")
    private String name;
    
    @ConditionOnPropertyNotEmpty // 仅当 nested 属性不为空时才应用操作
    @Disassemble(type = Foo.class)
    private List<Foo> foos;
}
~~~

和操作注解一样，你也可以将其放置在类上：

~~~java
@ConditionOnExpression(value = "#target.name != 'user'")
@Assemble(key = "name", container = "foo")
public class Foo {
    private String name;
}
~~~

如果在操作者接口中，则可以放在方法上：

~~~java
@Operator
public interface OperatorInterface {
    
    @ConditionOnExpression(value = "#target.name != 'user'")
    @Assemble(key = "name", container = "foo")
    void fill(Foo foo);
}
~~~

### 1.2.仅应用到指定操作

当你在类、属性或方法上指定触发条件时，若该元素上同时声明了多个操作，那么条件**同时将应用到该元素上声明的所有操作**：

~~~java
@ConditionOnExpression(expression = "#target.name != 'user'") // 该条件将同时应用到下面两个装配操作
@Assemble(key = "id", container = "foo")
@Assemble(key = "key", container = "foo")
public class Foo {
    private String id;
    private String key;
}
~~~

如果你需要让操作条件仅对特定的操作生效，那么你需要让它们**通过 id 关联起来**：

~~~java
@ConditionOnExpression(
    id = "op1",  // 该条件仅应用到 op1
    value = "#target.name != 'user'"
)
@Assemble(id = "op1", key = "id", container = "foo")
@Assemble(id = "op2", key = "key", container = "foo")
public class Foo {
    private String id;
    private String key;
}
~~~

### 1.3.注解的作用域

操作条件的作用域总是仅限于该注解所在的元素本身。

简单的来说，你在属性上添加了条件注解，那么这个条件注解**仅允许对同一个属性上声明的操作生效**，类或方法同理：

~~~java
@ConditionOnExpression( // 该条件不会生效，因为该注解下面两个操作配置没有被声明在同一个元素上
    id = {"op1", "op2"},
    value = "#target.name != 'user'"
)
public class Foo {
    
    @Assemble(id = "op1", key = "id", container = "foo")
    private String id;
    
    @Assemble(id = "op2", key = "key", container = "foo")
    private String key;
}
~~~

### 1.4.组合条件

你可以同时为操作应用多个条件，此时它们将会被合并为一个组合注解：

```java
public class Foo {
    
    @ConditionOnExpression("#target.code % 3 == 0")
    @ConditionOnExpression("#target.code % 2 == 0")
    @Assemble(container = CONTAINER_NAME, sort = 2)
    private Integer code;
}
```

目前组合组件仅支持 “and” 语义，即所有的条件都满足时才允许应用该操作。

## 2.可选条件

### 2.1.当表达式结果为真

参见 `@ConditionOnExpression` 注解。

运行时，crane4j 将根据指定表达式的执行结果确认是否要应用对应的操作：

~~~java
public class Foo {
    @ConditionOnExpression(value = "#target.name != 'user'") // 仅当 name 属性为 user 时才应用操作
    @Assemble(container = "foo")
    private String name;
}
~~~

表达式的语法取决于你的表达式引擎，在 Spring 环境中，默认使用 SpEL，而在非 Spring 环境中，则使用 Ognl。

不管哪一个表达式，都默认注册了 `target` 变量，你可以在表达式中通过 `target` 引用当前要填充的对象。

### 2.2.当指定属性值等于指定值

参见 `@ConditionOnProperty` 注解。

运行时，crane4j 将根据指定属性值是否等于期望值确认是否要应用对应的操作：

~~~java
@ConditionOnProperty(property = "key", value "user") // 仅当 key 属性为 user 时才应用操作
@Assemble(key = "key", container = "foo")
public class Foo {
    
    @ConditionOnProperty(value "user") // 仅当 name 属性为 user 时才应用操作
    @Assemble(container = "foo")
    private String name;
    
    private String key;
}
~~~

**类型转换**

crane4j 默认会将 `value` 指定的期望值转为实际值的类型后再进行比较。如果你事先就知道实际值的类型，那么可以手动指定期望值类型，避免这一步：

~~~java
public class Foo {
    @ConditionOnProperty(value "123", valueType = Integer.class) // 仅当 id 属性为 123 时才应用操作
    @Assemble(container = "foo")
    private Integer id;
}
~~~

**空值判断**

默认情况下，如果实际值为 `null`，则认为条件不通过。如果你希望允许空值，那么可以将 `enableNull` 设置为 `true`：

~~~java
public class Foo {
    @ConditionOnProperty(
        value "123", 
        valueType = Integer.class,
        enableNull = true // 当 id 为空时仍然应用该操作
    )
    @Assemble(container = "foo")
    private Integer id;
}
~~~

### 2.3.当指定属性值非空

参见 `@ConditionOnPropertyNotEmpty` 与 `@ConditionOnPropertyNotNull` 注解。

运行时，crane4j 将根据指定属性值是否为空确认是否要应用对应的操作：

~~~java
@ConditionOnPropertyNotEmpty(property = "keys", value "user") // 仅当 keys 属性不为空才应用操作
@Assemble(key = "keys", container = "foo")
public class Foo {
    
    @ConditionOnPropertyNotNull(value "user") // 仅当 name 属性不为null时才应用操作
    @Assemble(container = "foo")
    private String name;
    
    private Collection<String> keys;
}
~~~

其中，`@ConditionOnPropertyNotEmpty` 可以判断数组、集合或字符串是否为空，而 `@ConditionOnPropertyNotNull` 只能判断是否为 `null`。

## 3.自定义条件

如果有必要，你也可以自定义一个条件注解，实现自己的条件逻辑。

比如，我们要定义一个条件，即仅当目标对象实现了 `Serializable` 接口时才允许进行填充，那么总共需要三步：

**自定义注解**

定义一个条件注解 `@ConditionOnTargetSerializable`：

~~~java
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ConditionOnTargetSerializable {
    
}
~~~

**实现注解解析器**

你需要实现 `ConditionParser` 接口，定义一个用于解析 `@ConditionOnTargetSerializable` 注解的解析器.

crane4j 默认提供了 `AbstractConditionParser` 模板类，它已经实现好了大部分逻辑：

~~~java
public class TargetSerializableConditionParser
    extends AbstractConditionParser<ConditionOnTargetSerializable> {
    
    public TargetSerializableConditionParser(AnnotationFinder annotationFinder) {
        super(annotationFinder, ConditionOnTargetSerializable.class);
    }
    
    @Nullable
    @Override
    protected Condition createCondition(
        AnnotatedElement element, ConditionOnExpression annotation) {
        return (t, op) -> t instanceof Serializable;
    }
}
~~~

**注册注解解析器**

要令自定义注解解析器生效，你需要将其注册到 `ConditionalTypeHierarchyBeanOperationParser` 中。

在 Spring 环境，你只需要把自定义的注解解析器交给 Spring 管理即可，crane4j 会自行完成注册。

而在非 Spring 环境中，你需要通过全局配置类获取该组件，并手动完成注册：

~~~java
Crane4jGlobalConfiguration configuration = SimpleCrane4jGlobalConfiguration.create();
ConditionalTypeHierarchyBeanOperationParser parser = configuration.getBeanOperationParser(ConditionalTypeHierarchyBeanOperationParser.class);
parser.registerConditionParser(new ConditionalTypeHierarchyBeanOperationParser(......));
~~~

