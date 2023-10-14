# 示例1: 字典值自动转换

本示例将介绍如何基于自动填充，在每次请求 Controller 接口后自动将性别字典编码转为字典值。

在本示例中，你将学会：

+ 如何配置自动填充；
+ 如何在自动填充时包装对象；

## 1.原始代码

我们假设现在有一个 `SpringMVC` 项目，其中有一个 `User` 接口用于根据 id 查询用户信息：

~~~java
@RequestMapping("/user")
@RestController
public class UserController {

    @Autowried
    private UserService userService;

    @PostMapping
    public List<UserVO> listUser(@RequestBody List<Integer> ids) {
        return userService.listByIds(ids);
    } 
}

// 忽略接口
@Service
public class UserService {

    public List<UserVO> listUser(@RequestBody List<Integer> ids) {
        List<UserVO> users = userService.listByIds(ids);

        // 填充性别
        Map<Integer, String> genderMap = Stream.of(Gender.values())
            .collect(Collectors.toMap(Gender::getCode, Gender::getName));
        users.forEach(user -> {
            Integer genderCode = foo.getGenderCode();
            user.setGenderName(orderTypeMap.get(genderCode));
        });
        return users;
    } 
}
~~~

## 2.使用自动填充

我们可以使用 crane4j 进行优化。

首先，依然是在 `UserVO` 中使用注解对填充字段进行配置：

~~~java
@Data
public class UserVO {
    private Integer id;
    
    @AssembleEnum(
        type = Gender.class, enumKey = "code", 
        props = @Mapping(ref = "genderName")
    )
    private Integer genderCode;
    private String genderName;
}
~~~

然后将 `UserService` 中原本的填充代码移除，并在 `UserController` 对应方法上添加 `@AutoOperate` 注解即可：

~~~java
@RequestMapping("/user")
@RestController
public class UserController {

    @Autowried
    private UserService userService;

    @AutoOperate(type = UserVO.class) // 声明自动填充
    @PostMapping
    public List<UserVO> listUser(@RequestBody List<Integer> ids) {
        return userService.listByIds(ids);
    } 
}
~~~

## 3.填充包装对象

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

![image-20231013231124968](http://img.xiajibagao.top/image-20231013231124968.png)

**多级包装**

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

![image-20231013230948877](http://img.xiajibagao.top/image-20231013230948877.png)
