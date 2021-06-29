package io.metersphere.tuhu.mapper;

import io.metersphere.tuhu.dto.TuhuCodeCoverageRateResultDTO;
import java.util.List;


public interface TuhuCodeCoverageRateMappingMapper {
    List<TuhuCodeCoverageRateResultDTO> queryByTestPlanIds(String[] testPlanIds);

    List<TuhuCodeCoverageRateResultDTO> queryByTestReportIds(String[] testReportIds);

    int insert(TuhuCodeCoverageRateResultDTO codeCoverageBind);


}