-- 从 halley 同步的全部已上线 的appid
CREATE TABLE `tuhu_all_appid` (
  `id` varchar(50) NOT NULL,
  `organization_id` varchar(50) NOT NULL,
  `workspace_id` varchar(50) NOT NULL,
  `app_id` varchar(50) NOT NULL,
  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
-- 从forseti同步的 appid 与 api路径 的映射关系表
CREATE TABLE `tuhu_appid_api_mapping` (
  `id` varchar(50) NOT NULL,
  `url` varchar(255) NOT NULL,
  `app_id` varchar(50) NOT NULL,
  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `url` (`url`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
