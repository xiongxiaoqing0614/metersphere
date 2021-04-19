package io.metersphere.kanban.service;

import io.metersphere.kanban.dto.KanbanDTO;
import io.metersphere.kanban.mapper.KanbanMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class KanbanService {
    @Resource
    private KanbanMapper kanbanMapper;

    public List<KanbanDTO> getSummary() {
        List<KanbanDTO> list = kanbanMapper.getSummary();
        return list;
    }
}
