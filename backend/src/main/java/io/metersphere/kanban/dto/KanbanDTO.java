package io.metersphere.kanban.dto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KanbanDTO {
    private String department;
    private String team;
    private String project;
    private int apiCount;
    private int singleCount;
    private int scenarioCount;
}
