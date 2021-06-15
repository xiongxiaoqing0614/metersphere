package io.metersphere.tuhu.service;

import io.metersphere.api.dto.datacount.ApiDataCountResult;
import io.metersphere.api.dto.datacount.response.ApiDataCountDTO;
import io.metersphere.api.service.ApiAutomationService;
import io.metersphere.api.service.ApiDefinitionService;
import io.metersphere.api.service.ApiTestCaseService;
import io.metersphere.base.domain.TestPlanReportExample;
import io.metersphere.base.mapper.TestPlanReportMapper;
import io.metersphere.base.mapper.ext.ExtApiDefinitionMapper;
import io.metersphere.commons.utils.DateUtils;
import io.metersphere.tuhu.dto.ExecutionAllInfoDTO;
import io.metersphere.tuhu.dto.TestCaseAllInfoDTO;
import io.metersphere.tuhu.dto.TestCaseSummaryDTO;
import io.metersphere.tuhu.mapper.KanbanMapper;
import io.metersphere.track.dto.TestPlanDTOWithMetric;
import io.metersphere.track.request.testcase.QueryTestPlanRequest;
import io.metersphere.track.service.TestPlanService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.metersphere.base.mapper.ext.ExtTestPlanMapper;


import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class KanbanService {
    @Resource
    private KanbanMapper kanbanMapper;

    @Resource
    private ExtApiDefinitionMapper extApiDefinitionMapper;

    @Resource
    private ApiDefinitionService apiDefinitionService;

    @Resource
    private ApiTestCaseService apiTestCaseService;

    @Resource
    private ApiAutomationService apiAutomationService;

    @Resource
    private ExtTestPlanMapper extTestPlanMapper;

    @Resource
    private TestPlanService testPlanService;

    @Resource
    private TestPlanReportMapper testPlanReportMapper;

    public List<TestCaseAllInfoDTO> getSummary() {
        List<TestCaseSummaryDTO> summaryList = kanbanMapper.getSummary();
        List<TestCaseAllInfoDTO> allInfoList = new ArrayList<TestCaseAllInfoDTO>();
        for(TestCaseSummaryDTO summaryData : summaryList) {
            TestCaseAllInfoDTO allInfo = new TestCaseAllInfoDTO();
            BeanUtils.copyProperties(summaryData, allInfo);
            String projectId = summaryData.getProjectId();
            long dateCountByCreateInThisWeek = apiDefinitionService.countByProjectIDAndCreateInThisWeek(projectId);
            allInfo.setApiCountThisWeek(dateCountByCreateInThisWeek);
            long dateP0CountByCreateInThisWeek = countByProjectIDAndTagAndCreateInThisWeek(projectId, "P0");
            allInfo.setP0APICountThisWeek(dateP0CountByCreateInThisWeek);

            ApiDataCountDTO apiCountResult = new ApiDataCountDTO();

            List<ApiDataCountResult> countResultByStatelList = apiDefinitionService.countStateByProjectIDNoP4(projectId);
            apiCountResult.countStatus(countResultByStatelList);
            allInfo.setCompletedAPICount(apiCountResult.getFinishedCount());
            allInfo.setNonP0APICount(allInfo.getApiCount() - allInfo.getP0APICount());

            ApiDataCountDTO p0ApiCountResult = new ApiDataCountDTO();
            List<ApiDataCountResult> p0CountResultByStatelList = apiDefinitionService.countStateByProjectIDP0(projectId);
            p0ApiCountResult.countStatus(p0CountResultByStatelList);
            allInfo.setCompletedP0APICount(p0ApiCountResult.getFinishedCount());

            long dateSingleCountByCreateInThisWeek = apiTestCaseService.countByProjectIDAndCreateInThisWeek(projectId);
            allInfo.setSingleCountThisWeek(dateSingleCountByCreateInThisWeek);

            long dateScenarioCountByCreateInThisWeek = apiAutomationService.countScenarioByProjectIDAndCreatInThisWeek(projectId);
            allInfo.setScenarioCountThisWeek(dateScenarioCountByCreateInThisWeek);

            allInfoList.add(allInfo);
        }
        return allInfoList;
    }

    /**
     * 统计本周创建的数据总量
     *
     * @param projectId
     * @return
     */
    public long countByProjectIDAndTagAndCreateInThisWeek(String projectId, String tag) {
        Map<String, Date> startAndEndDateInWeek = DateUtils.getWeedFirstTimeAndLastTime(new Date());

        Date firstTime = startAndEndDateInWeek.get("firstTime");
        Date lastTime = startAndEndDateInWeek.get("lastTime");

        if (firstTime == null || lastTime == null) {
            return 0;
        } else {
            return extApiDefinitionMapper.countByProjectIDAndTagAndCreateInThisWeek(projectId, tag, firstTime.getTime(), lastTime.getTime());
        }
    }

    public List<ExecutionAllInfoDTO> getExeSummary() {
        List<TestCaseSummaryDTO> summaryList = kanbanMapper.getSummary();
        List<ExecutionAllInfoDTO> testPlans = new ArrayList<ExecutionAllInfoDTO>();
        for(TestCaseSummaryDTO summaryData : summaryList) {
            QueryTestPlanRequest request = new QueryTestPlanRequest();
            request.setProjectId(summaryData.getProjectId());
            List<TestPlanDTOWithMetric> oriTestPlans = extTestPlanMapper.list(request);
            if(oriTestPlans.size() == 0)
                continue;
            testPlanService.calcTestPlanRate(oriTestPlans);
            for(TestPlanDTOWithMetric oriTestPlan : oriTestPlans) {
                TestPlanReportExample example = new TestPlanReportExample();
                example.createCriteria().andTestPlanIdEqualTo(oriTestPlan.getId());
                oriTestPlan.setExecutionTimes((int) testPlanReportMapper.countByExample(example));
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
