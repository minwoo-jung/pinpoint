CREATE TABLE `nss_auth_override_user` (
  `user_id` char(7) NOT NULL,
  PRIMARY KEY (`user_id`)
);

CREATE TABLE `nss_auth_prefix` (
  `prefix` varchar(7) NOT NULL,
  PRIMARY KEY (`prefix`)
);

CREATE TABLE `paas_organization` (
  `number` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `organization` varchar(50) NOT NULL,
  `database_name` varchar(30) NOT NULL,
  `hbase_namespace` varchar(30) NOT NULL,
  PRIMARY KEY (`number`),
  UNIQUE KEY `organization_idx` (`organization`)
);