package io.metersphere.tuhu.controller;

import io.metersphere.service.CheckPermissionService;
import io.metersphere.tuhu.dto.IDsInfoDTO;
import io.metersphere.tuhu.service.TuhuIntegrationService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/tuhu/integration")
public class TuhuIntegrationController {

    @Resource
    private TuhuIntegrationService tuhuIntegrationService;

    @Resource
    private CheckPermissionService checkPermissionService;

    @PostMapping("/idbyname")
    public IDsInfoDTO getIDs(@RequestBody GetIDsRequest request) {
        return tuhuIntegrationService.getIdByName(request.getOrgName(), request.getWsName(), request.getProjName(), request.getPlanName());
    }
}
