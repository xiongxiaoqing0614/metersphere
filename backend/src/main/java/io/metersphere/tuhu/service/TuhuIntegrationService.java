package io.metersphere.tuhu.service;

import io.metersphere.base.domain.Project;
import io.metersphere.base.domain.ProjectExample;
import io.metersphere.base.domain.TestPlan;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.base.mapper.TestPlanMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanMapper;
import io.metersphere.commons.constants.TestPlanStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.i18n.Translator;
import io.metersphere.service.IssueTemplateService;
import io.metersphere.service.TestCaseTemplateService;
import io.metersphere.track.request.testplan.AddTestPlanRequest;
import io.metersphere.track.service.TestPlanService;
import io.metersphere.tuhu.dto.IDsInfoDTO;
import io.metersphere.tuhu.mapper.TuhuIntegrationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class TuhuIntegrationService {

    @Resource
    private ProjectMapper projectMapper;

    @Resource
    TestCaseTemplateService caseTemplateService;

    @Resource
    private IssueTemplateService issueTemplateService;

    @Resource
    private ExtTestPlanMapper extTestPlanMapper;

    @Resource
    private TestPlanService testPlanService;

    @Resource
    private TuhuIntegrationMapper tuhuIntegrationMapper;

    @Resource
    TestPlanMapper testPlanMapper;

    public String getOrgIdByName(String orgName)
    {
        return tuhuIntegrationMapper.getOrgIdByName(orgName);
    }

    public List<String> getWsIdByName(String wsName)
    {
        return tuhuIntegrationMapper.getWsIdsByName(wsName);
    }

    public List<String> getProjIdsByName(String projName)
    {
        return tuhuIntegrationMapper.getProjIdsByName(projName);
    }

    public List<String> getPlanIdsByName(String planName)
    {
        return tuhuIntegrationMapper.getPlanIdsByName(planName);
    }

    public boolean hasTestPlanByName(String orgName, String wsName, String projName, String planName) {
        List<TestPlan> plans = tuhuIntegrationMapper.getPlansByName(planName);
        if(plans.size() < 1)
            return false;
        String projId;
        String wsId;
        if(orgName != null) {
            String orgId = tuhuIntegrationMapper.getOrgIdByName(orgName);
            if(orgId == null)
            {
                MSException.throwException(Translator.get("org_name_not_exist"));
            }
            wsId = tuhuIntegrationMapper.getWsIdByNameOrgId(wsName, orgId);
            if(wsId == null)
            {
                MSException.throwException(Translator.get("workspace_name_not_exist"));
            }
            projId = tuhuIntegrationMapper.getProjIdByNameWsId(projName, wsId);
            if(projId == null)
            {
                return false;
            }
        }else{
            List<String> wsIds = tuhuIntegrationMapper.getWsIdsByName(wsName);
            if(wsIds.size() != 1)
            {
                MSException.throwException(Translator.get("multiple_workspace_found_with_same_name"));
            }
            projId = tuhuIntegrationMapper.getProjIdByNameWsId(projName, wsIds.get(0));
            if(projId == null)
            {
                return false;
            }
        }

        for (TestPlan plan : plans) {
            if(plan.getProjectId().equalsIgnoreCase(projId))
                return true;
        }
        return false;
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

    public IDsInfoDTO createPlanByNames(String orgName, String wsName, String projName, String planName){
        IDsInfoDTO returnInfo = new IDsInfoDTO();
        AddTestPlanRequest testPlanRequest = new AddTestPlanRequest();
        String orgId = null;
        String wsId = null;
        String projId = null;
        if(orgName != null) {
            orgId = getOrgIdByName(orgName);
            if(orgId == null){
                MSException.throwException(Translator.get("org_name_not_exist"));
            }
            returnInfo.setOrgId(orgId);
            wsId = tuhuIntegrationMapper.getWsIdByNameOrgId(wsName, orgId);
            if(wsId == null){
                MSException.throwException(Translator.get("ws_name_not_exist"));
            }
        }else{
            List<String> wsIds = getWsIdByName(wsName);
            if(wsIds.size() > 1){
                MSException.throwException(Translator.get("multi_ws_found"));
            }else if(wsIds.size() == 0){
                MSException.throwException(Translator.get("ws_name_not_exist"));
            }else{
                wsId = wsIds.get(0);
            }
        }

        projId = tuhuIntegrationMapper.getProjIdByNameWsId(projName, wsId);
        //create proj if it does not exist
        if(projId == null){
            Project project = new Project();
            ProjectExample example = new ProjectExample();
            example.createCriteria()
                    .andWorkspaceIdEqualTo(wsId)
                    .andNameEqualTo(projName);
            if (projectMapper.countByExample(example) > 0) {
                MSException.throwException(Translator.get("project_name_already_exists"));
            }
            project.setId(UUID.randomUUID().toString());
            long createTime = System.currentTimeMillis();
            project.setCreateTime(createTime);
            project.setUpdateTime(createTime);
            // set workspace id
            project.setWorkspaceId(wsId);

            project.setCaseTemplateId(caseTemplateService.getDefaultTemplate(wsId).getId());
            project.setIssueTemplateId(issueTemplateService.getDefaultTemplate(wsId).getId());
            project.setProtocal("http");
            project.setName(projName);
            projectMapper.insertSelective(project);
            projId = project.getId();
        }else{
            //check if exist plan
            List<TestPlan> plans = tuhuIntegrationMapper.getPlansByName(planName);
            for (TestPlan plan : plans) {
                if(plan.getProjectId().equalsIgnoreCase(projId))
                    MSException.throwException(Translator.get("plan_name_already_exists"));
            }
        }

        String testPlanId = UUID.randomUUID().toString();
        testPlanRequest.setId(testPlanId);
        testPlanRequest.setStatus(TestPlanStatus.Prepare.name());
        testPlanRequest.setCreateTime(System.currentTimeMillis());
        testPlanRequest.setUpdateTime(System.currentTimeMillis());
        //use user "Administrator".
        String userId = tuhuIntegrationMapper.getUserIdByName("Administrator");
        testPlanRequest.setCreator(userId);
        testPlanRequest.setProjectId(projId);
        testPlanRequest.setWorkspaceId(wsId);
        testPlanRequest.setName(planName);
        testPlanRequest.setStage("smoke");
        testPlanRequest.setDescription("Created by API");
        testPlanRequest.setPrincipal("admin");
        testPlanMapper.insert(testPlanRequest);
        returnInfo.setProjId(projId);
        returnInfo.setWsId(wsId);
        returnInfo.setPlanId(testPlanId);
        return returnInfo;
    }
}
