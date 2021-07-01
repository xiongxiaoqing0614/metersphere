package io.metersphere.tuhu.controller;

import io.metersphere.tuhu.dto.TestCaseAllInfoDTO;
import io.metersphere.tuhu.service.GraphService;
import io.metersphere.tuhu.service.KanbanService;
import io.metersphere.service.CheckPermissionService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;
import io.metersphere.tuhu.dto.ExecutionAllInfoDTO;

@RestController
@RequestMapping(value = "/tuhu/kanban")
public class KanbanController {

    @Resource
    private KanbanService kanbanService;

    @Resource
    private GraphService graphService;

    @Resource
    private CheckPermissionService checkPermissionService;

    @Deprecated
    @GetMapping("/summary")
    public List<TestCaseAllInfoDTO> dashboardSummary() {
      //  return kanbanService.getSummary();
        return kanbanService.getSummaryV2();
    }

    @GetMapping("/summary2")
    public List<TestCaseAllInfoDTO> dashboardSummaryV2() {
        return kanbanService.getSummaryV2();
    }

    @GetMapping("/exeSummary")
    public List<ExecutionAllInfoDTO> exeSummary() {
        return kanbanService.getExeSummary();
    }

    @GetMapping("/exeSummary2")
    public List<ExecutionAllInfoDTO> exeSummaryV2() {
        return kanbanService.getExeSummaryV2();
    }

    @GetMapping("/graph")
    public Object graph() {
        return kanbanService.getGraphData();
    }

    @GetMapping("/sync/data")
    public String syncData() {
        graphService.syncAllAppIdFromHalley();
        graphService.syncAppIdMappingFromForseti();
        return "done";
    }

    @GetMapping("/sync/halley")
    public String syncHalleyData() {
        graphService.syncAllAppIdFromHalley();
        return "done";
    }

    @GetMapping("/sync/forseti")
    public String syncForsetiData() {
        graphService.syncAppIdMappingFromForseti();
        return "done";
    }
}
