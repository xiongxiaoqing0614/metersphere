package io.metersphere.tuhu.controller;
import lombok.Data;

@Data
public class OKRRequest {
    private String name;
    private String description;
    private String workspace_id;
    private long api_total;
    private long api_p0;
    private long api_test_total;
    private long api_test_p0;
    private long scenario_test_total;
}
