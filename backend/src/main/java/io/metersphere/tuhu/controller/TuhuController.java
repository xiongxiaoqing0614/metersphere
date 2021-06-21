package io.metersphere.tuhu.controller;

import com.alibaba.fastjson.JSONObject;
import io.metersphere.tuhu.dto.TuhuCodeCoverageRateResultDTO;
import io.metersphere.tuhu.request.CodeCoverageBindRequest;
import io.metersphere.tuhu.request.CodeCoverageRequest;
import io.metersphere.tuhu.service.TuhuService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping(value = "/tuhu")
public class TuhuController {

    @Resource
    private TuhuService tuhuService;

    @PostMapping(value = "/testplan/coveragerate")
    public List<TuhuCodeCoverageRateResultDTO> testPlanCoverageRate(@RequestBody CodeCoverageRequest codeCoverageRequests) {
        return tuhuService.getCodeCoverageRateList(codeCoverageRequests);
    }

    @PostMapping(value = "/testplan/coveragerate/bind")
    public Object testPlanCoverageRateBind(@RequestBody CodeCoverageBindRequest codeCoverageBindRequest) {
        Integer num = tuhuService.addCodeCoverageRateMapping(codeCoverageBindRequest);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("num", num);
        return jsonObject;
    }

    @GetMapping(value = "/testplan/reportid/last")
    public String testPlanReportId(@RequestParam BigInteger timestamp, @RequestParam String workspaceName,
                                   @RequestParam String projectName, @RequestParam String testPlanName) {

        CodeCoverageBindRequest codeCoverageBindRequest = new CodeCoverageBindRequest();
        codeCoverageBindRequest.setTimestamp(timestamp);
        codeCoverageBindRequest.setWorkspaceName(workspaceName);
        codeCoverageBindRequest.setProjectName(projectName);
        codeCoverageBindRequest.setTestPlanName(testPlanName);

        return tuhuService.getTestReportByTimestamp(codeCoverageBindRequest);
    }

    @GetMapping(value = "/testplan/report")
    public void testPlanReport(@RequestParam String appId, @RequestParam String branchName,
                               @RequestParam String commitId, @RequestParam String stage,
                               @RequestParam(value="zip",required=false) Boolean zip,
                               HttpServletResponse response) throws IOException {

        tuhuService.redirectReportUrl(response, appId, branchName, commitId, stage, zip);
    }
}