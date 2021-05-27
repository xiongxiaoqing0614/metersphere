package io.metersphere.tuhu.mapper;

import io.metersphere.base.domain.TestCaseReport;
import io.metersphere.tuhu.dto.TuhuOKRDTO;

import java.util.List;

public interface OKRMapper {
    List<TuhuOKRDTO> getOKR();
    int insert(TuhuOKRDTO record);
}
