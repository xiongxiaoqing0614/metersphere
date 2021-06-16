package io.metersphere.tuhu.dto;

import lombok.Data;

@Data
public class TestCaseSummaryDTO {
    private String department;
    private String team;
    private String wsId;
    private String project;
    private String projectId;
    private long apiCount;
    private long p0APICount;
    private long singleCount;
    private long completedSingleCount;
    private long scenarioCount;
    private long completedScenarioCount;
    private long completedP0APICount;
}
