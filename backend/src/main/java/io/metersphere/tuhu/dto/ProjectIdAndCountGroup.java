package io.metersphere.tuhu.dto;

import lombok.Data;

@Data
public class ProjectIdAndCountGroup {
    private String groupField;
    private long countNumber;
    private String projectId;
}
