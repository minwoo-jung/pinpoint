CREATE TABLE `span_stat_agent` (
    `organization` varchar(50) NOT NULL,
    `applicationId` varchar(50) NOT NULL,
    `agentId` varchar(50) NOT NULL,
    `count` bigint(20) NOT NULL,
    `timestamp` datetime NOT NULL,
    PRIMARY KEY (`organization`,`applicationId`,`agentId`,`timestamp`)
);

CREATE TABLE `span_stat_application` (
  `organization` varchar(50) NOT NULL,
  `applicationId` varchar(50) NOT NULL,
  `timestamp` datetime NOT NULL,
  `count` bigint(20) NOT NULL,
  PRIMARY KEY (`organization`,`applicationId`,`timestamp`)
);

CREATE TABLE `span_stat_organization` (
  `organization` varchar(50) NOT NULL,
  `timestamp` datetime NOT NULL,
  `count` bigint(20) NOT NULL,
  PRIMARY KEY (`organization`,`timestamp`)
);

