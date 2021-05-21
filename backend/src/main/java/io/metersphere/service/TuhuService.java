package io.metersphere.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.TestPlan;
import io.metersphere.base.mapper.TestPlanMapper;
import io.metersphere.base.mapper.TuhuCodeCoverageRateMappingMapper;
import io.metersphere.controller.request.CodeCoverageBindRequest;
import io.metersphere.controller.request.CodeCoverageRequest;
import io.metersphere.dto.TuhuCodeCoverageRateResultDTO;
import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.metersphere.commons.utils.LogUtil;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import io.metersphere.base.domain.TuhuCodeCoverageRateMapping;
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

    public Integer addCodeCoverageRateMapping(CodeCoverageBindRequest codeCoverageBind) {
        TuhuCodeCoverageRateMapping tuhuCodeCoverageRateMapping = new TuhuCodeCoverageRateMapping();
        tuhuCodeCoverageRateMapping.setAppId(codeCoverageBind.getAppId());
        tuhuCodeCoverageRateMapping.setBranchName(codeCoverageBind.getBranchName());
        tuhuCodeCoverageRateMapping.setCommitId(codeCoverageBind.getCommitId());
        tuhuCodeCoverageRateMapping.setStage(codeCoverageBind.getStage());
        tuhuCodeCoverageRateMapping.setTestReportId(codeCoverageBind.getTestReportId());

        TestPlan testPlan = testPlanMapper.selectByConditions(codeCoverageBind);
        if (testPlan == null) {
            return null;
        }
        tuhuCodeCoverageRateMapping.setTestPlanId(testPlan.getId());
        return tuhuCodeCoverageRateMappingMapper.insert(tuhuCodeCoverageRateMapping);
    }

    public List<TuhuCodeCoverageRateResultDTO> getCodeCoverageRateList(CodeCoverageRequest codeCoverageRequests) {
        if (codeCoverageRateServerUrlPrefix.isEmpty()) {
            throw new ValueException("未配置代码覆盖率服务URL前缀");
        }
        List<TuhuCodeCoverageRateMapping> mappingList = null;
        String[] testPlanIds = codeCoverageRequests.getTestPlanIds();
        String[] testReportIds = codeCoverageRequests.getTestReportIds();
        if (testPlanIds != null) {
            mappingList = tuhuCodeCoverageRateMappingMapper.queryByTestPlanIds(testPlanIds);
        } else if (testReportIds != null) {
            mappingList = tuhuCodeCoverageRateMappingMapper.queryByTestReportIds(testReportIds);
        }
        String rjs = fetchCodeCoverageData(JSON.toJSONString(mappingList));
        LogUtil.info("code coverage rate result json: " + rjs);
        JSONObject jo = JSONObject.parseObject(rjs);
        if (jo == null || jo.getInteger("code") != 0) {
            return null;
        }

        List<TuhuCodeCoverageRateResultDTO> lst = new ArrayList<>();
        for (Object j : jo.getJSONArray("data")) {
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

    private String fetchCodeCoverageData(String js) {
        String codeCoverageRateServerUrl = codeCoverageRateServerUrlPrefix + "/api/coverager/data";
        LogUtil.info("codeCoverageRateServerUrl: " + codeCoverageRateServerUrl);
        LogUtil.info("code coverage rate request json: " + js);
        RestTemplate client = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(js, headers);
        ResponseEntity<String> response = client.exchange(codeCoverageRateServerUrl, HttpMethod.POST, requestEntity, String.class);

        return response.getBody();
    }

    public String getTestReportByTimestamp(CodeCoverageBindRequest codeCoverageBind) {
        return "9a286516-8515-4fca-8fc6-a1f47965804f";
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
}
