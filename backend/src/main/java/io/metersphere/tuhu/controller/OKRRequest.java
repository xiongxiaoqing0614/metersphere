package io.metersphere.tuhu.controller;
import lombok.Data;

@Data
public class OKRRequest {
    private String id;
    private String name;
    private String description;
    private String wsId;
    private long okrApiTotal;
    private long okrApiP0;
    private long okrApiTestTotal;
    private long okrApiTestP0;
    private long okrScenarioTestTotal;
}
