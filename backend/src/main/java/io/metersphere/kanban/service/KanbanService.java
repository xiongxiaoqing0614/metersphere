package io.metersphere.kanban.service;

import io.metersphere.base.domain.TestPlanReportExample;
import io.metersphere.base.mapper.TestPlanReportMapper;
import io.metersphere.kanban.dto.ExecutionAllInfoDTO;
import io.metersphere.kanban.dto.TestCaseSummaryDTO;
import io.metersphere.kanban.mapper.KanbanMapper;
import io.metersphere.track.dto.TestPlanDTOWithMetric;
import io.metersphere.track.request.testcase.QueryTestPlanRequest;
import io.metersphere.track.service.TestPlanService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.metersphere.base.mapper.ext.ExtTestPlanMapper;
import io.metersphere.track.service.TestPlanService;


import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class KanbanService {
    @Resource
    private KanbanMapper kanbanMapper;
    @Resource
    private ExtTestPlanMapper extTestPlanMapper;
    @Resource
    private TestPlanService testPlanService;
    @Resource
    private TestPlanReportMapper testPlanReportMapper;

    public List<TestCaseSummaryDTO> getSummary() {
        List<TestCaseSummaryDTO> list = kanbanMapper.getSummary();
        return list;
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
