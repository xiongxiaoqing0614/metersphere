package io.metersphere.tuhu.dto;

import lombok.Data;
import java.util.Map;

@Data
public class OrganizationDTO {
    private String id;
    private String name;
    private Map<String, WorkSpaceDTO> workSpace;
}
