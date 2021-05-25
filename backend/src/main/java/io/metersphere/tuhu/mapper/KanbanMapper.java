package io.metersphere.tuhu.mapper;

import io.metersphere.tuhu.dto.TestCaseSummaryDTO;
import java.util.List;

public interface KanbanMapper {
    List<TestCaseSummaryDTO> getSummary();
}
