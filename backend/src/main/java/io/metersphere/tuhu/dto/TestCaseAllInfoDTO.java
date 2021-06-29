package io.metersphere.tuhu.dto;

import lombok.Data;

@Data
public class TestCaseAllInfoDTO extends TestCaseSummaryDTO {
    private long completedAPICount;
    private long nonP0APICount;
    private long apiCountThisWeek;
    private long p0APICountThisWeek;
    private long singleCountThisWeek;
    private long scenarioCountThisWeek;
    private float appIdCoverage;
}
