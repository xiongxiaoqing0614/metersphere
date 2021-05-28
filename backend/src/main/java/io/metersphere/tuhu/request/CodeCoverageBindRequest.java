package io.metersphere.tuhu.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;

@Getter
@Setter
public class CodeCoverageBindRequest {
    private String workspaceName;
    private String projectName;
    private String testPlanName;
    private String testReportId;
    private String appId;
    private String branchName;
    private String commitId;
    private String stage;
    private BigInteger timestamp;
}
