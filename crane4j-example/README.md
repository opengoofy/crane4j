## Example

此模块为相对独立的示例模块，也作为某种程度上的集成测试使用。

该模块提供的示例仅提供本体中各个模块里面，文档说明较不直观或操作相对复杂的功能，如需求某些功能的用例可以在 issues 中提出。



## crane4j-spring-boot-starter-example

- `AutoOperateExample`：如何使用方法返回值、方法参数自动填充功能；
- `ContainerEnumExample`：如何在使用 `@ContainerEnum` 或不使用注解的情况下配置枚举作为数据源容器；
- `ContainerMethodExample`：如何使用 `@ContainerMethod` 将方法配置为数据源容器；
- `PropMappingExample`：如何配置一对一或一/多对多情况下的属性映射；
- `OneToManyAssembleExample`：如何使用一对多装配器完成一对多类型的填充；
- `ManyToManyAssembleExample`：如何使用多对多装配器完成多对多类型的填充；
- `ChainPropMappingExample`：如何在属性映射中通过类似 "xx.xx.xx" 的链式操作费进行属性映射；



## crane4j-mybatis-plus-extension-example

测试类入口为 `MpExtensionExample`，执行前务必确保 `/resource/test.yml` 中配置数据源可用，并且数据源中存在表定义如下：

~~~sql
-- ----------------------------
 -- Table structure for foo
 -- ----------------------------
 DROP TABLE IF EXISTS `foo`;
 CREATE TABLE `foo`  (
   `id` int(11) NOT NULL AUTO_INCREMENT,
   `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '',
   `age` int(3) NULL DEFAULT NULL,
   `sex` int(1) NULL DEFAULT NULL,
   PRIMARY KEY (`id`) USING BTREE
 ) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;
~~~

且数据库表 `foo` 中至少存在下述四条记录：

~~~sql
 -- ----------------------------
 -- Records of foo
 -- ----------------------------
 INSERT INTO `foo` VALUES (1, '小明', 18, 1);
 INSERT INTO `foo` VALUES (2, '小红', 18, 0);
 INSERT INTO `foo` VALUES (3, '小刚', 17, 1);
 INSERT INTO `foo` VALUES (4, '小李', 19, 0);
~~~

