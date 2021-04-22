package io.metersphere.kanban.dto;

import lombok.Data;

@Data
public class TestCaseAllInfoDTO extends TestCaseSummaryDTO {
    private long completedAPICount;
    private long nonP0APICount;
    private long apiCountThisWeek;
    private long singleCountThisWeek;
    private long scenarioCountThisWeek;
}
