package io.metersphere.tuhu.service;

import io.metersphere.base.mapper.ext.ExtTestPlanMapper;
import io.metersphere.service.OrganizationService;
import io.metersphere.service.ProjectService;
import io.metersphere.service.WorkspaceService;
import io.metersphere.track.service.TestPlanService;
import io.metersphere.tuhu.dto.IDsInfoDTO;
import io.metersphere.tuhu.mapper.TuhuIntegrationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class TuhuIntegrationService {
    @Resource
    private OrganizationService organizationService;

    @Resource
    private WorkspaceService workspaceService;

    @Resource
    private ProjectService projectService;

    @Resource
    private ExtTestPlanMapper extTestPlanMapper;

    @Resource
    private TestPlanService testPlanService;

    @Resource
    private TuhuIntegrationMapper tuhuIntegrationMapper;

    public String getOrgIdByName(String orgName) {
        return tuhuIntegrationMapper.getOrgIdByName(orgName);
    }
    public String getWSIdByName(String wsName) {
        return tuhuIntegrationMapper.getWSIdByName(wsName);
    }
    public List<String> getProjIdByName(String projName) {
        return tuhuIntegrationMapper.getProjIdByName(projName);
    }
    public List<String> getPlanIdByName(String planName) {
        return tuhuIntegrationMapper.getPlanIdByName(planName);
    }
    public List<String> getPlanIdByName() {
        return tuhuIntegrationMapper.getPlanIDs();
    }
    public IDsInfoDTO getIdByName(String wsName, String projName, String planName) {
        IDsInfoDTO returnInfo = new IDsInfoDTO();
        returnInfo.setOrgId("-1");
        List<String> planIds = getPlanIdByName();
        List<String> planId = getPlanIdByName(planName);
        returnInfo.setPlanId(planId.get(0));
        returnInfo.setWsId(getWSIdByName(wsName));
        returnInfo.setProId(getProjIdByName(projName).get(0));

        return returnInfo;
    }
}
