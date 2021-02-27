package io.metersphere.api.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ApiTestImportRequest {
    private String name;
    private String moduleId;
    private String modulePath;
    private String environmentId;
    private String projectId;
    private String platform;
    private Boolean useEnvironment;
    // 来自场景的导入不需要存储
    private boolean saved = true;
    private String swaggerUrl;
    //导入策略
    private String modeId;
    private String userId;
    //调用类型
    private String type;

    private String appId;
}
