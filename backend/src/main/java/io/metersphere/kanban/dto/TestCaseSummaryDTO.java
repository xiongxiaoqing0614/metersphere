package io.metersphere.kanban.dto;

import lombok.Data;

@Data
public class TestCaseSummaryDTO {
    private String department;
    private String team;
    private String project;
    private String projectId;
    private long apiCount;
    private long p0apiCount;
    private long singleCount;
    private long completedSingleCount;
    private long scenarioCount;
    private long completedScenarioCount;
}
