package io.metersphere.base.mapper;

import io.metersphere.base.domain.TuhuCodeCoverageRateMapping;
import java.util.List;


public interface TuhuCodeCoverageRateMappingMapper {
    List<TuhuCodeCoverageRateMapping> queryByTestPlanIds(String[] testPlanIds);

    List<TuhuCodeCoverageRateMapping> queryByTestReportIds(String[] testReportIds);

    int insert(TuhuCodeCoverageRateMapping codeCoverageBind);


}