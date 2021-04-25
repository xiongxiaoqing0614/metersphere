package io.metersphere.kanban.mapper;

import io.metersphere.kanban.dto.TestCaseSummaryDTO;
import java.util.List;

public interface KanbanMapper {
    List<TestCaseSummaryDTO> getSummary();
}
