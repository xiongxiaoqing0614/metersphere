package io.metersphere.tuhu.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.metersphere.api.dto.automation.ScenarioStatus;
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
import io.metersphere.commons.constants.TestPlanTestCaseStatus;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.DateUtils;
import io.metersphere.commons.utils.MathUtils;
import io.metersphere.track.dto.TestCaseReportAdvanceStatusResultDTO;
import io.metersphere.track.dto.TestCaseReportMetricDTO;
import io.metersphere.track.dto.TestCaseReportStatusResultDTO;
import io.metersphere.tuhu.dto.*;
import io.metersphere.base.mapper.ext.ExtApiDefinitionMapper;
import io.metersphere.tuhu.mapper.KanbanMapper;
import io.metersphere.track.dto.TestPlanDTOWithMetric;
import io.metersphere.track.request.testcase.QueryTestPlanRequest;
import io.metersphere.track.service.TestPlanService;
import org.apache.commons.lang3.StringUtils;
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
import java.util.stream.Collectors;

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

    @Resource
    private GraphService graphService;

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


    public List<TestCaseAllInfoDTO> getSummaryV2() {

        Map<String, Date> startAndEndDateInWeek = DateUtils.getWeedFirstTimeAndLastTime(new Date());

        Date firstTime = startAndEndDateInWeek.get("firstTime");
        Date lastTime = startAndEndDateInWeek.get("lastTime");


        List<TestCaseSummaryDTO> summaryList = kanbanMapper.getSummary();

        if (summaryList == null || summaryList.isEmpty())
            return new ArrayList<>();

        List<TestCaseAllInfoDTO> allInfoList = new ArrayList<>();

        //统计本周创建的数据总量
        boolean isDateCountByCreateInThisWeeksEmpty = true;
        List<ProjectIdAndCount> dateCountByCreateInThisWeeks = new ArrayList<>();
        if (firstTime != null && lastTime != null) {
            dateCountByCreateInThisWeeks = kanbanMapper.countByProjectIDAndCreateInThisWeek(firstTime.getTime(), lastTime.getTime());
            if (dateCountByCreateInThisWeeks != null && !dateCountByCreateInThisWeeks.isEmpty()) {
                isDateCountByCreateInThisWeeksEmpty = false;
            }
        }


        boolean isdateP0CountByCreateInThisWeeksEmpty = true;
        List<ProjectIdAndCount> dateP0CountByCreateInThisWeeks = new ArrayList<>();
        if (firstTime != null && lastTime != null) {
            dateP0CountByCreateInThisWeeks = kanbanMapper.countByProjectIDAndTagAndCreateInThisWeek("P0", firstTime.getTime(), lastTime.getTime());
            if (dateP0CountByCreateInThisWeeks != null && !dateP0CountByCreateInThisWeeks.isEmpty()) {
                isdateP0CountByCreateInThisWeeksEmpty = false;
            }
        }


        List<ProjectIdAndCountGroup> p4List;
        p4List = kanbanMapper.countByProjectIDAndTagAndCreateInThisWeekP4();


        List<ProjectIdAndCountGroup> p0List;
        p0List = kanbanMapper.countByProjectIDAndTagAndCreateInThisWeekP0();


        boolean iscountByProjectIDAndCreateInThisWeeksEmpty = true;
        List<ProjectIdAndCount> countByProjectIDAndCreateInThisWeeks = new ArrayList<>();
        if (firstTime != null && lastTime != null) {
            countByProjectIDAndCreateInThisWeeks = kanbanMapper.testCaseCountByProjectIDAndCreateInThisWeek(firstTime.getTime(), lastTime.getTime());
            if (countByProjectIDAndCreateInThisWeeks != null && !countByProjectIDAndCreateInThisWeeks.isEmpty()) {
                iscountByProjectIDAndCreateInThisWeeksEmpty = false;
            }
        }


        boolean iscountByProjectIDAndCreatInThisWeekEmpty = true;
        List<ProjectIdAndCount> countByProjectIDAndCreatInThisWeeks = new ArrayList<>();
        if (firstTime != null && lastTime != null) {
            countByProjectIDAndCreatInThisWeeks = kanbanMapper.countByProjectIDAndCreatInThisWeek(firstTime.getTime(), lastTime.getTime());
            if (countByProjectIDAndCreatInThisWeeks != null && !countByProjectIDAndCreatInThisWeeks.isEmpty()) {
                iscountByProjectIDAndCreatInThisWeekEmpty = false;
            }
        }


        for (TestCaseSummaryDTO summaryData : summaryList) {
            TestCaseAllInfoDTO allInfo = new TestCaseAllInfoDTO();
            BeanUtils.copyProperties(summaryData, allInfo);
            String projectId = summaryData.getProjectId();

            //统计本周创建的数据总量
            if (!isDateCountByCreateInThisWeeksEmpty) {
                dateCountByCreateInThisWeeks.stream().filter(p -> p.getProjectId().equals(projectId)).findFirst().ifPresent(projectIdAndCount -> allInfo.setApiCountThisWeek(projectIdAndCount.getCountNumber()));
            }

            //统计本周创建的数据总量P0
            if (!isdateP0CountByCreateInThisWeeksEmpty) {
                dateP0CountByCreateInThisWeeks.stream().filter(d -> d.getProjectId().equals(projectId)).findFirst().ifPresent(projectIdAndCount -> allInfo.setP0APICountThisWeek(projectIdAndCount.getCountNumber()));
            }


            ApiDataCountDTO apiCountResult = new ApiDataCountDTO();
            if (p4List != null && !p4List.isEmpty()) {
                List<ProjectIdAndCountGroup> p4s = p4List.stream().filter(p->p.getProjectId().equals(projectId)).collect(Collectors.toList());
                if(!p4s.isEmpty()) {
                    apiCountResult.countStatusV2(p4s);
                }
            }
            allInfo.setCompletedAPICount(apiCountResult.getFinishedCount());
            allInfo.setNonP0APICount(allInfo.getApiCount() - allInfo.getP0APICount());


            ApiDataCountDTO p0ApiCountResult = new ApiDataCountDTO();
            if (p0List != null && !p0List.isEmpty()) {
                List<ProjectIdAndCountGroup> p0s = p0List.stream().filter(p -> p.getProjectId().equals(projectId)).collect(Collectors.toList());
                if (!p0s.isEmpty()) {
                    p0ApiCountResult.countStatusV2(p0s);
                }
            }
            allInfo.setCompletedP0APICount(p0ApiCountResult.getFinishedCount());


            if (!iscountByProjectIDAndCreateInThisWeeksEmpty) {
                countByProjectIDAndCreateInThisWeeks.stream().filter(c -> c.getProjectId().equals(projectId)).findFirst().ifPresent(projectIdAndCount -> allInfo.setSingleCountThisWeek(projectIdAndCount.getCountNumber()));
            }


            if (!iscountByProjectIDAndCreatInThisWeekEmpty) {
                countByProjectIDAndCreatInThisWeeks.stream().filter(c -> c.getProjectId().equals(projectId)).findFirst().ifPresent(projectIdAndCount -> allInfo.setScenarioCountThisWeek(projectIdAndCount.getCountNumber()));
            }


            allInfoList.add(allInfo);
        }
        return allInfoList;
    }

    public List<ExecutionAllInfoDTO> getExeSummaryV2() {
        List<TestCaseSummaryDTO> summaryList = kanbanMapper.getSummary();
        List<ExecutionAllInfoDTO> plans = new ArrayList<>();

        List<TestPlanDTOWithMetric> testPlans = extTestPlanMapper.list(new QueryTestPlanRequest());

        List<TestPlanIdAndCount> testPlanIdAndCounts = kanbanMapper.getTestPlanIdAndCount();

        List<PlanIdAndStatus> allTestPlanTestCases = kanbanMapper.getAllTestPlanTestCase();
        List<PlanIdAndStatus> allTestPlanApiCases = kanbanMapper.getAllTestPlanApiCase();
        List<PlanIdAndStatus> allTestPlanApiScenarios = kanbanMapper.getAllTestPlanApiScenario();
        List<PlanIdAndStatus> allTestPlanLoadCases = kanbanMapper.getAllTestPlanLoadCase();

        List<String> testPlanIds = new ArrayList<>();
        Map<String, List<TestPlanDTOWithMetric>> projectTestPlan = new HashMap<>();
        for (TestCaseSummaryDTO summaryData : summaryList) {
            List<TestPlanDTOWithMetric> prjTestPlans = testPlans.stream().filter(p -> p.getProjectId().equals(summaryData.getProjectId())).collect(Collectors.toList());
            if (prjTestPlans.isEmpty())
                continue;
            projectTestPlan.put(summaryData.getProjectId(), prjTestPlans);

            prjTestPlans.forEach(p->{
                testPlanIds.add(p.getId());
            });
         }


        Map<String, Double> dailyAvgPassRateMap = this.getDailyAvgPassRateV2(testPlanIds);

        for (Map.Entry<String, List<TestPlanDTOWithMetric>> entry : projectTestPlan.entrySet()) {

            List<TestPlanDTOWithMetric> prjTestPlans = entry.getValue();

            TestCaseSummaryDTO summaryData = summaryList.stream().filter(s -> s.getProjectId().equals(entry.getKey())).findFirst().orElse(null);


            for (TestPlanDTOWithMetric oriTestPlan : prjTestPlans) {

                List<PlanIdAndStatus> functionalExecResults = allTestPlanTestCases.stream().filter(f->f.getPlanId().equals(oriTestPlan.getId())).collect(Collectors.toList());
                List<PlanIdAndStatus> apiExecResults = allTestPlanApiCases.stream().filter(f->f.getPlanId().equals(oriTestPlan.getId())).collect(Collectors.toList());
                List<PlanIdAndStatus> scenarioExecResults = allTestPlanApiScenarios.stream().filter(f->f.getPlanId().equals(oriTestPlan.getId())).collect(Collectors.toList());
                List<PlanIdAndStatus> loadResults = allTestPlanLoadCases.stream().filter(f->f.getPlanId().equals(oriTestPlan.getId())).collect(Collectors.toList());
                calcTestPlanRateV2(oriTestPlan,functionalExecResults,apiExecResults,scenarioExecResults,loadResults);

                TestPlanIdAndCount testPlanIdAndCount = testPlanIdAndCounts.stream().filter(t -> t.getTestPlanId().equals(oriTestPlan.getId())).findFirst().orElse(null);

                oriTestPlan.setExecutionTimes(testPlanIdAndCount == null ? 0 : (int) testPlanIdAndCount.getCountNumber());

                ExecutionAllInfoDTO thTestPlan = new ExecutionAllInfoDTO();
                BeanUtils.copyProperties(oriTestPlan, thTestPlan);
                thTestPlan.setDepartment(summaryData == null ? "" : summaryData.getDepartment());
                thTestPlan.setTeam(summaryData == null ? "" : summaryData.getTeam());
                if (dailyAvgPassRateMap != null) {
                    thTestPlan.setDailyAvgPassRate(dailyAvgPassRateMap.get(thTestPlan.getId()));
                }
                plans.add(thTestPlan);
            }
        }
        return plans;
    }



    public void calcTestPlanRateV2( TestPlanDTOWithMetric  testPlan,
                                    List<PlanIdAndStatus> functionalExecResults,
                                    List<PlanIdAndStatus> apiExecResults,
                                    List<PlanIdAndStatus> scenarioExecResults,
                                    List<PlanIdAndStatus> loadResults
    ) {
        testPlan.setTested(0);
        testPlan.setPassed(0);
        testPlan.setTotal(0);

        functionalExecResults.forEach(item -> {
            if (!StringUtils.equals(item.getStatus(), TestPlanTestCaseStatus.Prepare.name()) &&
                    !StringUtils.equals(item.getStatus(), TestPlanTestCaseStatus.Underway.name())) {
                testPlan.setTested(testPlan.getTested() + 1);
                if (StringUtils.equals(item.getStatus(), TestPlanTestCaseStatus.Pass.name())) {
                    testPlan.setPassed(testPlan.getPassed() + 1);
                }
            }
        });


        apiExecResults.forEach(item -> {
            if (StringUtils.isNotBlank(item.getStatus())) {
                testPlan.setTested(testPlan.getTested() + 1);
                if (StringUtils.equals(item.getStatus(), "success")) {
                    testPlan.setPassed(testPlan.getPassed() + 1);
                }
            }
        });


        scenarioExecResults.forEach(item -> {
            if (StringUtils.isNotBlank(item.getStatus())) {
                testPlan.setTested(testPlan.getTested() + 1);
                if (StringUtils.equals(item.getStatus(), ScenarioStatus.Success.name())) {
                    testPlan.setPassed(testPlan.getPassed() + 1);
                }
            }
        });


        loadResults.forEach(item -> {
            if (StringUtils.isNotBlank(item.getStatus())) {
                testPlan.setTested(testPlan.getTested() + 1);
                if (StringUtils.equals(item.getStatus(), "success")) {
                    testPlan.setPassed(testPlan.getPassed() + 1);
                }
            }
        });

        testPlan.setTotal(apiExecResults.size() + scenarioExecResults.size() + functionalExecResults.size() + loadResults.size());
        testPlan.setPassRate(MathUtils.getPercentWithDecimal(testPlan.getTested() == 0 ? 0 : testPlan.getPassed() * 1.0 / testPlan.getTotal()));
        testPlan.setTestRate(MathUtils.getPercentWithDecimal(testPlan.getTotal() == 0 ? 0 : testPlan.getTested() * 1.0 / testPlan.getTotal()));

    }


    private Map<String, Double> getDailyAvgPassRateV2(List<String> testPlanIds) {
        JSONArray ja = new JSONArray();
        ja.addAll(testPlanIds);

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
        List<ExecutionAllInfoDTO> testPlans = new ArrayList<>();
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
        jo.put("caseKind", "INTERFACE");
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

    public JSONObject getGraphData() {
        JSONObject jo = new JSONObject();
        jo.put("serviceInData", graphService.getServiceInData());
        jo.put("teamInData", graphService.getTeamInData());
        jo.put("apiDoneData", graphService.getApiDoneData());
        jo.put("apiPassRateData", graphService.getApiPassRateData());
        jo.put("apiCovRateData", graphService.getApiCovRateData());
        jo.put("scenCovRateData", graphService.getScenCovRateData());
        jo.put("caseFailData", graphService.getCaseFailData());

        return jo;
    }

}
