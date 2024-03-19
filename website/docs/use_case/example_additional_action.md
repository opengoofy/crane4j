# 示例：如何在填充后进行附加操作

本示例将指导你如何配置填充后的附加操作，在这之前，请先确保你已经阅读过[快速开始](./../user_guide/getting_started/getting_started_abstract.md)，并且成功将 crane4j 引入你的项目。

有时候，我们会希望在填充后，再进行一些附加操作，这些操作光靠 crane4j 提供的数据源机制无法很好的解决。

比如，在上一个示例中，我们[基于枚举填充了用户的性别字段](./example_fill_enum.md)，现在我们有了一个新的需求：

- 如果用户的年龄小于等于 10 岁，那么用户名需要拼接上 "小朋友"；
- 如果用户的年龄大于 10 岁且小于 18 岁，那么用户名需要拼接上 "同学"；
- 如果用户的年龄大于 18 岁，那么用户名需要根据性别拼接上 "先生" 或者 "女士"；

此时，你可以令你的待填充对象实现 `OperationAwareBean` 接口，并在对应回调方法中添加逻辑：

~~~java
@Acccessor(chain = true)
@Data
public class Student implements OperationAwareBean {
  	private Integer age;
  	private String name;
  	private Integer genderCode;
  	private String genderName;
  
  	@Override
    public void afterOperationsCompletion() { // 该回调方法会在所有操作完成后调用
      	if (age <= 10) {
          	name = name + "小朋友";
        } else if (age <= 18) {
          	name = name + "同学";
        } else {
          	name = name + genderName;
        }
    }
}
~~~

具体内容可参见：[组件的回调方法](./../advanced/callback_of_component.md) 一节。