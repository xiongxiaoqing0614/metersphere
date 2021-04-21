package io.metersphere.kanban.dto;

import lombok.Data;

@Data
public class TestCaseAllInfoDTO extends TestCaseSummaryDTO {
    private long apiCountThisWeek;
    private long singleCountThisWeek;
    private long scenarioCountThisWeek;
}
