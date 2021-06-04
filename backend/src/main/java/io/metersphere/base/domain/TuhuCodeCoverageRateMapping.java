package io.metersphere.base.domain;

import lombok.Data;
import java.io.Serializable;

@Data
public class TuhuCodeCoverageRateMapping implements Serializable {
    private Long id;

    private String testPlanId;

    private String testReportId;

    private String appId;

    private String branchName;

    private String commitId;

    private String stage;

    private Float coverageRate;
}