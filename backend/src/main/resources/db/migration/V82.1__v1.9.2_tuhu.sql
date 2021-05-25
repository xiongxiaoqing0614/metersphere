CREATE TABLE IF NOT EXISTS `tuhu_okr`
(
    id             varchar(50)   not null,
    `name`        varchar(64) NOT NULL COMMENT 'OKR name',
    `description` varchar(255) DEFAULT NULL COMMENT 'OKR description',
    `create_time` bigint(13)  NOT NULL COMMENT 'Create timestamp',
    `update_time` bigint(13)  NOT NULL COMMENT 'Update timestamp',
    `workspace_id` varchar(50) NOT NULL COMMENT 'Workspace ID this OKR belongs to',
    `api_total`        int      DEFAULT NULL COMMENT 'Planned total API',
    `api_p0`        int      DEFAULT NULL COMMENT 'Planned P0 API',
    `api_test_total`    int      DEFAULT NULL COMMENT 'Planned total API test cases',
    `api_test_p0`        int      DEFAULT NULL COMMENT 'Planned P0 API test cases',
    `scenario_test_total`  int      DEFAULT NULL COMMENT 'Planned scenario API test cases',
    primary key (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;