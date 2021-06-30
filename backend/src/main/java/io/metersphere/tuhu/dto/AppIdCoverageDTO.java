package io.metersphere.tuhu.dto;

import lombok.Data;

@Data
public class AppIdCoverageDTO {
    private String dep;
    private String depId;
    private String team;
    private String teamId;
    private Long allAppIdNum;
    private Long partAppIdNum;
    private Float percent;
}
