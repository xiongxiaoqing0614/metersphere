package io.metersphere.tuhu.controller;

import io.metersphere.service.CheckPermissionService;
import io.metersphere.tuhu.dto.IDsInfoDTO;
import io.metersphere.tuhu.service.TuhuIntegrationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/tuhu/integration")
public class TuhuIntegrationController {

    @Resource
    private TuhuIntegrationService tuhuIntegrationService;

    @Resource
    private CheckPermissionService checkPermissionService;

    @GetMapping("/idbyname/{wsName}/{projName}/{planName}")
    public IDsInfoDTO getIDs(@PathVariable String wsName, @PathVariable String projName, @PathVariable String planName) {
        return tuhuIntegrationService.getIdByName(wsName, projName, planName);
    }

//    @GetMapping("ids/{orgName}/{wsName}/{projName}/{planName}")
//    public IDsInfoDTO getIDs(@PathVariable String orgName, @PathVariable String wsName, @PathVariable String projName, @PathVariable String planName) {
//        return tuhuIntegrationService.getIdByName(orgName, wsName, projName, planName);
//    }

}
