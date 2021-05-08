package io.metersphere.kanban.controller;

import io.metersphere.api.dto.datacount.ApiDataCountResult;
import io.metersphere.api.dto.datacount.response.ApiDataCountDTO;
import io.metersphere.api.service.ApiAutomationService;
import io.metersphere.api.service.ApiDefinitionService;
import io.metersphere.api.service.ApiTestCaseService;
import io.metersphere.kanban.dto.TestCaseAllInfoDTO;
import io.metersphere.kanban.dto.TestCaseSummaryDTO;
import io.metersphere.kanban.service.KanbanService;
import io.metersphere.service.CheckPermissionService;
import io.metersphere.track.dto.TestPlanDTOWithMetric;
import io.metersphere.track.request.testcase.QueryTestPlanRequest;
import io.metersphere.track.service.TestPlanService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import io.metersphere.kanban.dto.ExecutionAllInfoDTO;

@RestController
@RequestMapping(value = "/kanban")
public class KanbanController {

    @Resource
    private KanbanService kanbanService;

    @Resource
    private ApiDefinitionService apiDefinitionService;

    @Resource
    private ApiTestCaseService apiTestCaseService;

    @Resource
    private ApiAutomationService apiAutomationService;

    @Resource
    private CheckPermissionService checkPermissionService;

    @Resource
    private TestPlanService testPlanService;

    @GetMapping("summary")
    public List<TestCaseAllInfoDTO> dashboardSummary() {
        List<TestCaseSummaryDTO> summaryList = kanbanService.getSummary();
        List<TestCaseAllInfoDTO> allInfoList = new ArrayList<TestCaseAllInfoDTO>();
        for(TestCaseSummaryDTO summaryData : summaryList) {
            TestCaseAllInfoDTO allInfo = new TestCaseAllInfoDTO();
            BeanUtils.copyProperties(summaryData, allInfo);
            String projectId = summaryData.getProjectId();
            long dateCountByCreateInThisWeek = apiDefinitionService.countByProjectIDAndCreateInThisWeek(projectId);
            allInfo.setApiCountThisWeek(dateCountByCreateInThisWeek);
            ApiDataCountDTO apiCountResult = new ApiDataCountDTO();

            List<ApiDataCountResult> countResultByStatelList = apiDefinitionService.countStateByProjectID(projectId);
            apiCountResult.countStatus(countResultByStatelList);
            allInfo.setCompletedAPICount(apiCountResult.getFinishedCount());
            allInfo.setNonP0APICount(allInfo.getApiCount() - allInfo.getP0APICount());

            long dateSingleCountByCreateInThisWeek = apiTestCaseService.countByProjectIDAndCreateInThisWeek(projectId);
            allInfo.setSingleCountThisWeek(dateSingleCountByCreateInThisWeek);

            long dateScenarioCountByCreateInThisWeek = apiAutomationService.countScenarioByProjectIDAndCreatInThisWeek(projectId);
            allInfo.setScenarioCountThisWeek(dateScenarioCountByCreateInThisWeek);

            allInfoList.add(allInfo);
        }
        return allInfoList;
    }

    @GetMapping("exeSummary")
    public List<ExecutionAllInfoDTO> exeSummary() {
        List<TestCaseSummaryDTO> summaryList = kanbanService.getSummary();
        List<ExecutionAllInfoDTO> testPlans = new ArrayList<ExecutionAllInfoDTO>();
        for(TestCaseSummaryDTO summaryData : summaryList) {
            QueryTestPlanRequest request = new QueryTestPlanRequest();
            request.setProjectId(summaryData.getProjectId());
            List<TestPlanDTOWithMetric> oriTestPlans = testPlanService.listTestPlanByProject(request);
            testPlanService.calcTestPlanRate(oriTestPlans);
            for(TestPlanDTOWithMetric oriTestPlan : oriTestPlans) {
                ExecutionAllInfoDTO thTestPlan = new ExecutionAllInfoDTO();
                BeanUtils.copyProperties(oriTestPlan, thTestPlan);
                thTestPlan.setDepartment(summaryData.getDepartment());
                thTestPlan.setTeam(summaryData.getTeam());
                testPlans.add(thTestPlan);
            }
        }
        return testPlans;
    }

}
