# 填充嵌套对象

在某些情况下，我们需要填充的对象中可能嵌套了其他对象，这种情况下，我们需要先将这些嵌套对象拆分出来，然后再进行填充操作。这个将嵌套对象取出并展开的操作称为**拆卸操作**。

嵌套对象可能存在多层级的情况，因此在执行填充操作之前，我们需要先完成拆卸操作，将所有的嵌套对象展开，然后再统一进行装配。

![DissembleOperation](https://img.xiajibagao.top/image-20230220182831112.png)

## 1.声明拆卸操作

拆卸操作支持处理数组、集合 (`Collection`) 或单个对象。此外，与装配操作一样，拆卸操作同样支持在类或属性上声明。

### 1.1.在属性上声明

我们可以直接在需要进行拆卸操作的属性上使用 `@Disassemble` 注解进行声明：

```java
public class Department {
    private Integer id;
    @Disassemble(type = Employee.class)
    private List<Employee> employees;
}
```

在上面的示例中，对于填充 `Department` 对象之前，会先将 `Department` 中的所有 `Employee` 对象取出并展开。如果 `Employee` 对象中还存在需要拆卸的嵌套对象，也会一并取出并展开，一直递归下去，直到所有的对象都被展开为止。

### 1.2.在方法上声明

在**有返回值的无参方法**上声明也是允许的，这种情况下 crane4j 会认为其为一个 fluent 风格的 getter 方法。比如：

```java
public class Department {
    private Integer id;
    @Disassemble(type = Employee.class)
    public List<Employee> getEmployees() {
        // return employee list
    }
}
```

### 1.3.在类上声明

我们也可以将 `@Disassemble` 注解声明在类上，此时你需要使用 `key` 属性显式指定需要拆卸的字段：

```java
// 直接声明
@Disassemble(key = "employees", type = Employee.class)
public class Department {
    private Integer id;
    private List<Employee> employees;
}
```

### 1.3.递归拆卸

一般情况下，拆卸操作是递归完成的。也就是说，无论你嵌套了多少层，在开始装配前，都会全部取出并摊平：

~~~java
public class Department {
    private Integer id;
    private String name;
    @Disassemble(type = Department.class) // 递归填充下级部门
    private List<Department> departments;
}
~~~

:::warning

注意，你需要自己避免循环引用，否则会栈溢出。

:::

## 2.自动推断类型

在某些情况下，无法在编译期确定要填充的对象类型。此时，可以不指定 `type` 属性，而是在执行拆卸操作时动态推断类型：

```java
public class Department<T> {
    private Integer id;
    @Disassemble // 无法确定填充类型
    private List<T> employees;
}
```

上述示例中，无法在编译期确定 `employees` 属性的类型，因此没有指定 `type` 属性。在执行拆卸操作时，会动态推断 `employees` 属性的类型。

这个功能是通过类型解析器 `TypeResolver` 实现，你也可以提供自己的实现类来替换默认的类型解析器。

## 3.拆卸操作处理器

与装配操作类似，拆卸操作也依赖于拆卸操作处理器 `DisassembleOperationHandler` 来完成。用户可以在注解中使用 `handler` 或 `handlerType` 属性来指定要使用的处理器。

例如：

```java
public class Department {
    private Integer id;
    @Disassemble(
        type = Employee.class,
        handlerType = ReflectiveDisassembleOperationHandler.class // 指定操作处理器
    )
    private List<Employee> employees;
}
```

在配置解析过程中，会根据指定的类型和处理器类型获取对应的操作处理器。

用户可以根据自己的需求，实现自定义的拆卸操作处理器，并通过 `handler` 或 `handlerType` 属性进行指定。目前默认的，也是唯一的拆卸操作处理就是 `ReflectiveDisassembleOperationHandler`。