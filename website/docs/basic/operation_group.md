# 操作分组

`crane4j` 提供了分组填充的功能，用法类似 `hibernate-validator` 中的分组校验。

你可以在 `@Assemble` 或 `@Disassemble` 中通过 `groups` 属性指定操作所属的组，然后在执行时指定仅执行或不执行特定的操作组。比如，在创建对象时只填充一部分属性，而在更新对象时填充另一部分属性。

<img src="https://img.xiajibagao.top/image-20230225012401927.png" alt="image-20230225012401927" style="zoom:150%;" />

## 1.配置分组

你可以通过在 `@Assemble` 和 `@Disassemble` 注解中使用 `groups` 属性来指定分组。

比如：

~~~java
public class UserVO {
    @Assemble(container = "user", props = @Mapping(src = "role", ref = "role"), groups = "admin")
    @Assemble(container = "user", props = @Mapping(src = "name", ref = "name"), groups = {"base", "admin"})
    private Integer id;
    private String name;
    private String role;
}
~~~

在示例中，我们有一个 `UserVO` 类，其中有一个 `id` 属性，我们声明了两个装配操作：

- 第一个装配操作是根据 `id` 装配 `name` 属性，这个装配操作只在 `admin` 组中生效；
- 第二个装配操作是根据 `id` 装配 `role` 属性，这个装配操作在 `base` 和 `admin` 组中生效；

当我们执行填充操作时，如果指定的组中包含了装配操作的分组，那么该装配操作将生效，否则，它将被忽略。

类似的，`@Disassemble` 注解也支持使用 `groups` 属性。

比如：

~~~java
public class Foo {
    @Assemble(container = "user", props = @Mapping(src = "name", ref = "name"), groups = "admin")
    private Integer id;
    private String name;
    @Disassemble(type = Foo.class, groups = "nested")
    private List<Foo> fooList;
}
~~~

在上述示例中，仅当指定执行操作组为 `nested` 时，才会执行对 `fooList` 的拆卸操作。

## 2.按操作组执行

你可以在手动填充和自动填充的情况下使用分组功能。

**手动填充**

当进行手动填充时，使用 `OperateTemplate` 的不同重载方法来设置本次填充操作的执行组：

~~~java
// 如果目标对象所属的填充组与指定的任何一个组匹配，执行填充操作
executeIfMatchAnyGroups(Object target, String... groups);

// 如果目标对象所属的填充组与指定的任何一个组都不匹配，执行填充操作
executeIfNoneMatchAnyGroups(Object target, String... groups);

// 如果目标对象所属的填充组与指定的所有组都匹配，执行填充操作
executeIfMatchAllGroups(Object target, String... groups);

// 仅执行通过指定过滤器条件的操作
execute(Object target, Predicate<? super KeyTriggerOperation> filter);
~~~

**自动填充**

当自动填充时，在 `@AutoOperate` 注解中使用 `includes` 或者 `excludes` 属性来指定仅执行/不执行特定的操作组：

~~~java
// 填充返回值
@AutoOperate(type = Foo.class, includes = "base")
public List<Foo> getFooList() {
    // do nothing
}

// 填充参数
@ArgAutoOperate(
    @AutoOperate(value = "foos", type = Foo.class, includes = "base")
)
public void setFooList(List<Foo> foos) {
    // do nothing
}
~~~