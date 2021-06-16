
-- 途虎代码覆盖率服务关系映射表
CREATE TABLE IF NOT EXISTS `tuhu_code_coverage_rate_mapping` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `testPlanId` varchar(50) NOT NULL COMMENT '测试计划id',
    `testReportId` varchar(50) DEFAULT NULL COMMENT '测试报告id',
    `appId` varchar(50) NOT NULL COMMENT '应用id',
    `branchName` varchar(50) DEFAULT NULL COMMENT '分支名称',
    `commitId` varchar(50) NOT NULL COMMENT '提交记录ID',
    `stage` enum('UnitTest','APITest','IntegrateTest','SystemTest','All') NOT NULL COMMENT '所在测试阶段',
    `coverageRate` float DEFAULT NULL COMMENT '代码覆盖率，备用字段',
    `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `testPlanId_idx` (`testPlanId`),
    KEY `testReportId_idx` (`testReportId`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4;
