# 自定义容器

用户可以通过实现 `Container` 接口来自定义容器，并将其声明为 `@Bean` 或 `@Component`，以便在程序中使用。

下面是一个示例：

```java
@RequiredArgsConstructor
public class UserContainer implements Container<Integer> {
    
    private final UserService userService;
    
    public String getNamespace() {
        return "user";
    }
    
    public Map<Integer, UserDO> get(Collection<Integer> ids) {
        List<UserDO> users = userService.listByIds(ids);
        return users.stream().collect(Collectors.toMap(UserDO::getId, Function.identity()));
    }
}
```

在上述示例中，我们实现了 `Container` 接口，并创建了一个根据用户ID返回`UserDO`集合的数据源容器。

在 spring 环境中，你只需要将 `UserContainer` 交给 spring 管理（比如在类上添加 `@Component` 注解）即可在项目启动后自动注册，在非 spring 环境需要手动注册到 `Crane4jGlobalConfiguration` 中。
