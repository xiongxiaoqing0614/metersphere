package io.metersphere.tuhu.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TuhuCodeCoverageRateResultDTO {
    private Long id;

    private String testPlanId;

    private String testReportId;

    private String appId;

    private String branchName;

    private String commitId;

    private String stage;

    private Float coverageRate;

}
