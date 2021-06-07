package io.metersphere.controller.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CodeCoverageRequest {
    private String[] testPlanIds;
    private String[] testReportIds;
}
