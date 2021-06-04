package io.metersphere.tuhu.controller;

import io.metersphere.service.CheckPermissionService;
import io.metersphere.track.request.testplan.AddTestPlanRequest;
import io.metersphere.track.service.TestPlanService;
import io.metersphere.tuhu.dto.IDsInfoDTO;
import io.metersphere.tuhu.service.TuhuIntegrationService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/tuhu/integration")
public class TuhuIntegrationController {

    @Resource
    private TuhuIntegrationService tuhuIntegrationService;

    @Resource
    private CheckPermissionService checkPermissionService;

    @PostMapping("/idbyname")
    public IDsInfoDTO getIDs(@RequestBody NamesRequest request) {
        return tuhuIntegrationService.getIdByName(request.getOrgName(), request.getWsName(), request.getProjName(), request.getPlanName());
    }

    @PostMapping("/createplanbyname")
    public IDsInfoDTO createPlanByName(@RequestBody NamesRequest request) {
        return tuhuIntegrationService.createPlanByNames(request.getOrgName(), request.getWsName(), request.getProjName(), request.getPlanName());
    }
}
