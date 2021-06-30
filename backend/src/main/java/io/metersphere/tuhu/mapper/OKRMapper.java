package io.metersphere.tuhu.mapper;

import io.metersphere.base.domain.ApiDefinitionExecResult;
import io.metersphere.base.domain.ApiDefinitionExecResultExample;
import io.metersphere.base.domain.TestCaseReport;
import io.metersphere.tuhu.dto.AppIdCoverageDTO;
import io.metersphere.tuhu.dto.TuhuOKRDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OKRMapper {
    List<TuhuOKRDTO> getOKR();
    List<TuhuOKRDTO> getOKRById(String id);
    List<TuhuOKRDTO> getOKRByName(String name);
    List<TuhuOKRDTO> getOKRByNameAndWSId(String okrName, String wsId);
    int insert(TuhuOKRDTO record);
    int update(@Param("record") TuhuOKRDTO record);
    List<String> getOKRNames();
    List<AppIdCoverageDTO> getAppIdCoverage();
}
