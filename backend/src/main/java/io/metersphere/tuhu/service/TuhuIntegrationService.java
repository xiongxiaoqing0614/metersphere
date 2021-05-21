package io.metersphere.tuhu.service;

import io.metersphere.base.domain.TestPlan;
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
import java.util.ArrayList;
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

    public String getOrgIdByName(String orgName)
    {
        return tuhuIntegrationMapper.getOrgIdByName(orgName);
    }
    public List<String> getWsIdByName(String wsName)
    {
        return tuhuIntegrationMapper.getWsIdsByName(wsName);
    }
    public List<String> getProjIdByName(String projName)
    {
        return tuhuIntegrationMapper.getProjIdsByName(projName);
    }
    public List<String> getPlanIdByName(String planName) {
        return tuhuIntegrationMapper.getPlanIdsByName(planName);
    }

    public IDsInfoDTO getIdByName(String orgName, String wsName, String projName, String planName)
    {
        IDsInfoDTO returnInfo = new IDsInfoDTO();
        if (orgName != null) {
            returnInfo.setOrgId(tuhuIntegrationMapper.getOrgIdByName(orgName));
            returnInfo.setWsId(tuhuIntegrationMapper.getWsIdByNameOrgId(wsName, returnInfo.getOrgId()));
            returnInfo.setProjId(tuhuIntegrationMapper.getProjIdByNameWsId(projName, returnInfo.getWsId()));
            returnInfo.setPlanId(tuhuIntegrationMapper.getPlanIdByNameProjId(planName, returnInfo.getProjId()));
            return returnInfo;
        } else {
            List<TestPlan> planList = tuhuIntegrationMapper.getPlansByName(planName);
            if(planList.size() == 0){
                //no plan found with plan name, return error
                returnInfo.setPlanId("no records found with the given plan name");
                returnInfo.setProjId("no records found with the given plan name");
                returnInfo.setWsId("no records found with the given plan name");
                returnInfo.setOrgId("no records found with the given plan name");
                return returnInfo;
            }

            if(planList.size() == 1){
                TestPlan plan = planList.get(0);
                returnInfo.setPlanId(plan.getId());
                returnInfo.setProjId(plan.getProjectId());
                returnInfo.setWsId(plan.getWorkspaceId());
                returnInfo.setOrgId(tuhuIntegrationMapper.getOrgIdByWsId(plan.getWorkspaceId()));
                return returnInfo;
            }

            List<String> wsList = tuhuIntegrationMapper.getWsIdsByName(wsName);
            List<String> projList = tuhuIntegrationMapper.getProjIdsByName(projName);
            if(wsList.size() == 0 && projList.size() == 0) {
                //multiple records found and we can't get one id, return error
                returnInfo.setPlanId("multiple records found");
                returnInfo.setProjId("multiple records found");
                returnInfo.setWsId("multiple records found");
                returnInfo.setOrgId("multiple records found");
                return returnInfo;
            }
            List<TestPlan> tmpPlanList = new ArrayList<>();
            if(projList.size() > 0){
                for (String projId : projList){
                    for (TestPlan plan : planList){
                        if(plan.getProjectId().equalsIgnoreCase(projId)) {
                            tmpPlanList.add(plan);
                        }
                    }
                }
            }
            if(tmpPlanList.size() == 1){
                returnInfo.setPlanId(tmpPlanList.get(0).getId());
                returnInfo.setProjId(tmpPlanList.get(0).getProjectId());
                returnInfo.setWsId(tmpPlanList.get(0).getWorkspaceId());
                returnInfo.setOrgId(tuhuIntegrationMapper.getOrgIdByWsId(tmpPlanList.get(0).getWorkspaceId()));
                return returnInfo;
            }
            if(tmpPlanList.size() == 0)
                tmpPlanList = planList;

            returnInfo.setOrgId("-1");
            if(wsList.size() > 0) {
                for (String wsId : wsList) {
                    for (TestPlan plan : tmpPlanList) {
                        if (plan.getWorkspaceId().equalsIgnoreCase(wsId)) {
                            if(!returnInfo.getOrgId().equalsIgnoreCase("-1")){
                                //multiple records found, return error.
                                returnInfo.setPlanId("multiple records found");
                                returnInfo.setProjId("multiple records found");
                                returnInfo.setWsId("multiple records found");
                                return returnInfo;
                            }
                            returnInfo.setPlanId(plan.getId());
                            returnInfo.setProjId(plan.getProjectId());
                            returnInfo.setWsId(plan.getWorkspaceId());
                            returnInfo.setOrgId(tuhuIntegrationMapper.getOrgIdByWsId(plan.getWorkspaceId()));
                        }
                    }
                }
            }
        }
        return returnInfo;
    }
}
