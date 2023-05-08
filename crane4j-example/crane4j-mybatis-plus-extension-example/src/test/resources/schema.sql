DROP TABLE IF EXISTS foo;

CREATE TABLE `foo` (
   `id` int(11) NOT NULL AUTO_INCREMENT,
   `name` varchar(255) DEFAULT '',
   `age` int(3) DEFAULT NULL,
   `sex` int(1) DEFAULT NULL,
   PRIMARY KEY (`id`)
);