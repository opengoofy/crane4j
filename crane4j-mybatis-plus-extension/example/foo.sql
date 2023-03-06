/*
 Navicat Premium Data Transfer

 Source Server         : ServiceRemote
 Source Server Type    : MySQL
 Source Server Version : 50718
 Source Host           : gz-cynosdbmysql-grp-nl9mays3.sql.tencentcdb.com:22327
 Source Schema         : crane4j-example

 Target Server Type    : MySQL
 Target Server Version : 50718
 File Encoding         : 65001

 Date: 03/03/2023 17:34:25
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

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

-- ----------------------------
-- Records of foo
-- ----------------------------
INSERT INTO `foo` VALUES (1, '小明', 18, 1);
INSERT INTO `foo` VALUES (2, '小红', 18, 0);
INSERT INTO `foo` VALUES (3, '小刚', 17, 1);
INSERT INTO `foo` VALUES (4, '小李', 19, 0);

SET FOREIGN_KEY_CHECKS = 1;
