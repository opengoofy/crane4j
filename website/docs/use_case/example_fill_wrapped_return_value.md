# 示例：如何填充被包装的返回值

本示例将指导你如何在进行自动填充时，正确的处理被包装的返回在。在这之前，请先确保你已经阅读过[快速开始](./../user_guide/getting_started/getting_started_abstract.md)，并且成功将 crane4j 引入你的项目。

关于如何配置填充操作的具体内容，请参见：[触发填充操作](./../basic/trigger_operation.md)。

## 1.填充被包装的数据

有时候，我们会在 `Controller` 中显式的使用通用响应体包装返回值，比如：

~~~java
@PostMapping
public Result<List<UserVO>> listUser(@RequestBody List<Integer> ids) {
    // 返回值被通用响应体包装
    return new Result<>(userService.listByIds(ids));
}

// 通用响应体
@AllArgsConstructor
@Data
public class Result<T> {
    private String msg = "ok";
    private Integer code = 200;
    private T data;
    public Result(T data) {
        this.data = data;
    }
}
~~~

此时，我们真正需要填充的数据其实是 `Result.data`，则可以在 `@AutoOperate` 注解中通过 `on` 属性指定：

~~~java
@AutoOperate(type = UserVO.class, on = "data") // 声明自动填充
@PostMapping
public Result<List<UserVO>> listUser(@RequestBody List<Integer> ids) {
    // 返回值被通用响应体包装
    return new Result<>(userService.listByIds(ids));
}
~~~

![image-20231013231124968](./image-20231013231124968-0813973.png)

## 2.填充被多级包装的数据

在特定情况下，我们会存在多级包装的情况。比如：

通用响应体包装了分页对象，然后分页对象里面才是需要填充的数据，则此时我们可以有：

~~~java
@AutoOperate(type = UserVO.class, on = "data.list") // 声明自动填充
@PostMapping
public Result<Page<List<UserVO>>> listUser(@RequestBody List<Integer> ids, @RequestParam PageDTO pageDTO) {
    // Result.data -> Page.list -> List<UserVo>
    return new Result<>(userService.pageByIds(ids, pageDTO));
}
~~~

![image-20231013230948877](./image-20231013230948877-0813989.png)
