package io.metersphere.tuhu.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrgAndWsInfoDTO {
    private String id;
    private String orgId;
    private String OrgName;
    private String wsId;
    private String wsName;
    private String appId;
}
