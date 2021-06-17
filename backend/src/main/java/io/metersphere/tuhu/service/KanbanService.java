package io.metersphere.tuhu.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.metersphere.api.jmeter.RequestResult;
import io.metersphere.api.jmeter.ScenarioResult;
import io.metersphere.api.jmeter.TestResult;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import io.metersphere.base.mapper.ApiScenarioMapper;
import io.metersphere.api.dto.datacount.ApiDataCountResult;
import io.metersphere.api.dto.datacount.response.ApiDataCountDTO;
import io.metersphere.api.service.ApiAutomationService;
import io.metersphere.api.service.ApiDefinitionService;
import io.metersphere.api.service.ApiTestCaseService;
import io.metersphere.base.domain.TestPlanReportExample;
import io.metersphere.base.mapper.TestPlanReportMapper;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.DateUtils;
import io.metersphere.track.dto.TestCaseReportAdvanceStatusResultDTO;
import io.metersphere.track.dto.TestCaseReportMetricDTO;
import io.metersphere.track.dto.TestCaseReportStatusResultDTO;
import io.metersphere.tuhu.dto.*;
import io.metersphere.base.mapper.ext.ExtApiDefinitionMapper;
import io.metersphere.tuhu.mapper.KanbanMapper;
import io.metersphere.track.dto.TestPlanDTOWithMetric;
import io.metersphere.track.request.testcase.QueryTestPlanRequest;
import io.metersphere.track.service.TestPlanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.metersphere.base.mapper.ext.ExtTestPlanMapper;


import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.IOException;
import java.util.Map;

import static io.metersphere.tuhu.service.TuhuService.restApiPost;

@Service
@Transactional(rollbackFor = Exception.class)
public class KanbanService {
    @Value("${daily.avg.pass.rate.server.url.prefix}")
    private String dailyAvgPassRateServerUrlPrefix;

    @Resource
    private TuhuService tuhuService;

    @Resource
    private ApiScenarioMapper apiScenarioMapper;

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
            Map<String, Double> dailyAvgPassRateMap = this.getDailyAvgPassRate(oriTestPlans);
            for(TestPlanDTOWithMetric oriTestPlan : oriTestPlans) {
                TestPlanReportExample example = new TestPlanReportExample();
                example.createCriteria().andTestPlanIdEqualTo(oriTestPlan.getId());
                oriTestPlan.setExecutionTimes((int) testPlanReportMapper.countByExample(example));
                ExecutionAllInfoDTO thTestPlan = new ExecutionAllInfoDTO();
                BeanUtils.copyProperties(oriTestPlan, thTestPlan);
                thTestPlan.setDepartment(summaryData.getDepartment());
                thTestPlan.setTeam(summaryData.getTeam());
                if (dailyAvgPassRateMap != null) {
                    thTestPlan.setDailyAvgPassRate(dailyAvgPassRateMap.get(thTestPlan.getId()));
                }
                testPlans.add(thTestPlan);
            }
        }
        return testPlans;
    }

    private Map<String, Double> getDailyAvgPassRate(List<TestPlanDTOWithMetric> testPlans) {
        JSONArray ja = new JSONArray();
        testPlans.forEach(testPlan -> {
            ja.add(testPlan.getId());
        });

        JSONObject jo = new JSONObject();
        jo.put("times", 0);
        jo.put("days", 7);  // 拉取前7天的数据
        jo.put("testplanIds", ja);

        JSONArray result = this.fetchDailyAvgPassData(JSONArray.toJSONString(jo));
        if (result == null) { return null; }

        Map<String, Double> map = new HashMap<>();
        for (Object j : result) {
            JSONObject t = (JSONObject)j;
            map.put(t.getString("testplanId"), t.getDouble("passRateAvg"));
        }
        return map;
    }

    private JSONArray fetchDailyAvgPassData(String js) {
        String dailyAvgPassRateServerUrl = String.format("%s/Monitor_API/passRate/avg", dailyAvgPassRateServerUrlPrefix);
        String result = TuhuService.restApiPost(dailyAvgPassRateServerUrl, js);
        if (result == null) { return null; }

        JSONObject jo = JSONObject.parseObject(result);
        if (jo == null || !jo.getBoolean("success")) {
            return null;
        }
        return jo.getJSONArray("data");
    }

    public void postTestResult(TestResult testResult, String reportId, String scenarioReportId) throws IOException {
        if (testResult.getScenarios().size() == 0) {
            return;
        }

        String url = "";
        JSONObject jo = null;
        ScenarioResult sr = testResult.getScenarios().get(0);

        if (sr.getRequestResults().size() == 1) { // 单接口
            LogUtil.info("reportId: " + reportId);
            jo = makeTestResultDataObj(testResult, reportId);
            RequestResult rr = sr.getRequestResults().get(0);
            LogUtil.info("apiSourceId: " + rr.getName());
            TestCaseExtDTO testCase = kanbanMapper.queryTestCaseInfoBySourceId(rr.getName());

            url = dailyAvgPassRateServerUrlPrefix + "/Monitor_API/write_single_interface_record";
            jo.put("returnStatusCode", rr.getResponseResult().getResponseCode());

            if (testCase != null) {
                jo.put("returnValue", rr.getResponseResult().getBody());
                jo.put("caseName", testCase.getName());
                jo.put("caseLeader", testCase.getCreateUser());

                if (testCase.getPriority().equals("P0")) {
                    jo.put("iskernel", 1);
                } else {
                    jo.put("iskernel", 0);
                }

                jo.put("interfaceName", testCase.getApiName());
                jo.put("interfaceDescription", testCase.getApiDesc());
            }
        } else {    // 场景
            LogUtil.info("scenarioReportId: " + scenarioReportId);
            String testPlanReportId = kanbanMapper.queryTestPlanReportIdByScenarioReportId(scenarioReportId);
            LogUtil.info("reportId: " + testPlanReportId);
            jo = makeTestResultDataObj(testResult, testPlanReportId);
            String scenarioId = testResult.getTestId();
            LogUtil.info("scenarioId: " + scenarioId);
            ApiScenarioWithBLOBs apiScenarioWithBLOBs = apiScenarioMapper.selectByPrimaryKey(scenarioId);

            url = dailyAvgPassRateServerUrlPrefix + "/Monitor_API/write_case_exe_record";
            jo.put("caseName", apiScenarioWithBLOBs.getName());
            jo.put("caseLeader", apiScenarioWithBLOBs.getCreateUser());
            jo.put("failureCause", "");
            jo.put("imageUrl", "");
            jo.put("deviceSN", "V1.10");
            jo.put("deviceName", "metersphere");
            jo.put("interfaceName", "");
            jo.put("interfaceDescription", "");
            jo.put("iskernel", 1);
        }

        JSONArray ja = new JSONArray();
        ja.add(jo);
        String rep = restApiPost(url, JSONObject.toJSONString(ja));
        if (rep == null) { return; }

        JSONObject repObj = JSONObject.parseObject(rep);
        if (repObj == null || !repObj.getBoolean("success")) {
            LogUtil.error("上传测试结果失败: " + rep);
        }
    }

    private JSONObject makeTestResultDataObj(TestResult testResult, String testPlanReportId) {
        LogUtil.info(JSONObject.toJSONString(testResult, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteDateUseDateFormat));
        ScenarioResult sr = testResult.getScenarios().get(0);
        JSONObject jo = new JSONObject();
        TestPlanReportExtDTO testPlanReportExtDTO = kanbanMapper.queryTestPlanInfoByReportId(testPlanReportId);
        if (testPlanReportExtDTO == null) { return jo; }

        jo.put("groupName", testPlanReportExtDTO.getGroupName());
        jo.put("projectName", testPlanReportExtDTO.getProjectName());
        jo.put("testUse", "回归");
        jo.put("casePurpose", "自动化测试");
        jo.put("caseKind", "INTERFACE");

        if (sr.getSuccess() == sr.getTotal()) {
            jo.put("result", 0);
        } else {
            jo.put("result", 2);
        }

        jo.put("timeCost", sr.getResponseTime());
        jo.put("execTool", "metersphere");
        jo.put("timeFlag", testPlanReportId);
        jo.put("testplanId", testPlanReportExtDTO.getTestPlanId());
        jo.put("testplanReportId", testPlanReportId);
        jo.put("updateDate", (new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss")).format(new Date()));

        return jo;
    }

    public void postCountResult(TestCaseReportMetricDTO testCaseReportMetricDTO, String testPlanReportId) {
        LogUtil.info("report id: " + testPlanReportId);
        String url = dailyAvgPassRateServerUrlPrefix + "/Monitor_API/write_cases_count_record";
        TestCaseReportAdvanceStatusResultDTO executeResult = testCaseReportMetricDTO.getExecuteResult();
        TestPlanReportExtDTO testPlanReportExtDTO = kanbanMapper.queryTestPlanInfoByReportId(testPlanReportId);

        JSONObject jo = new JSONObject();
        jo.put("groupName", testPlanReportExtDTO.getGroupName());
        jo.put("projectName", testCaseReportMetricDTO.getProjectName());
        jo.put("testUse", "回归");
        jo.put("caseKind", "接口测试");
        jo.put("timeCost", testPlanReportExtDTO.getEndTime() - testPlanReportExtDTO.getStartTime());
        jo.put("deviceSN", "v1.10");
        jo.put("deviceName", "metersphere");
        jo.put("timeFlag", testPlanReportId);

        int success = 0;
        int failure = 0;
        int single = 0;
        int link = 0;
        for (TestCaseReportStatusResultDTO item : executeResult.getApiResult()) {
            if (item.getStatus().equals("Pass")) {
                success += item.getCount();
            } else {
                failure += item.getCount();
            }
            single += item.getCount();
        }

        for (TestCaseReportStatusResultDTO item : executeResult.getScenarioResult()) {
            if (item.getStatus().equals("Pass")) {
                success += item.getCount();
            } else {
                failure += item.getCount();
            }
            link += item.getCount();
        }

        jo.put("successCount", success);
        jo.put("failureCount", failure);
        jo.put("singleCount", single);
        jo.put("linkCount", link);

        jo.put("testplanName", testPlanReportExtDTO.getTestPlanName());
        jo.put("testplanId", testPlanReportExtDTO.getTestPlanId());
        jo.put("testplanReportId", testPlanReportId);
        jo.put("updateDate", (new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss")).format(new Date()));

        JSONArray ja = new JSONArray();
        ja.add(jo);

        String rep = restApiPost(url, JSONObject.toJSONString(ja));
        if (rep == null) { return; }

        JSONObject repObj = JSONObject.parseObject(rep);
        if (repObj == null || !repObj.getBoolean("success")) {
            LogUtil.error("上传统计结果失败: " + rep);
        }
    }
}
