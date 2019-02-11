CREATE TABLE `manager` (
  `number` int(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` VARCHAR(30) NOT NULL,
  PRIMARY KEY (`number`)
);
ALTER TABLE manager ADD UNIQUE KEY user_id_idx (user_id);

CREATE TABLE `app_auth_user_group` (
  `number` int(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `application_id` VARCHAR(60) NOT NULL,
  `user_group_id` VARCHAR(30) NOT NULL,
  `role` VARCHAR(30) NOT NULL,
  `configuration` TEXT NOT NULL,
  PRIMARY KEY (`number`)
);
ALTER TABLE app_auth_user_group ADD UNIQUE KEY application_id_user_group_id_idx (application_id, user_group_id);

CREATE TABLE `agent_statistics` (
  `agent_count` int(10) UNSIGNED NOT NULL,
  `date_time` DATETIME NOT NULL,
  PRIMARY KEY (`date_time`)
);

CREATE TABLE `user_configuration` (
  `number` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` varchar(30) NOT NULL,
  `configuration` text NOT NULL,
  PRIMARY KEY (`number`)
);

ALTER TABLE user_configuration ADD UNIQUE KEY user_id_idx (user_id);

CREATE TABLE `role_definition` (
  `number` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `role_id` varchar(30) NOT NULL,
  `permission_collection` text NOT NULL,
  PRIMARY KEY (`number`)
);

ALTER TABLE role_definition ADD UNIQUE KEY role_id_idx (role_id);

CREATE TABLE `user_role` (
  `number` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` varchar(90) DEFAULT NULL,
  `role_id` varchar(30) DEFAULT NULL,
  PRIMARY KEY (`number`)
);

ALTER TABLE user_role ADD UNIQUE KEY user_id_role_id_idx (user_id, role_id);