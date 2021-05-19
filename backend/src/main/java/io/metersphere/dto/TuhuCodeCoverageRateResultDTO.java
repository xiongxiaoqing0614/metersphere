package io.metersphere.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TuhuCodeCoverageRateResultDTO {

    private String testPlanId;

    private String testReportId;

    private Float coverageRate;

}
