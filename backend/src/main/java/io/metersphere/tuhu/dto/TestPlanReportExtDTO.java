package io.metersphere.tuhu.dto;

import io.metersphere.track.dto.TestPlanReportDTO;
import lombok.Data;

@Data
public class TestPlanReportExtDTO extends TestPlanReportDTO {
    private String groupName;
}
