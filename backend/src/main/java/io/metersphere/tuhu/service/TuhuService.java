package io.metersphere.tuhu.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import io.metersphere.base.domain.TestPlan;
import io.metersphere.base.mapper.ApiScenarioMapper;
import io.metersphere.base.mapper.TestPlanMapper;
import io.metersphere.base.mapper.TestPlanReportMapper;
import io.metersphere.tuhu.dto.TuhuCodeCoverageRateResultDTO;
import io.metersphere.tuhu.mapper.TuhuCodeCoverageRateMappingMapper;
import io.metersphere.tuhu.request.CodeCoverageBindRequest;
import io.metersphere.tuhu.request.CodeCoverageRequest;
import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.metersphere.commons.utils.LogUtil;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;


@Service
@Transactional(rollbackFor = Exception.class)
public class TuhuService {

    @Value("${coverage.rate.server.url.prefix}")
    private String codeCoverageRateServerUrlPrefix;

    @Resource
    private TuhuCodeCoverageRateMappingMapper tuhuCodeCoverageRateMappingMapper;

    @Resource
    private TestPlanMapper testPlanMapper;

    @Resource
    private TestPlanReportMapper testPlanReportMapper;

    @Resource
    private ApiScenarioMapper apiScenarioMapper;


    public Integer addCodeCoverageRateMapping(CodeCoverageBindRequest codeCoverageBind) {
        TuhuCodeCoverageRateResultDTO tuhuCodeCoverageRateResultDTO = new TuhuCodeCoverageRateResultDTO();
        tuhuCodeCoverageRateResultDTO.setAppId(codeCoverageBind.getAppId());
        tuhuCodeCoverageRateResultDTO.setBranchName(codeCoverageBind.getBranchName());
        tuhuCodeCoverageRateResultDTO.setCommitId(codeCoverageBind.getCommitId());
        tuhuCodeCoverageRateResultDTO.setStage(codeCoverageBind.getStage());
        tuhuCodeCoverageRateResultDTO.setTestReportId(codeCoverageBind.getTestReportId());

        TestPlan testPlan = testPlanMapper.selectByConditions(codeCoverageBind);
        if (testPlan == null) {
            return null;
        }
        tuhuCodeCoverageRateResultDTO.setTestPlanId(testPlan.getId());
        return tuhuCodeCoverageRateMappingMapper.insert(tuhuCodeCoverageRateResultDTO);
    }

    public List<TuhuCodeCoverageRateResultDTO> getCodeCoverageRateList(CodeCoverageRequest codeCoverageRequests) {
        if (codeCoverageRateServerUrlPrefix.isEmpty()) {
            throw new ValueException("未配置代码覆盖率服务URL前缀");
        }
        List<TuhuCodeCoverageRateResultDTO> mappingList = null;
        String[] testPlanIds = codeCoverageRequests.getTestPlanIds();
        String[] testReportIds = codeCoverageRequests.getTestReportIds();
        if (testPlanIds != null && testPlanIds.length > 0) {
            LogUtil.info("test plan id length: " + testPlanIds.length);
            mappingList = tuhuCodeCoverageRateMappingMapper.queryByTestPlanIds(testPlanIds);
        } else if (testReportIds != null && testReportIds.length > 0) {
            LogUtil.info("test report id length: " + testReportIds.length);
            mappingList = tuhuCodeCoverageRateMappingMapper.queryByTestReportIds(testReportIds);
        } else {
            LogUtil.info("testPlanIds or testReportIds is null");
            return null;
        }

        if (mappingList.isEmpty()) {
            return null;
        }

        JSONArray rjs = fetchCodeCoverageData(JSON.toJSONString(mappingList));
        if (rjs == null) { return null; }

        List<TuhuCodeCoverageRateResultDTO> lst = new ArrayList<>();
        for (Object j : rjs) {
            TuhuCodeCoverageRateResultDTO tuhuCodeCoverageRateResultDTO = new TuhuCodeCoverageRateResultDTO();
            JSONObject t = (JSONObject)j;
            tuhuCodeCoverageRateResultDTO.setTestPlanId(t.getString("testPlanId"));
            tuhuCodeCoverageRateResultDTO.setTestReportId(t.getString("testReportId"));
            tuhuCodeCoverageRateResultDTO.setCoverageRate(t.getFloat("coverageRate"));
            tuhuCodeCoverageRateResultDTO.setAppId(t.getString("appId"));
            tuhuCodeCoverageRateResultDTO.setBranchName(t.getString("branchName"));
            tuhuCodeCoverageRateResultDTO.setCommitId(t.getString("commitId"));
            tuhuCodeCoverageRateResultDTO.setStage(t.getString("stage"));
            lst.add(tuhuCodeCoverageRateResultDTO);
        }

        return lst;
    }

    private JSONArray fetchCodeCoverageData(String js) {
        String codeCoverageRateServerUrl = codeCoverageRateServerUrlPrefix + "/api/coverager/data";
        String result = restApiPost(codeCoverageRateServerUrl, js);
        if (result == null) { return null; }

        JSONObject jo = JSONObject.parseObject(result);
        if (jo == null || jo.getInteger("code") != 0) {
            return null;
        }

        return jo.getJSONArray("data");
    }

    public static String restApiPost(String url, String js) {
        LogUtil.info("post url: " + url);
        LogUtil.info("request json: " + js);

        try {
            SimpleClientHttpRequestFactory clientHttpRequestFactory
                    = new SimpleClientHttpRequestFactory();
            clientHttpRequestFactory.setConnectTimeout(2 * 1000);
            clientHttpRequestFactory.setReadTimeout(10 * 1000);

            RestTemplate client = new RestTemplate(clientHttpRequestFactory);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> requestEntity = new HttpEntity<>(js, headers);
            ResponseEntity<String> response = client.exchange(url, HttpMethod.POST, requestEntity, String.class);

            String result = response.getBody();
            LogUtil.info("pass rate response json: " + result);

            return result;
        } catch (Exception e) {
            LogUtil.error("网络访问错误！" + e.getMessage());
            return null;
        }

    }

    public static String restApiGet(String url, String params, Map<String, String> headers) {
        LogUtil.info("get url: " + url);
        LogUtil.info("request param: " + params);

        try {
            SimpleClientHttpRequestFactory clientHttpRequestFactory
                    = new SimpleClientHttpRequestFactory();
            clientHttpRequestFactory.setConnectTimeout(2 * 1000);
            clientHttpRequestFactory.setReadTimeout(10 * 1000);

            RestTemplate client = new RestTemplate(clientHttpRequestFactory);
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.TEXT_HTML);
            httpHeaders.setAll(headers);
            HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);
            ResponseEntity<String> response = client.exchange(String.format("%s?%s", url, params), HttpMethod.GET, httpEntity, String.class);

            return response.getBody();
        } catch (Exception e) {
            LogUtil.error("网络访问错误！" + e.getMessage());
            return null;
        }
    }

    public String getTestReportByTimestamp(CodeCoverageBindRequest codeCoverageBind) {
        return testPlanReportMapper.queryTestPlanReportId(codeCoverageBind);
    }

    public void redirectReportUrl(HttpServletResponse response, String appId, String branchName,
                                  String commitId, String stage, Boolean zip) throws IOException {
        StringBuilder sb = new StringBuilder(codeCoverageRateServerUrlPrefix);
        if (zip != null && zip) {  // 打包报告
            sb.append("/api/coverager/report");
        } else {
            sb.append("/api/coverager/report/html");
        }
        sb.append("?appId=");
        sb.append(appId);
        sb.append("&branchName=");
        sb.append(branchName);
        sb.append("&commitId=");
        sb.append(commitId);
        sb.append("&stage=");
        sb.append(stage);
        String url = sb.toString();

        LogUtil.info("redirect report url: " + url);
        response.sendRedirect(response.encodeRedirectURL(url));
    }

    /***
     * 根据测试场景id 查询其默认的 测试环境 数据
     * @param ids： 测试场景id 集合
     * @return
     */
    public Map<String, Map<String, String>> getDefaultEnvMap(Set<String> ids) {
        Map<String, Map<String, String>> map = new HashMap<>();

        List<ApiScenarioWithBLOBs> apiScenarios = apiScenarioMapper.selectByPrimaryKeys(ids);
        for(ApiScenarioWithBLOBs apiScenario : apiScenarios) {
            JSONObject jo = JSONObject.parseObject(apiScenario.getScenarioDefinition());
            JSONObject env = jo.getJSONObject("environmentMap");
            Map jsonMap = env.toJavaObject(Map.class);
            map.put(apiScenario.getId(), jsonMap);
        }

        return map;
    }
}