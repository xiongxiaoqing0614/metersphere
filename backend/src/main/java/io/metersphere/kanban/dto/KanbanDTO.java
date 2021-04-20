package io.metersphere.kanban.dto;

import lombok.Data;

@Data
public class KanbanDTO {
    private String department;
    private String team;
    private String project;
    private int apiCount;
    private int p0apiCount;
    private int singleCount;
    private int completedSingleCount;
    private int scenarioCount;
    private int completedScenarioCount;
}
