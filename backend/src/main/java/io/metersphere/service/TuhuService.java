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
import java.util.ArrayList;
import java.util.List;
import io.metersphere.base.domain.TuhuCodeCoverageRateMapping;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;


@Service
@Transactional(rollbackFor = Exception.class)
public class TuhuService {

    @Value("${coverage.rate.fetch.url}")
    private String codeCoverageRateServerUrl;

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
        if (codeCoverageRateServerUrl.isEmpty()) {
            throw new ValueException("未配置代码覆盖率服务URL");
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
            lst.add(tuhuCodeCoverageRateResultDTO);
        }

        return lst;
    }

    private String fetchCodeCoverageData(String js) {
        LogUtil.info("codeCoverageRateServerUrl: " + codeCoverageRateServerUrl);
        LogUtil.info("code coverage rate request json: " + js);
        RestTemplate client = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(js, headers);
        ResponseEntity<String> response = client.exchange(codeCoverageRateServerUrl, HttpMethod.POST, requestEntity, String.class);

        return response.getBody();
    }
}
