package io.metersphere.kanban.mapper;

import io.metersphere.kanban.dto.KanbanDTO;
import java.util.List;

public interface KanbanMapper {
    List<KanbanDTO> getSummary();
}
