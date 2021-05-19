package io.metersphere.controller;

import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.User;
import io.metersphere.controller.request.CodeCoverageRequest;
import io.metersphere.controller.request.CodeCoverageBindRequest;
import io.metersphere.dto.TuhuCodeCoverageRateResultDTO;
import io.metersphere.service.TuhuService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/tuhu")
public class TuhuController {

    @Resource
    private TuhuService tuhuService;

    @PostMapping(value = "/testplan/coveragerate")
    public List<TuhuCodeCoverageRateResultDTO> testPlanCoverageRate(@RequestBody CodeCoverageRequest codeCoverageRequests) {
        return tuhuService.getCodeCoverageRateList(codeCoverageRequests);
    }

    @PostMapping(value = "/testplan/coveragerate/bind")
    public Object testPlanCoverageRateBind(@RequestBody CodeCoverageBindRequest codeCoverageBindRequest) {
        Integer num = tuhuService.addCodeCoverageRateMapping(codeCoverageBindRequest);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("num", num);
        return jsonObject;
    }

}