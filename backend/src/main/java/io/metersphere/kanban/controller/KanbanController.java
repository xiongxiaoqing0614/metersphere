package io.metersphere.kanban.controller;

import io.metersphere.api.dto.datacount.ApiDataCountResult;
import io.metersphere.api.dto.datacount.response.ApiDataCountDTO;
import io.metersphere.api.service.ApiAutomationService;
import io.metersphere.api.service.ApiDefinitionService;
import io.metersphere.api.service.ApiTestCaseService;
import io.metersphere.base.domain.TestPlanReportExample;
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
    private CheckPermissionService checkPermissionService;


    @GetMapping("summary")
    public List<TestCaseAllInfoDTO> dashboardSummary() {
        return kanbanService.getSummary();
    }

    @GetMapping("exeSummary")
    public List<ExecutionAllInfoDTO> exeSummary() {
        return kanbanService.getExeSummary();
    }

}
