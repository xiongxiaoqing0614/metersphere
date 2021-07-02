-- 修改 appid 字段长度
ALTER TABLE tuhu_all_appid MODIFY `app_id` varchar(70) NOT NULL;
ALTER TABLE tuhu_appid_api_mapping MODIFY `app_id` varchar(70) NOT NULL;

