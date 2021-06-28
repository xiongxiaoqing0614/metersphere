package io.metersphere.tuhu.mapper;


import io.metersphere.api.dto.datacount.ApiDataCountResult;
import io.metersphere.api.dto.datacount.ExecutedCaseInfoResult;
import io.metersphere.base.domain.ApiDefinition;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import io.metersphere.base.domain.ApiTestCase;

import java.util.List;

public interface GraphMapper {
    List<String> queryApiAppIdList();

    Integer countApiTotal();

    Integer countApiDone();

    List<ApiDataCountResult> countApiCoverage();

    List<ApiDataCountResult> countExecuteResult();

    List<ExecutedCaseInfoResult> findFailureCaseInfoByExecuteTimeAndLimitNumber(long startTimestamp);

    List<ApiScenarioWithBLOBs> selectIdAndScenario(long start, long step);

    List<String> queryAppIdList();
}
