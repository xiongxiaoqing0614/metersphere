package io.metersphere.tuhu.dto;
import lombok.Data;
import io.metersphere.track.dto.TestPlanDTOWithMetric;

@Data
public class ExecutionAllInfoDTO extends TestPlanDTOWithMetric{
    private String department;
    private String team;
}
